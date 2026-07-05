package com.home.myweather.di

import androidx.lifecycle.ViewModel
import com.home.myweather.ui.viewmodel.ForecastViewModel
import com.home.myweather.ui.viewmodel.MapViewModel
import com.home.myweather.ui.viewmodel.NowViewModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.multibindings.IntoMap

/**
 * ViewModel bindings for Hilt.
 * Uses ViewModelMarker to allow multiple ViewModel bindings.
 */
@Suppress("unused")
@Module
@InstallIn(ViewModelComponent::class)
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(NowViewModel::class)
    @ViewModelScoped
    abstract fun bindNowViewModel(viewModel: NowViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ForecastViewModel::class)
    @ViewModelScoped
    abstract fun bindForecastViewModel(viewModel: ForecastViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MapViewModel::class)
    @ViewModelScoped
    abstract fun bindMapViewModel(viewModel: MapViewModel): ViewModel
}
