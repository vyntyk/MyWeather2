package com.home.myweather.di

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.BINARY

/**
 * Qualifiers for distinguishing between different Retrofit instances.
 */
@Qualifier
@Retention(BINARY)
annotation class WeatherApi

@Qualifier
@Retention(BINARY)
annotation class GeocodingApi
