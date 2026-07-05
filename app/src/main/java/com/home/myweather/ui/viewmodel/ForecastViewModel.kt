package com.home.myweather.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.home.myweather.data.model.GeoLocation
import com.home.myweather.data.model.ForecastResponse
import com.home.myweather.data.repository.WeatherRepository
import com.home.myweather.utils.ForecastGrouper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for ForecastFragment.
 * Manages 5-day forecast state.
 */
@HiltViewModel
class ForecastViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {
    
    private val _uiState = MutableLiveData<ForecastUiState>(ForecastUiState())
    val uiState: LiveData<ForecastUiState> = _uiState
    
    private var currentGeo: GeoLocation? = null
    private val forecastCache = mutableMapOf<String, List<com.home.myweather.data.model.DailyData>>()
    
    fun setGeoLocation(geo: GeoLocation) {
        currentGeo = geo
        loadForecast(geo.lat, geo.lon)
    }
    
    fun loadForecast(lat: Double, lon: Double) {
        val locationKey = "$lat,$lon"
        
        // Check ForecastCache first (from NowFragment)
        val cachedItems = com.home.myweather.data.repository.ForecastCache.get(lat, lon)
        if (cachedItems != null && cachedItems.isNotEmpty()) {
            val grouped = com.home.myweather.utils.ForecastGrouper.groupByDay(cachedItems)
            _uiState.postValue(ForecastUiState(days = grouped, isLoading = false))
            return
        }
        
        // Check local cache
        val cached = forecastCache[locationKey]
        if (cached != null) {
            _uiState.postValue(ForecastUiState(days = cached, isLoading = false))
            return
        }
        
        _uiState.postValue(ForecastUiState(isLoading = true, error = null))
        
        viewModelScope.launch {
            weatherRepository.fetchForecast(lat, lon, object : WeatherRepository.ForecastCallback {
                override fun onSuccess(forecast: ForecastResponse) {
                    val items = forecast.list ?: emptyList()
                    val grouped = com.home.myweather.utils.ForecastGrouper.groupByDay(items)
                    
                    // Cache the result
                    forecastCache[locationKey] = grouped
                    
                    _uiState.postValue(
                        ForecastUiState(
                            days = grouped,
                            isLoading = false,
                            error = null
                        )
                    )
                }
                
                override fun onError(message: String) {
                    _uiState.postValue(
                        ForecastUiState(
                            isLoading = false,
                            error = message
                        )
                    )
                }
            })
        }
    }
    
    fun refresh() {
        currentGeo?.let { geo ->
            loadForecast(geo.lat, geo.lon)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        forecastCache.clear()
    }
}

/**
 * UI state for Forecast screen.
 */
data class ForecastUiState(
    val days: List<com.home.myweather.data.model.DailyData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
