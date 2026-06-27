package com.home.myweather.data.network;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.model.ForecastResponse;
import com.home.myweather.data.network.WeatherApiService;
import com.home.myweather.data.model.WeatherResponse;

/**
 * Retrofit service для двухшагового получения погоды:
 * 1) Geocoding API       → широта/долгота по названию города
 * 2) Current Weather 2.5 → текущая погода по координатам
 */
public interface WeatherApiService {

    /**
     * Шаг 1. Получение координат по названию города.
     * GET http://api.openweathermap.org/geo/1.0/direct
     *   ?q={city},{state},{country}&limit=1&appid={key}
     */
    @GET("geo/1.0/direct")
    Call<List<GeoLocation>> getCoordinates(
            @Query("q")     String cityQuery,   // "London" или "London,GB"
            @Query("limit") int    limit,        // обычно 1
            @Query("appid") String apiKey
    );

    /**
     * Шаг 2. Текущая погода по координатам (Current Weather API 2.5).
     * GET https://api.openweathermap.org/data/2.5/weather
     *   ?lat={lat}&lon={lon}&appid={key}&units=metric&lang=ru
     */
    @GET("data/2.5/weather")
    Call<WeatherResponse> getCurrentWeather(
            @Query("lat")   double lat,
            @Query("lon")   double lon,
            @Query("appid") String apiKey,
            @Query("units") String units,   // "metric" | "imperial" | "standard"
            @Query("lang")  String lang     // "ru", "en", …
    );

    /**
     * Прогноз на 5 дней с шагом 3 часа (Forecast API 2.5).
     * GET https://api.openweathermap.org/data/2.5/forecast
     *   ?lat={lat}&lon={lon}&appid={key}&units=metric&lang=ru
     */
    @GET("data/2.5/forecast")
    Call<ForecastResponse> get5DayForecast(
            @Query("lat")   double lat,
            @Query("lon")   double lon,
            @Query("appid") String apiKey,
            @Query("units") String units,
            @Query("lang")  String lang
    );
}
