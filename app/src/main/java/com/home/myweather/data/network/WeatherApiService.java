package com.home.myweather.data.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import com.home.myweather.data.model.OpenMeteoForecastResponse;
import com.home.myweather.data.model.WeatherResponse;

/**
 * Retrofit-интерфейс для Open-Meteo Forecast API.
 * API-ключ не нужен.
 *
 * GET https://api.open-meteo.com/v1/forecast
 *   ?latitude=55.75&longitude=37.61
 *   &current=temperature_2m,...
 *   &hourly=temperature_2m,...
 *   &daily=weather_code,...
 *   &wind_speed_unit=ms
 *   &timezone=auto
 *   &forecast_days=7
 */
public interface WeatherApiService {

    @GET("v1/forecast")
    Call<OpenMeteoForecastResponse> getForecast(
            @Query("latitude")        double latitude,
            @Query("longitude")       double longitude,
            @Query("current")         String current,
            @Query("hourly")          String hourly,
            @Query("daily")           String daily,
            @Query("wind_speed_unit") String windSpeedUnit,
            @Query("timezone")        String timezone,
            @Query("forecast_days")   int    forecastDays
    );

    /**
     * GET https://api.openweathermap.org/data/2.5/weather
     *   ?lat={lat}&lon={lon}&appid={apiKey}&units={units}&lang={lang}
     */
    @GET("data/2.5/weather")
    Call<WeatherResponse> getCurrentWeather(
            @Query("lat")      double latitude,
            @Query("lon")      double longitude,
            @Query("appid")    String apiKey,
            @Query("units")    String units,
            @Query("lang")     String lang
    );
}
