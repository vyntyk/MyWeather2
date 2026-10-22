package com.home.myweather.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.home.myweather.data.model.ForecastItem
import com.home.myweather.data.model.GeoLocation
import com.home.myweather.data.model.WeatherResponse
import com.home.myweather.data.repository.WeatherRepository
import com.home.myweather.data.repository.WeatherStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для NowFragment.
 * Управляет состоянием погоды и работает с WeatherRepository.
 */
@HiltViewModel
class NowViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    weatherStorage: WeatherStorage
) : ViewModel() {
    
    private val cachedGeo = weatherStorage.loadGeo()
    private val cachedWeather = weatherStorage.loadWeather()
    private val _uiState = MutableLiveData(
        if (cachedWeather != null && cachedGeo != null) {
            WeatherUiState(weather = cachedWeather, geo = cachedGeo)
        } else {
            WeatherUiState()
        }
    )
    @JvmField
    val uiState: LiveData<WeatherUiState> = _uiState
    
    private val _hourlyForecast = MutableLiveData(
        weatherStorage.loadHourly()
            .filter { it.timestamp + 3600L > System.currentTimeMillis() / 1000L }
            .take(24)
    )
    @JvmField
    val hourlyForecast: LiveData<List<ForecastItem>> = _hourlyForecast
    
    @JvmField
    var lastGeo: GeoLocation? = cachedGeo
    
    fun fetchWeatherByCoords(lat: Double, lon: Double) {
        _uiState.value = (_uiState.value ?: WeatherUiState()).copy(isLoading = true, error = null)
        
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
                    val current = _uiState.value ?: WeatherUiState()
                    _uiState.postValue(current.copy(isLoading = false, error = message))
                }
            })
        }
    }
    
    fun fetchWeatherByCity(city: String) {
        _uiState.value = (_uiState.value ?: WeatherUiState()).copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            weatherRepository.fetchWeather(city, null, object : WeatherRepository.WeatherCallback {
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
                    val current = _uiState.value ?: WeatherUiState()
                    _uiState.postValue(current.copy(isLoading = false, error = message))
                }
            })
        }
    }
    
    fun loadForecast(lat: Double, lon: Double) {
        viewModelScope.launch {
            weatherRepository.fetchForecast(lat, lon, object : WeatherRepository.ForecastCallback {
                override fun onSuccess(forecast: com.home.myweather.data.model.ForecastResponse) {
                    val items = forecast.list ?: emptyList()
                    // Синхронизируем прогноз с текущим временем: берём блоки, начиная
                    // с текущего часа (блок, в котором мы сейчас находимся, включаем),
                    // прошедшие часы отбрасываем — они «уходят влево».
                    val nowSec = System.currentTimeMillis() / 1000L
                    val fromNow = items.filter { it.timestamp + 3600L > nowSec }
                    val first24Hours = if (fromNow.size > 24) fromNow.subList(0, 24) else fromNow
                    _hourlyForecast.postValue(first24Hours)
                }
                
                override fun onError(message: String) {
                    // Тихая обработка ошибки
                }
            })
        }
    }
    
    fun refresh() {
        lastGeo?.let { geo ->
            fetchWeatherByCoords(geo.lat, geo.lon)
        }
    }
    
    fun clearRequests() {
        weatherRepository.cancelPendingRequests()
    }
}

/**
 * Состояние UI для экрана погоды.
 */
data class WeatherUiState(
    @JvmField val weather: WeatherResponse? = null,
    @JvmField val geo: GeoLocation? = null,
    @JvmField val isLoading: Boolean = false,
    @JvmField val error: String? = null
)
