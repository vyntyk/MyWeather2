package com.home.myweather.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.home.myweather.data.model.ForecastItem
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
    
    private val _uiState = MutableLiveData<WeatherUiState>(WeatherUiState())
    
    @get:JvmName("getUiState")
    val uiState: LiveData<WeatherUiState> = _uiState
    
    private val _hourlyForecast = MutableLiveData<List<ForecastItem>>()
    
    @get:JvmName("getHourlyForecastData")
    val hourlyForecastLiveData: LiveData<List<ForecastItem>> = _hourlyForecast
    
    @JvmField var lastGeo: GeoLocation? = null
    
    private var lastForecastLat = Double.NaN
    private var lastForecastLon = Double.NaN
    
    fun getUiStateLiveData(): LiveData<WeatherUiState> = uiState
    
    @JvmName("getHourlyForecast")
    fun getHourlyForecast(): LiveData<List<ForecastItem>> = hourlyForecastLiveData
    
    fun fetchWeatherByCoords(lat: Double, lon: Double) {
        _uiState.postValue(WeatherUiState(isLoading = true, error = null))
        
        viewModelScope.launch {
            weatherRepository.fetchWeatherByCoords(lat, lon, object : WeatherRepository.WeatherCallback {
                override fun onSuccess(weather: WeatherResponse, geo: GeoLocation) {
                    lastGeo = geo
                    _uiState.postValue(
                        WeatherUiState(
                            weather = weather,
                            geo = geo,
                            isLoading = false,
                            error = null
                        )
                    )
                }
                
                override fun onError(message: String) {
                    _uiState.postValue(
                        WeatherUiState(
                            isLoading = false,
                            error = message
                        )
                    )
                }
            })
        }
    }
    
    @JvmOverloads
    fun fetchWeatherByCity(city: String, country: String? = null) {
        _uiState.postValue(WeatherUiState(isLoading = true, error = null))
        
        viewModelScope.launch {
            weatherRepository.fetchWeather(city, country, object : WeatherRepository.WeatherCallback {
                override fun onSuccess(weather: WeatherResponse, geo: GeoLocation) {
                    lastGeo = geo
                    _uiState.postValue(
                        WeatherUiState(
                            weather = weather,
                            geo = geo,
                            isLoading = false,
                            error = null
                        )
                    )
                }
                
                override fun onError(message: String) {
                    _uiState.postValue(
                        WeatherUiState(
                            isLoading = false,
                            error = message
                        )
                    )
                }
            })
        }
    }
    
    fun loadForecast(lat: Double, lon: Double) {
        // Avoid duplicate requests with same coordinates
        if (lastForecastLat.isNaN().not() && lastForecastLon.isNaN().not() &&
            Math.abs(lat - lastForecastLat) < 0.001 && Math.abs(lon - lastForecastLon) < 0.001) {
            return
        }
        lastForecastLat = lat
        lastForecastLon = lon
        
        viewModelScope.launch {
            weatherRepository.fetchForecast(lat, lon, object : WeatherRepository.ForecastCallback {
                override fun onSuccess(forecast: ForecastResponse) {
                    val items = forecast.list ?: emptyList()
                    _hourlyForecast.postValue(items)
                    // Update UI state to trigger observation
                    _uiState.postValue(
                        WeatherUiState(
                            weather = _uiState.value?.weather,
                            geo = _uiState.value?.geo,
                            isLoading = false,
                            error = null
                        )
                    )
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
    
    fun getWeatherRepository(): WeatherRepository = weatherRepository
    
    fun clearRequests() {
        weatherRepository.cancelPendingRequests()
    }
}

/**
 * UI state for Weather screen.
 */
class WeatherUiState(
    @JvmField var weather: WeatherResponse? = null,
    @JvmField var geo: GeoLocation? = null,
    @JvmField var isLoading: Boolean = false,
    @JvmField var error: String? = null
)
