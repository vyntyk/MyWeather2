package com.home.myweather;

import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// BuildConfig генерируется Gradle в том же пакете com.home.myweather.
// Если IDE подсвечивает ошибку — выполните File → Sync Project with Gradle Files.

/**
 * Шаг 2: Current Weather API → погода по координатам.
 * Для поиска по городу делегирует геокодирование в GeocodingRepository.
 */
public class WeatherRepository {

    private static final String TAG   = "WeatherRepository";
    private static final String UNITS = "metric";
    private static final String LANG  = "ru";

    public interface WeatherCallback {
        void onSuccess(WeatherResponse weather, GeoLocation geo);
        void onError(String message);
    }

    private final String               apiKey      = BuildConfig.OPENWEATHER_API_KEY;
    private final WeatherApiService    apiService  = RetrofitClient.getInstance().getApiService();
    private final GeocodingRepository  geocoding   = new GeocodingRepository();
    private final Object               lock        = new Object();
    private       Call<WeatherResponse> weatherCall;
    private       Call<ForecastResponse> forecastCall;
    private       long                 requestId   = 0L;

    // ── По названию города ────────────────────────────────────────────────
    public void fetchWeather(String city, String country, WeatherCallback callback) {
        if (!validate(city, callback)) return;
        String query = (country != null && !country.trim().isEmpty())
                ? city.trim() + "," + country.trim() : city.trim();
        final long rid;
        synchronized (lock) { cancelLocked(); rid = ++requestId; }

        geocoding.fetch(query, apiKey(), rid, new GeocodingRepository.Callback2() {
            @Override public void onSuccess(GeoLocation geo) {
                fetchCurrentWeather(geo, callback, rid);
            }
            @Override public void onError(String message) { callback.onError(message); }
        });
    }

    // ── По координатам GPS ────────────────────────────────────────────────
    public void fetchWeatherByCoords(double lat, double lon, WeatherCallback callback) {
        if (callback == null) throw new IllegalArgumentException("callback must not be null");
        GeoLocation geo = new GeoLocation();
        geo.lat = lat; geo.lon = lon; geo.name = "GPS";
        final long rid;
        synchronized (lock) { cancelLocked(); rid = ++requestId; }
        fetchCurrentWeather(geo, callback, rid);
    }

    // ── Прогноз на 5 дней ─────────────────────────────────────────────────
    public void fetchForecast(double lat, double lon, ForecastCallback callback) {
        if (callback == null) throw new IllegalArgumentException("callback must not be null");
        final long rid;
        synchronized (lock) { cancelLocked(); rid = ++requestId; }

        final Call<ForecastResponse> call = apiService.get5DayForecast(lat, lon, apiKey(), UNITS, LANG);
        synchronized (lock) { forecastCall = call; }

        call.enqueue(new Callback<ForecastResponse>() {
            @Override public void onResponse(Call<ForecastResponse> c, Response<ForecastResponse> r) {
                if (c.isCanceled() || rid != requestId) return;
                ForecastResponse body = r.body();
                if (!r.isSuccessful() || body == null || body.list == null || body.list.isEmpty()) {
                    callback.onError("Ошибка прогноза (код " + r.code() + ")"); return;
                }
                callback.onSuccess(body);
            }
            @Override public void onFailure(Call<ForecastResponse> c, Throwable t) {
                if (!c.isCanceled() && rid == requestId) callback.onError(networkError(t));
            }
        });
    }

    public interface ForecastCallback {
        void onSuccess(ForecastResponse forecast);
        void onError(String message);
    }
    private void fetchCurrentWeather(GeoLocation geo, WeatherCallback callback, long rid) {
        final Call<WeatherResponse> call;
        synchronized (lock) {
            if (rid != requestId) return;
            call = apiService.getCurrentWeather(geo.lat, geo.lon, apiKey(), UNITS, LANG);
            weatherCall = call;
        }
        call.enqueue(new Callback<WeatherResponse>() {
            @Override public void onResponse(Call<WeatherResponse> c, Response<WeatherResponse> r) {
                if (c.isCanceled() || rid != requestId) return;
                WeatherResponse body = r.body();
                if (!r.isSuccessful() || body == null) {
                    callback.onError("Ошибка погоды (код " + r.code() + ")"); return;
                }
                if (body.getMain() == null) {
                    callback.onError("Неполный ответ для " + geo.name); return;
                }
                callback.onSuccess(body, geo);
            }
            @Override public void onFailure(Call<WeatherResponse> c, Throwable t) {
                if (!c.isCanceled() && rid == requestId)
                    callback.onError(networkError(t));
            }
        });
    }

    public void cancelPendingRequests() {
        synchronized (lock) { cancelLocked(); requestId++; }
    }

    private void cancelLocked() {
        geocoding.cancel();
        if (weatherCall != null && !weatherCall.isCanceled()) weatherCall.cancel();
        if (forecastCall != null && !forecastCall.isCanceled()) forecastCall.cancel();
    }

    private boolean validate(String city, WeatherCallback cb) {
        if (cb == null) throw new IllegalArgumentException("callback must not be null");
        String key = apiKey();
        if (key == null || key.trim().isEmpty()) {
            cb.onError("API-ключ не настроен"); return false;
        }
        if (city == null || city.trim().isEmpty()) {
            cb.onError("Укажите название города"); return false;
        }
        return true;
    }

    private String apiKey() { return apiKey; }

    private static String networkError(Throwable t) {
        String m = t != null ? t.getMessage() : null;
        return "Ошибка сети" + (m != null && !m.isEmpty() ? ": " + m : "");
    }
}
