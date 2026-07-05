package com.home.myweather.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.home.myweather.data.model.GeoLocation
import com.home.myweather.data.model.WeatherResponse
import com.home.myweather.data.repository.WeatherRepository
import com.home.myweather.utils.TemperatureConverter
import com.home.myweather.utils.WeatherTileLayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for MapFragment.
 * Manages map state, weather display, and layer selection.
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<MapUiState>(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()
    
    private var currentLocation: Pair<Double, Double>? = null
    
    fun moveToLocation(lat: Double, lon: Double) {
        currentLocation = lat to lon
        _uiState.update {
            it.copy(
                centerLat = lat,
                centerLon = lon
            )
        }
    }
    
    fun fetchWeatherForPoint(lat: Double, lon: Double) {
        viewModelScope.launch {
            weatherRepository.fetchWeatherByCoords(lat, lon, object : WeatherRepository.WeatherCallback {
                override fun onSuccess(weather: WeatherResponse, geo: GeoLocation) {
                    _uiState.update {
                        it.copy(
                            weather = weather,
                            geo = geo,
                            showWeatherCard = true,
                            cityName = geo.name
                        )
                    }
                }
                
                override fun onError(message: String) {
                    // Silent error handling
                }
            })
        }
    }
    
    fun showWeatherCard(weather: WeatherResponse, cityName: String?) {
        _uiState.update {
            it.copy(
                weather = weather,
                cityName = cityName,
                showWeatherCard = true
            )
        }
    }
    
    fun hideWeatherCard() {
        _uiState.update { it.copy(showWeatherCard = false) }
    }
    
    fun switchLayer(newLayer: WeatherTileLayer.Layer?) {
        _uiState.update { it.copy(activeLayer = newLayer) }
    }
    
    fun updateLayerButtons() {
        // UI updates for button states
    }
    
    fun loadTemperatureMarkers(cities: List<Pair<Double, Double>>) {
        _uiState.update { it.copy(isLoadingMarkers = true) }
        
        viewModelScope.launch {
            // Simulate loading markers
            val results = mutableListOf<TempMarker>()
            var completedCount = 0
            
            for ((lat, lon) in cities) {
                weatherRepository.fetchWeatherByCoords(lat, lon, object : WeatherRepository.WeatherCallback {
                    override fun onSuccess(weather: WeatherResponse, geo: GeoLocation) {
                        weather.main?.temp?.let { temp ->
                            results.add(TempMarker(lat, lon, temp))
                        }
                        completedCount++
                        if (completedCount == cities.size) {
                            _uiState.update {
                                it.copy(
                                    temperatureMarkers = results,
                                    isLoadingMarkers = false
                                )
                            }
                        }
                    }
                    
                    override fun onError(message: String) {
                        completedCount++
                        if (completedCount == cities.size) {
                            _uiState.update {
                                it.copy(
                                    temperatureMarkers = results,
                                    isLoadingMarkers = false
                                )
                            }
                        }
                    }
                })
            }
        }
    }
    
    fun clearTemperatureMarkers() {
        _uiState.update { it.copy(temperatureMarkers = emptyList()) }
    }
}

/**
 * UI state for Map screen.
 */
data class MapUiState(
    val centerLat: Double = 55.751244,
    val centerLon: Double = 37.618423,
    val activeLayer: WeatherTileLayer.Layer? = null,
    val weather: WeatherResponse? = null,
    val geo: GeoLocation? = null,
    val cityName: String? = null,
    val showWeatherCard: Boolean = false,
    val isLoadingMarkers: Boolean = false,
    val temperatureMarkers: List<TempMarker> = emptyList()
)

/**
 * Data class for temperature marker on map.
 */
data class TempMarker(
    val lat: Double,
    val lon: Double,
    val temp: Double
)
