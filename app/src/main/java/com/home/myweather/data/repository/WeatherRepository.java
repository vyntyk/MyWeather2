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
import javax.inject.Inject;

/**
 * Репозиторий погоды на базе Open-Meteo.
 *
 * Оркестрирует запросы, кэширование и обработку ошибок.
 * Использует OpenMeteoMapper для преобразования API-ответов в модели приложения.
 *
 * API-ключ не нужен. Open-Meteo используется для текущей погоды и прогноза.
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
    private final Object lock = new Object();

    private Call<OpenMeteoForecastResponse> weatherCall;
    private Call<OpenMeteoForecastResponse> forecastCall;
    private long requestId = 0L;

    // ── Конструктор с внедрением зависимостей ────────────────────────────

    public WeatherRepository(WeatherApiService apiService, GeocodingRepository geocoding) {
        this.apiService = apiService;
        this.geocoding = geocoding;
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
     * Получить почасовой прогноз на 7 дней.
     */
    public void fetchForecast(double lat, double lon, ForecastCallback callback) {
        if (callback == null) throw new IllegalArgumentException("callback must not be null");

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
                    callback.onError("Ошибка прогноза (код " + r.code() + ")");
                    return;
                }
                
                // Используем OpenMeteoMapper для преобразования
                List<ForecastItem> items = OpenMeteoMapper.toForecastItems(body);
                ForecastCache.put(lat, lon, items);

                ForecastResponse fr = new ForecastResponse();
                fr.list = items;
                callback.onSuccess(fr);
            }

            @Override
            public void onFailure(Call<OpenMeteoForecastResponse> c, Throwable t) {
                if (!c.isCanceled() && rid == requestId) {
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
                    callback.onError("Ошибка погоды (код " + r.code() + ")");
                    return;
                }
                
                // Кэшируем почасовые данные — ForecastFragment их возьмёт без нового запроса
                List<ForecastItem> items = OpenMeteoMapper.toForecastItems(body);
                ForecastCache.put(geo.lat, geo.lon, items);

                // Используем OpenMeteoMapper для преобразования
                WeatherResponse wr = OpenMeteoMapper.toWeatherResponse(body, geo);
                callback.onSuccess(wr, geo);
            }

            @Override
            public void onFailure(Call<OpenMeteoForecastResponse> c, Throwable t) {
                if (!c.isCanceled() && rid == requestId) {
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
