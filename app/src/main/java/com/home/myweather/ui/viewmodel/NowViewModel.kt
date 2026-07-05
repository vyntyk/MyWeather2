package com.home.myweather.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.home.myweather.data.model.ForecastResponse
import com.home.myweather.data.model.GeoLocation
import com.home.myweather.data.model.WeatherResponse
import com.home.myweather.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for NowFragment.
 * Manages weather state and coordinates with WeatherRepository.
 */
@HiltViewModel
class NowViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()
    
    var lastGeo: GeoLocation? = null
        private set
    
    fun fetchWeatherByCoords(lat: Double, lon: Double) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            weatherRepository.fetchWeatherByCoords(lat, lon, object : WeatherRepository.WeatherCallback {
                override fun onSuccess(weather: WeatherResponse, geo: GeoLocation) {
                    lastGeo = geo
                    _uiState.update {
                        it.copy(
                            weather = weather,
                            geo = geo,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                
                override fun onError(message: String) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = message
                        )
                    }
                }
            })
        }
    }
    
    fun fetchWeatherByCity(city: String, country: String? = null) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            weatherRepository.fetchWeather(city, country, object : WeatherRepository.WeatherCallback {
                override fun onSuccess(weather: WeatherResponse, geo: GeoLocation) {
                    lastGeo = geo
                    _uiState.update {
                        it.copy(
                            weather = weather,
                            geo = geo,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                
                override fun onError(message: String) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = message
                        )
                    }
                }
            })
        }
    }
    
    fun loadForecast(lat: Double, lon: Double) {
        viewModelScope.launch {
            weatherRepository.fetchForecast(lat, lon, object : WeatherRepository.ForecastCallback {
                override fun onSuccess(forecast: com.home.myweather.data.model.ForecastResponse) {
                    // Forecast is cached, no need to update UI state
                }
                
                override fun onError(message: String) {
                    // Silent error handling for forecast
                }
            })
        }
    }
    
    fun refresh() {
        lastGeo?.let { geo ->
            fetchWeatherByCoords(geo.lat, geo.lon)
        }
    }
}

/**
 * UI state for Weather screen.
 */
data class WeatherUiState(
    val weather: WeatherResponse? = null,
    val geo: GeoLocation? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
