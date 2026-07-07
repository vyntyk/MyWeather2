package com.home.myweather.di

import android.app.Activity
import android.content.Context
import com.home.myweather.helpers.LocationHelper
import com.home.myweather.utils.AppPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext

/**
 * Main module providing Activity-scoped dependencies.
 * Note: Application-scoped dependencies (WeatherRepository, etc.) are in SingletonModule.
 */
@Module
@InstallIn(ActivityComponent::class)
object MainModule {

    // AppCompatActivity for LocationHelper
    @Provides
    fun provideAppCompatActivity(activity: Activity): androidx.appcompat.app.AppCompatActivity {
        return activity as androidx.appcompat.app.AppCompatActivity
    }

    // LocationHelper - available to Activities and Fragments
    @Provides
    fun provideLocationHelper(activity: androidx.appcompat.app.AppCompatActivity): LocationHelper {
        return LocationHelper(activity)
    }

    // AppPreferences - available to Activities and Fragments
    @Provides
    fun provideAppPreferences(@ActivityContext context: Context): AppPreferences {
        return AppPreferences(context.applicationContext)
    }
}
