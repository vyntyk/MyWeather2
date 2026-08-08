package com.home.myweather.data.repository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

import com.home.myweather.data.mapper.OpenMeteoMapper;
import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.data.model.ForecastResponse;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.model.OpenMeteoForecastResponse;
import com.home.myweather.data.model.WeatherResponse;
import com.home.myweather.data.network.WeatherApiService;
import com.home.myweather.helpers.NetworkMonitor;
import javax.inject.Inject;

/**
 * Репозиторий погоды на базе Open-Meteo.
 *
 * Оркестрирует запросы, кэширование и обработку ошибок.
 * Использует OpenMeteoMapper для преобразования API-ответов в модели приложения.
 *
 * API-ключ не нужен. Open-Meteo используется для текущей погоды и прогноза.
 *
 * Оффлайн-режим: если сети нет, запрос к API не отправляется вовсе —
 * пользователю отдаются данные из {@link WeatherStorage} (если они есть).
 * Каждый успешный ответ API дополнительно сохраняется в WeatherStorage,
 * чтобы прогноз «запоминался» между запусками приложения.
 */
public class WeatherRepository {

    // ── Параметры запроса ─────────────────────────────────────────────────

    private static final String CURRENT_PARAMS =
            "temperature_2m,relative_humidity_2m,apparent_temperature," +
            "precipitation_probability,precipitation,weather_code,cloud_cover," +
            "surface_pressure,wind_speed_10m,wind_direction_10m,is_day";

    private static final String HOURLY_PARAMS =
            "temperature_2m,relative_humidity_2m,apparent_temperature," +
            "precipitation_probability,weather_code,wind_speed_10m," +
            "wind_direction_10m,surface_pressure,visibility";

    private static final String DAILY_PARAMS =
            "weather_code,temperature_2m_max,temperature_2m_min," +
            "precipitation_probability_max,wind_speed_10m_max,sunrise,sunset";

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

    private Call<OpenMeteoForecastResponse> weatherCall;
    private Call<OpenMeteoForecastResponse> forecastCall;
    private long requestId = 0L;

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
            cancelLocked();
            rid = ++requestId; 
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
            cancelLocked();
            rid = ++requestId; 
        }
        fetchCurrentWeather(geo, callback, rid);
    }

    /**
     * Получить текущую погоду по координатам ПАРАЛЕЛЬНО (для карты).
     * В отличие от {@link #fetchWeatherByCoords}, НЕ отменяет предыдущие запросы
     * и НЕ трогает общий requestId — чтобы несколько запросов (метки температуры
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

        final Call<OpenMeteoForecastResponse> call = buildForecastCall(lat, lon);
        call.enqueue(new Callback<OpenMeteoForecastResponse>() {
            @Override
            public void onResponse(Call<OpenMeteoForecastResponse> c,
                                   Response<OpenMeteoForecastResponse> r) {
                if (c.isCanceled()) return;
                OpenMeteoForecastResponse body = r.body();
                if (!r.isSuccessful() || body == null || body.current == null) {
                    callback.onError("Ошибка погоды (код " + r.code() + ")");
                    return;
                }
                WeatherResponse wr = OpenMeteoMapper.toWeatherResponse(body, geo);
                callback.onSuccess(wr, geo);
            }

            @Override
            public void onFailure(Call<OpenMeteoForecastResponse> c, Throwable t) {
                if (!c.isCanceled()) {
                    callback.onError(networkError(t));
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
            cancelLocked();
            rid = ++requestId; 
        }

        final Call<OpenMeteoForecastResponse> call = buildForecastCall(lat, lon);
        synchronized (lock) { 
            forecastCall = call; 
        }

        call.enqueue(new Callback<OpenMeteoForecastResponse>() {
            @Override
            public void onResponse(Call<OpenMeteoForecastResponse> c,
                                   Response<OpenMeteoForecastResponse> r) {
                if (c.isCanceled() || rid != requestId) return;
                OpenMeteoForecastResponse body = r.body();
                if (!r.isSuccessful() || body == null || body.hourly == null) {
                    // API вернул ошибку — отдаём сохранённый прогноз
                    if (serveCachedForecast(callback)) return;
                    callback.onError("Ошибка прогноза (код " + r.code() + ")");
                    return;
                }
                
                // Используем OpenMeteoMapper для преобразования
                List<ForecastItem> items = OpenMeteoMapper.toForecastItems(body);
                ForecastCache.put(lat, lon, items);

                // Постоянно запоминаем прогноз для оффлайн-режима
                weatherCache.saveForecast(items);

                ForecastResponse fr = new ForecastResponse();
                fr.list = items;
                callback.onSuccess(fr);
            }

            @Override
            public void onFailure(Call<OpenMeteoForecastResponse> c, Throwable t) {
                if (!c.isCanceled() && rid == requestId) {
                    // Сеть «есть», но API недоступен: отдаём сохранённый прогноз.
                    if (serveCachedForecast(callback)) return;
                    callback.onError(networkError(t));
                }
            }
        });
    }

    /**
     * Отменить все текущие запросы.
     */
    public void cancelPendingRequests() {
        synchronized (lock) { 
            cancelLocked();
            requestId++; 
        }
    }

    // ── Внутренняя логика ─────────────────────────────────────────────────

    private void fetchCurrentWeather(GeoLocation geo, WeatherCallback callback, long rid) {
        final Call<OpenMeteoForecastResponse> call;
        synchronized (lock) {
            if (rid != requestId) return;
            call = buildForecastCall(geo.lat, geo.lon);
            weatherCall = call;
        }

        call.enqueue(new Callback<OpenMeteoForecastResponse>() {
            @Override
            public void onResponse(Call<OpenMeteoForecastResponse> c,
                                   Response<OpenMeteoForecastResponse> r) {
                if (c.isCanceled() || rid != requestId) return;
                OpenMeteoForecastResponse body = r.body();
                if (!r.isSuccessful() || body == null || body.current == null) {
                    // API вернул ошибку — отдаём сохранённую погоду
                    if (serveCachedWeather(callback)) return;
                    callback.onError("Ошибка погоды (код " + r.code() + ")");
                    return;
                }
                
                // Кэшируем почасовые данные — ForecastFragment их возьмёт без нового запроса
                List<ForecastItem> items = OpenMeteoMapper.toForecastItems(body);
                ForecastCache.put(geo.lat, geo.lon, items);

                // Используем OpenMeteoMapper для преобразования
                WeatherResponse wr = OpenMeteoMapper.toWeatherResponse(body, geo);

                // Постоянно запоминаем текущую погоду + прогноз для оффлайн-режима
                weatherCache.save(wr, geo, items);
                callback.onSuccess(wr, geo);
            }

            @Override
            public void onFailure(Call<OpenMeteoForecastResponse> c, Throwable t) {
                if (!c.isCanceled() && rid == requestId) {
                    // Сеть «есть», но API недоступен: показываем сохранённые данные,
                    // а не ошибку — прогноз должен оставаться доступным оффлайн.
                    if (serveCachedWeather(callback)) return;
                    callback.onError(networkError(t));
                }
            }
        });
    }

    private Call<OpenMeteoForecastResponse> buildForecastCall(double lat, double lon) {
        return apiService.getForecast(
                lat, lon,
                CURRENT_PARAMS, HOURLY_PARAMS, DAILY_PARAMS,
                "ms",    // скорость ветра в м/с
                "auto",  // часовой пояс по координатам
                7        // дней
        );
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

    private void cancelLocked() {
        geocoding.cancel();
        if (weatherCall != null && !weatherCall.isCanceled()) {
            weatherCall.cancel();
        }
        if (forecastCall != null && !forecastCall.isCanceled()) {
            forecastCall.cancel();
        }
    }

    private static String networkError(Throwable t) {
        String msg = t != null ? t.getMessage() : null;
        return "Ошибка сети" + (msg != null && !msg.isEmpty() ? ": " + msg : "");
    }
}
