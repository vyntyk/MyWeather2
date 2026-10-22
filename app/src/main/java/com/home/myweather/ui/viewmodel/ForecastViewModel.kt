package com.home.myweather.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.home.myweather.data.model.DailyData
import com.home.myweather.data.model.ForecastResponse
import com.home.myweather.data.repository.WeatherRepository
import com.home.myweather.data.repository.WeatherStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для ForecastFragment.
 * Управляет состоянием прогноза на 5 дней.
 */
@HiltViewModel
class ForecastViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val weatherStorage: WeatherStorage
) : ViewModel() {
    
    private val _uiState = MutableLiveData<ForecastUiState>(ForecastUiState())
    val uiState: LiveData<ForecastUiState> = _uiState
    
    private var currentGeo: com.home.myweather.data.model.GeoLocation? = null
    private val forecastCache = mutableMapOf<String, List<DailyData>>()
    
    fun setGeoLocation(geo: com.home.myweather.data.model.GeoLocation) {
        currentGeo = geo
        loadForecast(geo.lat, geo.lon)
    }
    
    fun loadForecast(lat: Double, lon: Double) {
        val locationKey = "$lat,$lon"
        
        // Проверяем ForecastCache сначала (из NowFragment)
        val cachedItems = com.home.myweather.data.repository.ForecastCache.get(lat, lon)
        if (cachedItems != null && cachedItems.isNotEmpty()) {
            val grouped = com.home.myweather.utils.ForecastGrouper.groupByDay(cachedItems)
            _uiState.postValue(ForecastUiState(days = grouped, isLoading = false))
            return
        }
        
        var hasPersistentCache = false
        val savedGeo = weatherStorage.loadForecastGeo()
        if (savedGeo != null &&
            kotlin.math.abs(savedGeo.lat - lat) <= 0.01 &&
            kotlin.math.abs(savedGeo.lon - lon) <= 0.01
        ) {
            val savedItems = weatherStorage.loadHourly()
            if (savedItems.isNotEmpty()) {
                val grouped = com.home.myweather.utils.ForecastGrouper.groupByDay(savedItems)
                forecastCache[locationKey] = grouped
                _uiState.value = ForecastUiState(days = grouped, isLoading = false)
                hasPersistentCache = true
            }
        }

        // Проверяем локальный кэш
        val cached = forecastCache[locationKey]
        if (cached != null && !hasPersistentCache) {
            _uiState.postValue(ForecastUiState(days = cached, isLoading = false))
            return
        }
        
        if (!hasPersistentCache) {
            _uiState.postValue(ForecastUiState(isLoading = true, error = null))
        }
        
        viewModelScope.launch {
            weatherRepository.fetchForecast(lat, lon, object : WeatherRepository.ForecastCallback {
                override fun onSuccess(forecast: ForecastResponse) {
                    val items = forecast.list ?: emptyList()
                    val grouped = com.home.myweather.utils.ForecastGrouper.groupByDay(items)
                    
                    // Кэшируем результат
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
                    val current = _uiState.value ?: ForecastUiState()
                    _uiState.postValue(current.copy(isLoading = false, error = message))
                }
            })
        }
    }
    
    fun refresh() {
        currentGeo?.let { geo ->
            loadForecast(geo.lat, geo.lon)
        }
    }
    
    fun getCachedDays(): List<DailyData> {
        currentGeo?.let { geo ->
            val locationKey = "${geo.lat},${geo.lon}"
            return forecastCache[locationKey] ?: emptyList()
        }
        return emptyList()
    }
    
    override fun onCleared() {
        super.onCleared()
        forecastCache.clear()
    }
}

/**
 * Состояние UI для экрана прогноза.
 */
data class ForecastUiState(
    val days: List<DailyData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
