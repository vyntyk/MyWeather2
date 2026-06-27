package com.home.myweather.data.repository;

import android.util.Log;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.home.myweather.data.network.WeatherApiService;
import com.home.myweather.data.repository.GeocodingRepository;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.network.RetrofitClient;
import com.home.myweather.data.repository.WeatherRepository;

/**
 * Шаг 1: Geocoding API → получает координаты по названию города.
 * Используется только из WeatherRepository.
 */
class GeocodingRepository {

    private static final String TAG = "GeocodingRepository";

    interface Callback2 {
        void onSuccess(GeoLocation geo);
        void onError(String message);
    }

    private final WeatherApiService apiService =
            RetrofitClient.getInstance().getApiService();

    private Call<List<GeoLocation>> pendingCall;

    void fetch(String query, String apiKey, long requestId,
               GeocodingRepository.Callback2 callback) {
        pendingCall = apiService.getCoordinates(query, 1, apiKey);
        pendingCall.enqueue(new Callback<List<GeoLocation>>() {
            @Override
            public void onResponse(Call<List<GeoLocation>> call,
                                   Response<List<GeoLocation>> response) {
                if (call.isCanceled()) return;
                List<GeoLocation> list = response.body();
                if (!response.isSuccessful() || list == null || list.isEmpty()) {
                    callback.onError("Город не найден: " + query);
                    return;
                }
                GeoLocation geo = list.get(0);
                if (geo == null) { callback.onError("Город не найден: " + query); return; }
                Log.d(TAG, "Координаты: " + geo.lat + ", " + geo.lon);
                callback.onSuccess(geo);
            }
            @Override
            public void onFailure(Call<List<GeoLocation>> call, Throwable t) {
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
