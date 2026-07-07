package com.home.myweather.di

import com.home.myweather.BuildConfig
import com.home.myweather.data.network.GeocodingApiService
import com.home.myweather.data.network.WeatherApiService
import com.home.myweather.data.repository.GeocodingRepository
import com.home.myweather.data.repository.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Application module providing dependencies that are shared across the entire app.
 * Available to all components including ViewModels.
 */
@Module
@InstallIn(SingletonComponent::class)
object SingletonModule {

    // HTTP Logging
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) Level.BODY else Level.NONE
        }
    }

    // OkHttpClient
    @Provides
    fun provideOkHttpClient(logInterceptor: HttpLoggingInterceptor): OkHttpClient {
        val cacheDir = File(System.getProperty("java.io.tmpdir"), "okhttp_cache")
        val cache = Cache(cacheDir, 50L * 1024 * 1024)

        return OkHttpClient.Builder()
            .connectTimeout(15L, TimeUnit.SECONDS)
            .readTimeout(15L, TimeUnit.SECONDS)
            .writeTimeout(15L, TimeUnit.SECONDS)
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

    // Retrofit for Weather API
    @WeatherApi
    @Provides
    fun provideWeatherRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Retrofit for Geocoding API
    @GeocodingApi
    @Provides
    fun provideGeocodingRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // API Services - singleton scoped
    @Singleton
    @WeatherApi
    @Provides
    fun provideWeatherApiService(@WeatherApi retrofit: Retrofit): WeatherApiService {
        return retrofit.create(WeatherApiService::class.java)
    }

    @Singleton
    @GeocodingApi
    @Provides
    fun provideGeocodingApiService(@GeocodingApi retrofit: Retrofit): GeocodingApiService {
        return retrofit.create(GeocodingApiService::class.java)
    }

    // Repositories - singleton scoped
    @Singleton
    @Provides
    fun provideGeocodingRepository(
        @GeocodingApi geoService: GeocodingApiService
    ): GeocodingRepository {
        return GeocodingRepository(geoService)
    }

    @Singleton
    @Provides
    fun provideWeatherRepository(
        @WeatherApi apiService: WeatherApiService,
        geocodingRepository: GeocodingRepository
    ): WeatherRepository {
        return WeatherRepository(apiService, geocodingRepository)
    }
}
