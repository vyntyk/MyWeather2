package com.home.myweather.di

import com.home.myweather.BuildConfig
import com.home.myweather.data.network.GeocodingApiService
import com.home.myweather.data.network.WeatherApiService
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Qualifiers for distinguishing between different Retrofit instances.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WeatherApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeocodingApi

/**
 * Network module providing OkHttpClient, Retrofit, and API services.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TIMEOUT_SECONDS = 15L
    private const val CACHE_SIZE_MB = 50L

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) Level.BODY else Level.NONE
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(logInterceptor: HttpLoggingInterceptor): OkHttpClient {
        val cacheDir = File(System.getProperty("java.io.tmpdir"), "okhttp_cache")
        val cache = Cache(cacheDir, CACHE_SIZE_MB * 1024 * 1024)

        return OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .cache(cache)
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("User-Agent", "MyWeather/2.1")
                    .header("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logInterceptor)
            .build()
    }

    @WeatherApi
    @Provides
    @Singleton
    fun provideWeatherRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @GeocodingApi
    @Provides
    @Singleton
    fun provideGeocodingRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @WeatherApi
    @Provides
    @Singleton
    fun provideWeatherApiService(@WeatherApi retrofit: Retrofit): WeatherApiService {
        return retrofit.create(WeatherApiService::class.java)
    }

    @GeocodingApi
    @Provides
    @Singleton
    fun provideGeocodingApiService(@GeocodingApi retrofit: Retrofit): GeocodingApiService {
        return retrofit.create(GeocodingApiService::class.java)
    }
}
