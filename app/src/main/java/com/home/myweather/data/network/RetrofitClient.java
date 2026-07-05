package com.home.myweather.data.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;
import com.home.myweather.BuildConfig;

/**
 * Singleton Retrofit-клиент для Open-Meteo.
 *
 * Два отдельных Retrofit-экземпляра с разными base URL:
 *  - api.open-meteo.com        → прогноз погоды
 *  - geocoding-api.open-meteo.com → геокодирование
 *
 * Оба используют один и тот же OkHttpClient.
 * API-ключ не требуется.
 */
public class RetrofitClient {

    private static final String WEATHER_BASE_URL = "https://api.open-meteo.com/";
    private static final String GEO_BASE_URL     = "https://geocoding-api.open-meteo.com/";
    private static final int    TIMEOUT_SECONDS  = 15;

    private final WeatherApiService    weatherService;
    private final GeocodingApiService  geoService;

    private RetrofitClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build();

        weatherService = new Retrofit.Builder()
                .baseUrl(WEATHER_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherApiService.class);

        geoService = new Retrofit.Builder()
                .baseUrl(GEO_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GeocodingApiService.class);
    }

    /** Holder-идиома Singleton — потокобезопасна без synchronized. */
    private static final class Holder {
        static final RetrofitClient INSTANCE = new RetrofitClient();
    }

    public static RetrofitClient getInstance() {
        return Holder.INSTANCE;
    }

    public static WeatherApiService getWeatherApiService() {
        return getInstance().weatherService;
    }

    public static GeocodingApiService getGeocodingApiService() {
        return getInstance().geoService;
    }

    public WeatherApiService   getApiService()        { return weatherService; }
    public GeocodingApiService getGeocodingService()  { return geoService; }
}
