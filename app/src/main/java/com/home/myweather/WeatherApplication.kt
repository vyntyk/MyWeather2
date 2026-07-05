package com.home.myweather

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class with Hilt dependency injection.
 * Declared in AndroidManifest.xml
 */
@HiltAndroidApp
class WeatherApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("WeatherApp", "Application onCreate called")
    }
}
