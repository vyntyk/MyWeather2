package com.home.myweather.data.repository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.model.OpenMeteoGeoResponse;
import com.home.myweather.data.network.GeocodingApiService;
import com.home.myweather.data.network.RetrofitClient;

/**
 * Шаг 1: получение координат по названию города через Open-Meteo Geocoding API.
 * API-ключ не нужен.
 */
class GeocodingRepository {

    interface Callback2 {
        void onSuccess(GeoLocation geo);
        void onError(String message);
    }

    private final GeocodingApiService geoService =
            RetrofitClient.getInstance().getGeocodingService();

    private Call<OpenMeteoGeoResponse> pendingCall;

    /**
     * @param query  название города, возможно "City,Country" — берётся только часть до запятой
     * @param requestId идентификатор запроса для отмены устаревших ответов
     */
    void fetch(String query, long requestId, Callback2 callback) {
        // Open-Meteo Geocoding принимает только имя города без кода страны
        String cityName = query.contains(",") ? query.split(",")[0].trim() : query.trim();

        pendingCall = geoService.search(cityName, 1, "ru", "json");
        pendingCall.enqueue(new Callback<OpenMeteoGeoResponse>() {
            @Override
            public void onResponse(Call<OpenMeteoGeoResponse> call,
                                   Response<OpenMeteoGeoResponse> response) {
                if (call.isCanceled()) return;
                OpenMeteoGeoResponse body = response.body();
                if (!response.isSuccessful()
                        || body == null
                        || body.results == null
                        || body.results.isEmpty()) {
                    callback.onError("Город не найден: " + cityName);
                    return;
                }
                OpenMeteoGeoResponse.Result r = body.results.get(0);
                GeoLocation geo = new GeoLocation();
                geo.lat     = r.latitude;
                geo.lon     = r.longitude;
                geo.name    = r.name;
                geo.country = r.countryCode;
                geo.state   = r.admin1;
                callback.onSuccess(geo);
            }

            @Override
            public void onFailure(Call<OpenMeteoGeoResponse> call, Throwable t) {
                if (!call.isCanceled()) callback.onError(networkError(t));
            }
        });
    }

    void cancel() {
        if (pendingCall != null && !pendingCall.isCanceled()) pendingCall.cancel();
    }

    private static String networkError(Throwable t) {
        String m = t != null ? t.getMessage() : null;
        return "Ошибка сети" + (m != null && !m.isEmpty() ? ": " + m : "");
    }
}
