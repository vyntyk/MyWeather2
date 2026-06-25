package com.home.myweather;

import android.util.Log;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Репозиторий погоды.
 *
 * Двухшаговый процесс:
 *   1. Geocoding API       → получаем lat/lon по названию города
 *   2. Current Weather 2.5 → получаем текущую погоду по координатам
 *
 * Использование:
 *   WeatherRepository repo = new WeatherRepository();
 *   repo.fetchWeather("Kyiv", "UA", new WeatherRepository.WeatherCallback() {
 *       @Override public void onSuccess(WeatherResponse weather, GeoLocation geo) { ... }
 *       @Override public void onError(String message) { ... }
 *   });
 *
 * Примечание: callback всегда вызывается в потоке Retrofit (фоновый поток).
 * Обновление UI внутри callback необходимо выполнять через runOnUiThread().
 */
public class WeatherRepository {

    private static final String TAG   = "WeatherRepository";
    private static final String UNITS = "metric";  // "imperial" для °F
    private static final String LANG  = "ru";      // язык описания погоды

    private final WeatherApiService apiService =
            RetrofitClient.getInstance().getApiService();

    private final Object requestLock = new Object();
    private Call<List<GeoLocation>> geocodeCall;
    private Call<WeatherResponse>   weatherCall;
    private long requestId = 0L;

    // ──────────────────────────────────────────────────────────────────────
    /** Публичный интерфейс обратного вызова */
    public interface WeatherCallback {
        /**
         * Вызывается в фоновом потоке Retrofit.
         * Для обновления UI используйте runOnUiThread().
         */
        void onSuccess(WeatherResponse weather, GeoLocation geo);

        /**
         * Вызывается в фоновом потоке Retrofit.
         * Для обновления UI используйте runOnUiThread().
         */
        void onError(String message);
    }

    // ──────────────────────────────────────────────────────────────────────
    /**
     * Запрашивает погоду напрямую по координатам (GPS / FusedLocation).
     * Геокодирование не нужно — пропускаем шаг 1 и сразу делаем шаг 2.
     *
     * @param lat      широта
     * @param lon      долгота
     * @param callback результат или ошибка (вызывается в фоновом потоке)
     */
    public void fetchWeatherByCoords(double lat, double lon, WeatherCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback must not be null");
        }
        String apiKey = BuildConfig.OPENWEATHER_API_KEY;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            callback.onError("API-ключ не настроен. Добавьте OPENWEATHER_API_KEY в local.properties");
            return;
        }
        // Создаём заглушку-заголовок GeoLocation с GPS-координатами
        GeoLocation gpsGeo = new GeoLocation();
        gpsGeo.lat  = lat;
        gpsGeo.lon  = lon;
        gpsGeo.name = "GPS";

        final long currentRequestId;
        synchronized (requestLock) {
            cancelPendingRequestsLocked();
            currentRequestId = ++requestId;
        }
        fetchCurrentWeather(gpsGeo, apiKey, callback, currentRequestId);
    }

    /**
     * Запрашивает погоду по названию города.
     *
     * @param city     название города, например "London"
     * @param country  код страны (ISO 3166-1 alpha-2), например "GB"; можно null
     * @param callback результат или ошибка (вызывается в фоновом потоке)
     */
    public void fetchWeather(String city, String country, WeatherCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback must not be null");
        }

        String apiKey = BuildConfig.OPENWEATHER_API_KEY;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            callback.onError("API-ключ не настроен. Добавьте OPENWEATHER_API_KEY в local.properties");
            return;
        }

        if (city == null || city.trim().isEmpty()) {
            callback.onError("Укажите название города");
            return;
        }

        String trimmedCity = city.trim();
        String query = (country != null && !country.trim().isEmpty())
                ? trimmedCity + "," + country.trim()
                : trimmedCity;

        final long currentRequestId;
        final Call<List<GeoLocation>> call;
        synchronized (requestLock) {
            cancelPendingRequestsLocked();
            currentRequestId = ++requestId;
            call = apiService.getCoordinates(query, 1, apiKey);
            geocodeCall = call;
        }

        // ── ШАГ 1: Geocoding ──────────────────────────────────────────────
        call.enqueue(new Callback<List<GeoLocation>>() {

            @Override
            public void onResponse(Call<List<GeoLocation>> call,
                                   Response<List<GeoLocation>> response) {
                if (!isLatestRequest(currentRequestId) || call.isCanceled()) {
                    return;
                }

                List<GeoLocation> locations = response.body();
                if (!response.isSuccessful() || locations == null || locations.isEmpty()) {
                    callback.onError("Город не найден: " + query);
                    return;
                }

                GeoLocation geo = locations.get(0);
                if (geo == null) {
                    callback.onError("Город не найден: " + query);
                    return;
                }

                Log.d(TAG, "Координаты: " + geo.lat + ", " + geo.lon);

                // ── ШАГ 2: Current Weather 2.5 ────────────────────────────
                fetchCurrentWeather(geo, apiKey, callback, currentRequestId);
            }

            @Override
            public void onFailure(Call<List<GeoLocation>> call, Throwable t) {
                if (!isLatestRequest(currentRequestId) || call.isCanceled()) {
                    return;
                }
                Log.e(TAG, "Ошибка геокодирования", t);
                callback.onError(networkErrorMessage(t));
            }
        });
    }

    // ──────────────────────────────────────────────────────────────────────
    private void fetchCurrentWeather(GeoLocation geo, String apiKey,
                                     WeatherCallback callback, long currentRequestId) {
        final Call<WeatherResponse> call;
        synchronized (requestLock) {
            if (!isLatestRequest(currentRequestId)) {
                return;
            }
            call = apiService.getCurrentWeather(geo.lat, geo.lon, apiKey, UNITS, LANG);
            weatherCall = call;
        }

        call.enqueue(new Callback<WeatherResponse>() {

            @Override
            public void onResponse(Call<WeatherResponse> call,
                                   Response<WeatherResponse> response) {
                if (!isLatestRequest(currentRequestId) || call.isCanceled()) {
                    return;
                }

                WeatherResponse body = response.body();
                if (!response.isSuccessful() || body == null) {
                    callback.onError("Не удалось получить погоду (код " + response.code() + ")");
                    return;
                }

                if (!isValidWeatherResponse(body)) {
                    callback.onError("Неполный ответ погоды для " + geo.name);
                    return;
                }

                callback.onSuccess(body, geo);
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                if (!isLatestRequest(currentRequestId) || call.isCanceled()) {
                    return;
                }
                Log.e(TAG, "Ошибка получения погоды", t);
                callback.onError(networkErrorMessage(t));
            }
        });
    }

    /**
     * Отменяет все незавершённые запросы.
     * Вызывайте из {@code onDestroy()} Activity/Fragment, чтобы callback
     * не сработал после уничтожения UI.
     */
    public void cancelPendingRequests() {
        synchronized (requestLock) {
            // FIX: инкремент requestId выполняется внутри того же synchronized-блока,
            // что и отмена запросов — устраняет окно гонки между двумя операциями.
            cancelPendingRequestsLocked();
            requestId++;
        }
    }

    private void cancelPendingRequestsLocked() {
        if (geocodeCall != null && !geocodeCall.isCanceled()) {
            geocodeCall.cancel();
        }
        if (weatherCall != null && !weatherCall.isCanceled()) {
            weatherCall.cancel();
        }
    }

    private boolean isLatestRequest(long id) {
        synchronized (requestLock) {
            return id == requestId;
        }
    }

    private static boolean isValidWeatherResponse(WeatherResponse response) {
        return response.getMain() != null
                && response.getWeather() != null
                && response.getWeather().length > 0
                && response.getWeather()[0] != null;
    }

    private static String networkErrorMessage(Throwable t) {
        if (t == null) {
            return "Ошибка сети";
        }
        String message = t.getMessage();
        if (message == null || message.isEmpty()) {
            return "Ошибка сети";
        }
        return "Ошибка сети: " + message;
    }
}