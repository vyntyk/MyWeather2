package com.home.myweather;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * Retrofit-клиент.
 *
 * Geocoding API и Current Weather API имеют одинаковый хост (api.openweathermap.org),
 * поэтому используем один клиент с базовым URL "https://api.openweathermap.org/".
 * Пути в аннотациях @GET уже содержат нужные prefixes (geo/1.0/... и data/4.0/...).
 */
public class RetrofitClient {

    private static final String BASE_URL = "https://api.openweathermap.org/";
    private static final int TIMEOUT_SECONDS = 15;

    private static RetrofitClient instance;
    private final WeatherApiService apiService;

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

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(WeatherApiService.class);
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public WeatherApiService getApiService() {
        return apiService;
    }
}
