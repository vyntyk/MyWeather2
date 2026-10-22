package com.home.myweather.data.repository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.model.OpenMeteoGeoResponse;
import com.home.myweather.data.network.GeocodingApiService;
import javax.inject.Inject;

import android.util.Log;

/**
 * Шаг 1: получение координат по названию города через Open-Meteo Geocoding API.
 * API-ключ не нужен.
 */
public class GeocodingRepository {
    private static final String TAG = "GeocodingRepository";
    
    private final GeocodingApiService geoService;

    public GeocodingRepository(GeocodingApiService geoService) {
        this.geoService = geoService;
    }

    interface Callback2 {
        void onSuccess(GeoLocation geo);
        void onError(String message);
    }

    private Call<OpenMeteoGeoResponse> pendingCall;

    /**
     * @param query  название города, возможно "City,Country" — берётся только часть до запятой
     * @param requestId идентификатор запроса для отмены устаревших ответов
     */
    public void fetch(String query, long requestId, Callback2 callback) {
        // Open-Meteo Geocoding принимает только имя города без кода страны
        String cityName = query.contains(",") ? query.split(",")[0].trim() : query.trim();

        Log.d(TAG, "fetch: cityName=" + cityName + ", requestId=" + requestId);

        pendingCall = geoService.search(cityName, 1, "ru", "json");
        pendingCall.enqueue(new Callback<OpenMeteoGeoResponse>() {
            @Override
            public void onResponse(Call<OpenMeteoGeoResponse> call,
                                   Response<OpenMeteoGeoResponse> response) {
                if (call.isCanceled()) return;
                OpenMeteoGeoResponse body = response.body();
                Log.d(TAG, "onResponse: code=" + response.code() + ", body=" + (body != null ? "present" : "null")
                        + ", results=" + (body != null && body.results != null ? body.results.size() : "null"));
                if (!response.isSuccessful()
                        || body == null
                        || body.results == null
                        || body.results.isEmpty()) {
                    String errorMsg = "Город не найден: " + cityName;
                    Log.w(TAG, errorMsg + " (code=" + response.code() + ")");
                    callback.onError(errorMsg);
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
                if (!call.isCanceled()) {
                    String errorMsg = networkError(t);
                    Log.w(TAG, "onFailure: " + errorMsg, t);
                    callback.onError(errorMsg);
                }
            }
        });
    }

    public void cancel() {
        if (pendingCall != null && !pendingCall.isCanceled()) pendingCall.cancel();
    }

    private static String networkError(Throwable t) {
        String m = t != null ? t.getMessage() : null;
        return "Ошибка сети" + (m != null && !m.isEmpty() ? ": " + m : "");
    }
}
