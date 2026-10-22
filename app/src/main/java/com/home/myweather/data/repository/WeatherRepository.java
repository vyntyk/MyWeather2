package com.home.myweather.data.repository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

import com.home.myweather.data.mapper.MetNoMapper;
import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.data.model.ForecastResponse;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.model.MetNoForecastResponse;
import com.home.myweather.data.model.WeatherResponse;
import com.home.myweather.data.network.WeatherApiService;
import com.home.myweather.helpers.NetworkMonitor;
import javax.inject.Inject;

import android.util.Log;

/**
 * Repository for current weather and forecasts from MET Norway.
 *
 * Оркестрирует запросы, кэширование и обработку ошибок.
 * Uses MetNoMapper to convert API responses to app models.
 *
 * API-ключ не нужен. Open-Meteo используется для текущей погоды и прогноза.
 *
 * Оффлайн-режим: если сети нет, запрос к API не отправляется вовсе —
 * пользователю отдаются данные из {@link WeatherStorage} (если они есть).
 * Каждый успешный ответ API дополнительно сохраняется в WeatherStorage,
 * чтобы прогноз «запоминался» между запусками приложения.
 */
public class WeatherRepository {

    private static final String TAG = "WeatherRepository";

    // ── Параметры запроса ─────────────────────────────────────────────────

    // ── Интерфейсы обратного вызова ───────────────────────────────────────

    public interface WeatherCallback {
        void onSuccess(WeatherResponse weather, GeoLocation geo);
        void onError(String message);
    }

    public interface ForecastCallback {
        void onSuccess(ForecastResponse forecast);
        void onError(String message);
    }

    // ── Поля ──────────────────────────────────────────────────────────────

    private final WeatherApiService apiService;
    private final GeocodingRepository geocoding;
    private final WeatherStorage weatherCache;
    private final NetworkMonitor networkMonitor;
    private final Object lock = new Object();

    private Call<MetNoForecastResponse> weatherCall;
    private Call<MetNoForecastResponse> forecastCall;
    private long weatherRequestId = 0L;
    private long forecastRequestId = 0L;

    // ── Конструктор с внедрением зависимостей ────────────────────────────

    public WeatherRepository(WeatherApiService apiService,
                             GeocodingRepository geocoding,
                             WeatherStorage weatherCache,
                             NetworkMonitor networkMonitor) {
        this.apiService = apiService;
        this.geocoding = geocoding;
        this.weatherCache = weatherCache;
        this.networkMonitor = networkMonitor;
    }

    // ── Публичный API ─────────────────────────────────────────────────────

    /**
     * Получить текущую погоду по названию города.
     */
    public void fetchWeather(String city, String country, WeatherCallback callback) {
        if (callback == null) throw new IllegalArgumentException("callback must not be null");
        if (city == null || city.trim().isEmpty()) {
            callback.onError("Укажите название города");
            return;
        }
        String query = (country != null && !country.trim().isEmpty())
                ? city.trim() + "," + country.trim()
                : city.trim();

        // Оффлайн: запрос к API не отправляем, отдаём сохранённые данные
        if (!isOnline()) {
            WeatherResponse cachedWeather = weatherCache.loadWeather();
            GeoLocation cachedGeo = weatherCache.loadGeo();
            if (cachedWeather != null && cachedGeo != null) {
                callback.onSuccess(cachedWeather, cachedGeo);
            } else {
                callback.onError("Нет интернета. Сохранённых данных пока нет — подключитесь к сети");
            }
            return;
        }

        final long rid;
        synchronized (lock) {
            cancelWeatherLocked();
            rid = ++weatherRequestId;
        }

        geocoding.fetch(query, rid, new GeocodingRepository.Callback2() {
            @Override
            public void onSuccess(GeoLocation geo) {
                fetchCurrentWeather(geo, callback, rid);
            }
            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    /**
     * Получить текущую погоду по GPS-координатам.
     */
    public void fetchWeatherByCoords(double lat, double lon, WeatherCallback callback) {
        if (callback == null) throw new IllegalArgumentException("callback must not be null");

        // Оффлайн: запрос к API не отправляем, отдаём сохранённые данные
        if (!isOnline()) {
            WeatherResponse cachedWeather = weatherCache.loadWeather();
            GeoLocation cachedGeo = weatherCache.loadGeo();
            if (cachedWeather != null && cachedGeo != null) {
                callback.onSuccess(cachedWeather, cachedGeo);
            } else {
                callback.onError("Нет интернета. Сохранённых данных пока нет");
            }
            return;
        }

        GeoLocation geo = new GeoLocation();
        geo.lat = lat;
        geo.lon = lon;
        geo.name = "GPS";

        final long rid;
        synchronized (lock) {
            cancelWeatherLocked();
            rid = ++weatherRequestId;
        }
        fetchCurrentWeather(geo, callback, rid);
    }

    /**
     * Получить текущую погоду по координатам ПАРАЛЕЛЬНО (для карты).
     * В отличие от {@link #fetchWeatherByCoords}, НЕ отменяет предыдущие запросы
     * и НЕ трогает идентификаторы запросов — чтобы несколько запросов (метки температуры
     * на карте) выполнялись одновременно и не отменяли друг друга.
     */
    public void fetchCurrentWeatherMarker(double lat, double lon, WeatherCallback callback) {
        if (callback == null) throw new IllegalArgumentException("callback must not be null");

        // Оффлайн: метки температуры на карте не запрашиваем
        if (!isOnline()) {
            callback.onError("Нет интернета");
            return;
        }

        GeoLocation geo = new GeoLocation();
        geo.lat = lat;
        geo.lon = lon;
        geo.name = "GPS";

        Log.d(TAG, "fetchCurrentWeatherMarker: lat=" + lat + ", lon=" + lon);

        final Call<MetNoForecastResponse> call = buildForecastCall(lat, lon);
        call.enqueue(new Callback<MetNoForecastResponse>() {
            @Override
            public void onResponse(Call<MetNoForecastResponse> c,
                                   Response<MetNoForecastResponse> r) {
                if (c.isCanceled()) return;
                MetNoForecastResponse body = r.body();
                Log.d(TAG, "fetchCurrentWeatherMarker onResponse: code=" + r.code() + ", body=" + (body != null ? "present" : "null")
                        + ", timeseries=" + (body != null && body.properties != null
                        && body.properties.timeSeries != null ? body.properties.timeSeries.size() : "null"));
                if (!r.isSuccessful() || body == null || body.properties == null
                        || body.properties.timeSeries == null || body.properties.timeSeries.isEmpty()) {
                    callback.onError("Ошибка погоды (код " + r.code() + ")");
                    return;
                }
                WeatherResponse wr = MetNoMapper.toWeatherResponse(body, geo);
                callback.onSuccess(wr, geo);
            }

            @Override
            public void onFailure(Call<MetNoForecastResponse> c, Throwable t) {
                if (!c.isCanceled()) {
                    String errorMsg = networkError(t);
                    Log.w(TAG, "fetchCurrentWeatherMarker onFailure: " + errorMsg, t);
                    callback.onError(errorMsg);
                }
            }
        });
    }

    /**
     * Получить почасовой прогноз на 7 дней.
     */
    public void fetchForecast(double lat, double lon, ForecastCallback callback) {
        if (callback == null) throw new IllegalArgumentException("callback must not be null");

        // Оффлайн: запрос к API не отправляем, отдаём сохранённый прогноз
        if (!isOnline()) {
            java.util.ArrayList<ForecastItem> cached = weatherCache.loadHourly();
            if (!cached.isEmpty()) {
                ForecastResponse fr = new ForecastResponse();
                fr.list = cached;
                callback.onSuccess(fr);
            } else {
                callback.onError("Нет интернета. Сохранённого прогноза пока нет");
            }
            return;
        }

        final long rid;
        synchronized (lock) {
            cancelForecastLocked();
            rid = ++forecastRequestId;
        }

        Log.d(TAG, "fetchForecast: lat=" + lat + ", lon=" + lon + ", rid=" + rid);

        final Call<MetNoForecastResponse> call = buildForecastCall(lat, lon);
        synchronized (lock) {
            forecastCall = call;
        }

        call.enqueue(new Callback<MetNoForecastResponse>() {
            @Override
            public void onResponse(Call<MetNoForecastResponse> c,
                                   Response<MetNoForecastResponse> r) {
                if (c.isCanceled() || rid != forecastRequestId) return;
                MetNoForecastResponse body = r.body();
                Log.d(TAG, "fetchForecast onResponse: code=" + r.code() + ", body=" + (body != null ? "present" : "null") 
                        + ", timeseries=" + (body != null && body.properties != null
                        && body.properties.timeSeries != null ? body.properties.timeSeries.size() : "null"));
                if (!r.isSuccessful() || body == null || body.properties == null
                        || body.properties.timeSeries == null) {
                    // API вернул ошибку — отдаём сохранённый прогноз
                    if (serveCachedForecast(callback)) return;
                    String errorMsg = "Ошибка прогноза (код " + r.code() + ")";
                    Log.w(TAG, errorMsg);
                    callback.onError(errorMsg);
                    return;
                }
                
                // Convert the API response to the app model.
                List<ForecastItem> items = MetNoMapper.toForecastItems(body);
                ForecastCache.put(lat, lon, items);

                // Постоянно запоминаем прогноз для оффлайн-режима
                GeoLocation forecastGeo = new GeoLocation();
                forecastGeo.lat = lat;
                forecastGeo.lon = lon;
                forecastGeo.name = "GPS";
                weatherCache.saveForecast(items, forecastGeo);

                ForecastResponse fr = new ForecastResponse();
                fr.list = items;
                callback.onSuccess(fr);
            }

            @Override
            public void onFailure(Call<MetNoForecastResponse> c, Throwable t) {
                if (!c.isCanceled() && rid == forecastRequestId) {
                    // Сеть «есть», но API недоступен: отдаём сохранённый прогноз.
                    if (serveCachedForecast(callback)) return;
                    String errorMsg = networkError(t);
                    Log.w(TAG, "fetchForecast onFailure: " + errorMsg, t);
                    callback.onError(errorMsg);
                }
            }
        });
    }

    /**
     * Отменить все текущие запросы.
     */
    public void cancelPendingRequests() {
        synchronized (lock) { 
            cancelWeatherLocked();
            cancelForecastLocked();
            weatherRequestId++;
            forecastRequestId++;
        }
    }

    // ── Внутренняя логика ─────────────────────────────────────────────────

    private void fetchCurrentWeather(GeoLocation geo, WeatherCallback callback, long rid) {
        final Call<MetNoForecastResponse> call;
        synchronized (lock) {
            if (rid != weatherRequestId) return;
            call = buildCurrentWeatherCall(geo.lat, geo.lon);
            weatherCall = call;
        }

        Log.d(TAG, "fetchCurrentWeather: lat=" + geo.lat + ", lon=" + geo.lon + ", rid=" + rid);

        call.enqueue(new Callback<MetNoForecastResponse>() {
            @Override
            public void onResponse(Call<MetNoForecastResponse> c,
                                   Response<MetNoForecastResponse> r) {
                if (c.isCanceled() || rid != weatherRequestId) return;
                MetNoForecastResponse body = r.body();
                Log.d(TAG, "onResponse: code=" + r.code() + ", body=" + (body != null ? "present" : "null") 
                        + ", timeseries=" + (body != null && body.properties != null
                        && body.properties.timeSeries != null ? body.properties.timeSeries.size() : "null"));
                if (!r.isSuccessful() || body == null || body.properties == null
                        || body.properties.timeSeries == null || body.properties.timeSeries.isEmpty()) {
                    // API вернул ошибку — отдаём сохранённую погоду
                    if (serveCachedWeather(callback)) return;
                    String errorMsg = "Ошибка погоды (код " + r.code() + ")";
                    Log.w(TAG, errorMsg);
                    callback.onError(errorMsg);
                    return;
                }
                
                // Convert the API response to the app model.
                WeatherResponse wr = MetNoMapper.toWeatherResponse(body, geo);

                weatherCache.saveWeather(wr, geo);
                callback.onSuccess(wr, geo);
            }

            @Override
            public void onFailure(Call<MetNoForecastResponse> c, Throwable t) {
                if (!c.isCanceled() && rid == weatherRequestId) {
                    // Сеть «есть», но API недоступен: показываем сохранённые данные,
                    // а не ошибку — прогноз должен оставаться доступным оффлайн.
                    if (serveCachedWeather(callback)) return;
                    String errorMsg = networkError(t);
                    Log.w(TAG, "onFailure: " + errorMsg, t);
                    callback.onError(errorMsg);
                }
            }
        });
    }

    private Call<MetNoForecastResponse> buildForecastCall(double lat, double lon) {
        return apiService.getForecast(
                String.format(java.util.Locale.US, "%.4f", lat),
                String.format(java.util.Locale.US, "%.4f", lon)
        );
    }

    private Call<MetNoForecastResponse> buildCurrentWeatherCall(double lat, double lon) {
        return buildForecastCall(lat, lon);
    }

    // ── Вспомогательные методы ────────────────────────────────────────────

    /**
     * @return true, если есть доступ в интернет.
     * При отсутствии сети никакие запросы к API не отправляются.
     */
    private boolean isOnline() {
        return networkMonitor != null && networkMonitor.isOnline();
    }

    /**
     * Попытаться отдать сохранённые данные текущей погоды.
     *
     * @return true, если кэш есть и вызывающему был доставлен onSuccess.
     */
    private boolean serveCachedWeather(WeatherCallback callback) {
        WeatherResponse cachedWeather = weatherCache.loadWeather();
        GeoLocation cachedGeo = weatherCache.loadGeo();
        if (cachedWeather == null || cachedGeo == null) return false;
        callback.onSuccess(cachedWeather, cachedGeo);
        return true;
    }

    /**
     * Попытаться отдать сохранённый прогноз.
     *
     * @return true, если кэш есть и вызывающему был доставлен onSuccess.
     */
    private boolean serveCachedForecast(ForecastCallback callback) {
        java.util.ArrayList<ForecastItem> cached = weatherCache.loadHourly();
        if (cached.isEmpty()) return false;
        ForecastResponse fr = new ForecastResponse();
        fr.list = cached;
        callback.onSuccess(fr);
        return true;
    }

    private void cancelWeatherLocked() {
        geocoding.cancel();
        if (weatherCall != null && !weatherCall.isCanceled()) {
            weatherCall.cancel();
        }
    }

    private void cancelForecastLocked() {
        if (forecastCall != null && !forecastCall.isCanceled()) {
            forecastCall.cancel();
        }
    }

    private static String networkError(Throwable t) {
        String msg = t != null ? t.getMessage() : null;
        return "Ошибка сети" + (msg != null && !msg.isEmpty() ? ": " + msg : "");
    }
}
