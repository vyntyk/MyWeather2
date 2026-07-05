package com.home.myweather.di

import androidx.fragment.app.Fragment
import com.home.myweather.ui.fragments.CitiesFragment
import com.home.myweather.ui.fragments.DayDetailFragment
import com.home.myweather.ui.fragments.ForecastFragment
import com.home.myweather.ui.fragments.MapFragment
import com.home.myweather.ui.fragments.NowFragment
import com.home.myweather.ui.fragments.SettingsFragment
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped

/**
 * Fragment bindings for Hilt.
 * Not strictly necessary for simple fragments, but useful for fragments with dependencies.
 */
@Module
@InstallIn(FragmentComponent::class)
object FragmentModule
