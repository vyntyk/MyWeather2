package com.home.myweather.data.network;

import com.home.myweather.data.model.MetNoForecastResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/** Retrofit interface for the MET Norway Locationforecast API. */
public interface WeatherApiService {

    @GET("weatherapi/locationforecast/2.0/compact")
    Call<MetNoForecastResponse> getForecast(
            @Query("lat") String latitude,
            @Query("lon") String longitude
    );
}
