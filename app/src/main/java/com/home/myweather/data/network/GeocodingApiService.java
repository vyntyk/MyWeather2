package com.home.myweather.data.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import com.home.myweather.data.model.OpenMeteoGeoResponse;

/** Retrofit-интерфейс для Open-Meteo Geocoding API. */
public interface GeocodingApiService {

    /**
     * GET https://geocoding-api.open-meteo.com/v1/search
     *   ?name={city}&count=1&language=ru&format=json
     * API-ключ не нужен.
     */
    @GET("v1/search")
    Call<OpenMeteoGeoResponse> search(
            @Query("name")     String name,
            @Query("count")    int    count,
            @Query("language") String language,
            @Query("format")   String format
    );
}
