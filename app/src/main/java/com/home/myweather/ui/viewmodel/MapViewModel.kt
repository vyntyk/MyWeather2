package com.home.myweather.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.home.myweather.data.model.GeoLocation
import com.home.myweather.data.model.WeatherResponse
import com.home.myweather.data.repository.WeatherRepository
import com.home.myweather.utils.WeatherTileLayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * ViewModel for MapFragment.
 * Manages map state, weather display, and layer selection.
 *
 * Все изменения состояния проходят через [updateState]: меняется только та часть,
 * которую затрагивает операция, остальные поля (центр карты, слой, карточка) сохраняются.
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    /**
     * Актуальное состояние. Хранится отдельно от LiveData, потому что postValue
     * применяется отложенно: чтение из LiveData сразу после записи вернуло бы старое значение.
     */
    private var state = MapUiState()
    private val _uiState = MutableLiveData<MapUiState>(state)
    val uiState: LiveData<MapUiState> = _uiState

    private var currentLocation: Pair<Double, Double>? = null

    fun moveToLocation(lat: Double, lon: Double) {
        currentLocation = lat to lon
        updateState { copy(centerLat = lat, centerLon = lon) }
    }

    fun fetchWeatherForPoint(lat: Double, lon: Double) {
        viewModelScope.launch {
            weatherRepository.fetchWeatherByCoords(lat, lon, object : WeatherRepository.WeatherCallback {
                override fun onSuccess(weather: WeatherResponse, geo: GeoLocation) {
                    updateState {
                        copy(
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
        updateState {
            copy(
                weather = weather,
                cityName = cityName,
                showWeatherCard = true
            )
        }
    }

    fun hideWeatherCard() {
        updateState { copy(showWeatherCard = false) }
    }

    fun switchLayer(newLayer: WeatherTileLayer.Layer?) {
        updateState { copy(activeLayer = newLayer) }
    }

    fun updateLayerButtons() {
        // UI updates for button states
    }

    /**
     * Загружает температуру для набора точек и выводит маркеры на карту.
     *
     * Запросы идут параллельно через fetchCurrentWeatherMarker, который не отменяет
     * друг друга. Ошибка отдельной точки пропускает её маркер. Пустой список
     * сразу завершает загрузку.
     */
    fun loadTemperatureMarkers(cities: List<Pair<Double, Double>>) {
        if (cities.isEmpty()) {
            updateState { copy(temperatureMarkers = emptyList(), isLoadingMarkers = false) }
            return
        }

        updateState { copy(isLoadingMarkers = true) }

        viewModelScope.launch {
            val results: MutableList<TempMarker> = Collections.synchronizedList(mutableListOf())
            val remaining = AtomicInteger(cities.size)

            fun finishOne() {
                if (remaining.decrementAndGet() == 0) {
                    val markers = results.toList()
                    updateState { copy(temperatureMarkers = markers, isLoadingMarkers = false) }
                }
            }

            for ((lat, lon) in cities) {
                weatherRepository.fetchCurrentWeatherMarker(lat, lon, object : WeatherRepository.WeatherCallback {
                    override fun onSuccess(weather: WeatherResponse, geo: GeoLocation) {
                        weather.main?.temp?.let { temp ->
                            results.add(TempMarker(lat, lon, temp))
                        }
                        finishOne()
                    }

                    override fun onError(message: String) {
                        finishOne()
                    }
                })
            }
        }
    }

    fun clearTemperatureMarkers() {
        updateState { copy(temperatureMarkers = emptyList()) }
    }

    /**
     * Единственная точка записи состояния: применяет изменение к актуальному состоянию
     * и публикует его в LiveData.
     */
    @Synchronized
    private fun updateState(change: MapUiState.() -> MapUiState) {
        state = state.change()
        _uiState.postValue(state)
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
