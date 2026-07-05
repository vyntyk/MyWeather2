package com.home.myweather.di

import android.content.Context
import com.home.myweather.data.network.GeocodingApiService
import com.home.myweather.data.network.WeatherApiService
import com.home.myweather.data.repository.GeocodingRepository
import com.home.myweather.data.repository.WeatherRepository
import com.home.myweather.utils.AppPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Application-level dependency bindings.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppPreferences(@ApplicationContext context: Context): AppPreferences {
        return AppPreferences(context)
    }

    @Provides
    @Singleton
    fun provideGeocodingRepository(
        @GeocodingApi geoService: GeocodingApiService
    ): GeocodingRepository {
        return GeocodingRepository(geoService)
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(
        @WeatherApi apiService: WeatherApiService,
        geocodingRepository: GeocodingRepository
    ): WeatherRepository {
        return WeatherRepository(apiService, geocodingRepository)
    }
}
