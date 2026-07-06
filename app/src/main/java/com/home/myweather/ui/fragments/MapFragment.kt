package com.home.myweather.ui.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.home.myweather.MainActivity
import com.home.myweather.R
import com.home.myweather.data.model.GeoLocation
import com.home.myweather.data.model.WeatherResponse
import com.home.myweather.data.repository.WeatherRepository
import com.home.myweather.data.repository.WeatherStorage
import com.home.myweather.utils.AppPreferences
import com.home.myweather.utils.TemperatureConverter
import com.home.myweather.utils.WeatherIcon
import com.home.myweather.utils.WeatherTileLayer
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.sources.RasterSource
import org.maplibre.android.style.sources.TileSet
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback {

    companion object {
        private const val DEFAULT_LAT = 55.751244
        private const val DEFAULT_LON = 37.618423
        private const val DEFAULT_ZOOM = 5.0
        private const val OSM_STYLE_URL =
            "https://basemaps.cartocdn.com/gl/voyager-gl-style/style.json"
        private const val TEMP_ICON_ID = "temp_label_icon"
        private const val STATE_LAYER = "active_layer"
        private const val STATE_LAT = "last_lat"
        private const val STATE_LON = "last_lon"

        private val CITIES = arrayOf(
            doubleArrayOf(55.751244, 37.618423),
            doubleArrayOf(59.939095, 30.315868),
            doubleArrayOf(56.838011, 60.597474),
            doubleArrayOf(43.115542, 131.885495),
            doubleArrayOf(51.660781, 39.200296),
            doubleArrayOf(51.768205, 55.096903),
            doubleArrayOf(54.989342, 82.904632),
            doubleArrayOf(53.195873, 50.100193),
            doubleArrayOf(48.708048, 44.513916),
            doubleArrayOf(55.030199, 82.920430)
        )
    }

    private var mapView: MapView? = null
    private var mapLibreMap: MapLibreMap? = null
    private var activeLayer: WeatherTileLayer.Layer? = null
    private var symbolManager: SymbolManager? = null
    private val tempSymbols = mutableListOf<Symbol>()
    private var executor = Executors.newFixedThreadPool(4)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isViewCreated = false

    private var btnNone: TextView? = null
    private var btnTemp: TextView? = null
    private var btnPrecip: TextView? = null
    private var btnClouds: TextView? = null
    private var btnWind: TextView? = null

    private var cardWeatherInfo: CardView? = null
    private var mapWeatherIcon: ImageView? = null
    private var mapCityName: TextView? = null
    private var mapTemp: TextView? = null
    private var mapDesc: TextView? = null
    private var mapWind: TextView? = null
    private var mapHumidity: TextView? = null

    private var cardLegend: CardView? = null
    private var legendContainer: LinearLayout? = null

    private var lastLat = DEFAULT_LAT
    private var lastLon = DEFAULT_LON
    private var cachedWeather: WeatherResponse? = null
    private var cachedCityName: String? = null

    @Inject
    lateinit var weatherRepository: WeatherRepository

    private lateinit var weatherStorage: WeatherStorage
    private lateinit var appPreferences: AppPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        MapLibre.getInstance(requireContext())
        val v = inflater.inflate(R.layout.fragment_map, container, false)

        appPreferences = AppPreferences(requireContext())
        weatherStorage = WeatherStorage(requireContext())

        mapView = v.findViewById(R.id.map_view)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)

        initLayerButtons(v)
        initLocationButton(v)
        initWeatherCard(v)
        initLegend(v)

        // Restore state
        if (savedInstanceState != null) {
            lastLat = savedInstanceState.getDouble(STATE_LAT, DEFAULT_LAT)
            lastLon = savedInstanceState.getDouble(STATE_LON, DEFAULT_LON)
            val layerName = savedInstanceState.getString(STATE_LAYER)
            activeLayer = layerName?.let {
                try {
                    WeatherTileLayer.Layer.valueOf(it)
                } catch (e: Exception) {
                    null
                }
            }
        }

        // Show cached weather immediately
        val saved = weatherStorage.loadWeather()
        if (saved != null) {
            val geo = weatherStorage.loadGeo()
            cachedWeather = saved
            cachedCityName = geo?.name
            if (isViewCreated) {
                showWeatherCard(saved, geo?.name)
            }
            geo?.let {
                lastLat = it.lat
                lastLon = it.lon
            }
        }

        // Try to get latest from MainActivity if available
        val activity = activity as? MainActivity
        activity?.lastGeo?.let { geo ->
            lastLat = geo.lat
            lastLon = geo.lon
        }

        isViewCreated = true
        return v
    }

    private fun initLayerButtons(v: View) {
        btnNone = v.findViewById(R.id.btn_layer_none)
        btnTemp = v.findViewById(R.id.btn_layer_temp)
        btnPrecip = v.findViewById(R.id.btn_layer_precipitation)
        btnClouds = v.findViewById(R.id.btn_layer_clouds)
        btnWind = v.findViewById(R.id.btn_layer_wind)

        btnNone?.setOnClickListener { switchLayer(null) }
        btnTemp?.setOnClickListener { switchLayer(WeatherTileLayer.Layer.TEMPERATURE) }
        btnPrecip?.setOnClickListener { switchLayer(WeatherTileLayer.Layer.PRECIPITATION) }
        btnClouds?.setOnClickListener { switchLayer(WeatherTileLayer.Layer.CLOUDS) }
        btnWind?.setOnClickListener { switchLayer(WeatherTileLayer.Layer.WIND) }
    }

    private fun initLocationButton(v: View) {
        val fab = v.findViewById<FloatingActionButton>(R.id.fab_my_location)
        fab.setOnClickListener {
            if (requireActivity() is MainActivity) {
                (requireActivity() as MainActivity).requestGeoLocation()
            }
        }
    }

    private fun initWeatherCard(v: View) {
        cardWeatherInfo = v.findViewById(R.id.card_weather_info)
        mapWeatherIcon = v.findViewById(R.id.map_weather_icon)
        mapCityName = v.findViewById(R.id.map_city_name)
        mapTemp = v.findViewById(R.id.map_temp)
        mapDesc = v.findViewById(R.id.map_desc)
        mapWind = v.findViewById(R.id.map_wind)
        mapHumidity = v.findViewById(R.id.map_humidity)
    }

    private fun initLegend(v: View) {
        cardLegend = v.findViewById(R.id.card_legend)
        legendContainer = v.findViewById(R.id.legend_container)
    }

    override fun onMapReady(map: MapLibreMap) {
        mapLibreMap = map

        map.setStyle(
            Style.Builder().fromUri(OSM_STYLE_URL)
        ) { style ->
            symbolManager = SymbolManager(mapView!!, map, style)
            symbolManager?.setIconAllowOverlap(true)
            symbolManager?.setTextAllowOverlap(true)

            // Load active layer if set
            activeLayer?.let {
                if (it == WeatherTileLayer.Layer.TEMPERATURE) {
                    loadTemperatureMarkers()
                } else {
                    addOWMLayer(style, it)
                }
            }

            updateButtonStates()
        }

        map.moveCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .target(LatLng(lastLat, lastLon))
                    .zoom(DEFAULT_ZOOM)
                    .build()
            )
        )

        map.uiSettings.setAllGesturesEnabled(true)
        map.uiSettings.setCompassEnabled(true)
        map.uiSettings.setAttributionEnabled(true)
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.isAttributionEnabled = true

        map.addOnMapClickListener { point ->
            fetchWeatherForPoint(point.latitude, point.longitude)
            true
        }
    }

    private fun fetchWeatherForPoint(lat: Double, lon: Double) {
        weatherRepository.fetchWeatherByCoords(
            lat, lon,
            object : WeatherRepository.WeatherCallback {
                override fun onSuccess(w: WeatherResponse?, geo: GeoLocation?) {
                    mainHandler.post {
                        if (isAdded) {
                            val cityName = geo?.name ?: w?.name ?: ""
                            w?.let {
                                cachedWeather = it
                                cachedCityName = cityName
                                showWeatherCard(it, cityName)
                            }
                        }
                    }
                }

                override fun onError(message: String) {
                    // Silently handle error
                }
            }
        )
    }

    private fun showWeatherCard(w: WeatherResponse?, cityName: String?) {
        if (!isViewCreated || w == null || w.main == null || cardWeatherInfo == null) return

        if (!cityName.isNullOrEmpty()) {
            mapCityName?.text = cityName
        } else {
            mapCityName?.visibility = View.GONE
        }

        val tempUnit = appPreferences.getTempUnit()
        mapTemp?.text = TemperatureConverter.formatWhole(w.main.temp, tempUnit)

        val wc = w.weather
        if (wc != null && wc.isNotEmpty() && wc[0] != null) {
            mapDesc?.text = wc[0].description ?: ""
            mapWeatherIcon?.setImageResource(WeatherIcon.getResId(wc[0].icon))
        }

        if (w.wind != null) {
            mapWind?.text = "💨 " + String.format(Locale.US, "%.1f м/с", w.wind.speed)
        }
        mapHumidity?.text = "💧 " + w.main.humidity + "%"

        cardWeatherInfo?.visibility = View.VISIBLE
    }

    private fun switchLayer(newLayer: WeatherTileLayer.Layer?) {
        activeLayer = newLayer
        updateButtonStates()
        updateLegend(newLayer)

        val map = mapLibreMap ?: return
        val style = map.style ?: return

        WeatherTileLayer.Layer.values().forEach { layer ->
            if (layer != WeatherTileLayer.Layer.TEMPERATURE) {
                removeOWMLayer(style, layer)
            }
        }
        clearTemperatureMarkers()

        when (newLayer) {
            WeatherTileLayer.Layer.TEMPERATURE -> loadTemperatureMarkers()
            else -> newLayer?.let { addOWMLayer(style, it) }
        }
    }

    private fun updateLegend(layer: WeatherTileLayer.Layer?) {
        val legend = cardLegend ?: return
        val container = legendContainer ?: return
        container.removeAllViews()

        if (layer == null || layer == WeatherTileLayer.Layer.TEMPERATURE) {
            legend.visibility = View.GONE
            return
        }

        val entries = legendEntries(layer)
        for (entry in entries) {
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 2, 0, 2)
            }

            val dot = View(requireContext()).apply {
                val lp = LinearLayout.LayoutParams(16, 16)
                lp.setMarginEnd(8)
                layoutParams = lp
                setBackgroundColor(Color.parseColor(entry[0]))
            }

            val label = TextView(requireContext()).apply {
                text = entry[1]
                textSize = 10f
                setTextColor(Color.parseColor("#1E293B"))
            }

            row.addView(dot)
            row.addView(label)
            container.addView(row)
        }
        legend.visibility = View.VISIBLE
    }

    private fun legendEntries(layer: WeatherTileLayer.Layer): Array<Array<String>> {
        return when (layer) {
            WeatherTileLayer.Layer.PRECIPITATION -> arrayOf(
                arrayOf("#B3E5FC", "лёгкий дождь"),
                arrayOf("#4FC3F7", "умеренный"),
                arrayOf("#0277BD", "сильный"),
                arrayOf("#6A1B9A", "очень сильный")
            )
            WeatherTileLayer.Layer.CLOUDS -> arrayOf(
                arrayOf("#ECEFF1", "ясно"),
                arrayOf("#B0BEC5", "малооблачно"),
                arrayOf("#607D8B", "облачно"),
                arrayOf("#263238", "пасмурно")
            )
            WeatherTileLayer.Layer.WIND -> arrayOf(
                arrayOf("#E8F5E9", "штиль"),
                arrayOf("#81C784", "слабый"),
                arrayOf("#F9A825", "умеренный"),
                arrayOf("#E53935", "сильный")
            )
            else -> emptyArray()
        }
    }

    private fun addOWMLayer(style: Style, layer: WeatherTileLayer.Layer) {
        val sourceId = WeatherTileLayer.sourceId(layer)
        val layerId = WeatherTileLayer.layerId(layer)
        val tileUrl = WeatherTileLayer.tileUrl(layer, "")

        if (style.getSource(sourceId) == null) {
            val tileSet = TileSet("2.2.0", tileUrl).apply {
                maxZoom = 12f
                minZoom = 0f
            }
            val source = RasterSource(sourceId, tileSet, 256)
            style.addSource(source)
        }

        if (style.getLayer(layerId) == null) {
            val opacity = when (layer) {
                WeatherTileLayer.Layer.CLOUDS -> 1.0f
                WeatherTileLayer.Layer.PRECIPITATION -> 0.95f
                else -> 0.8f
            }
            val rasterLayer = RasterLayer(layerId, sourceId)
            rasterLayer.setProperties(PropertyFactory.rasterOpacity(opacity))
            style.addLayer(rasterLayer)
        }
    }

    private fun removeOWMLayer(style: Style, layer: WeatherTileLayer.Layer) {
        val layerId = WeatherTileLayer.layerId(layer)
        val sourceId = WeatherTileLayer.sourceId(layer)
        if (style.getLayer(layerId) != null) style.removeLayer(layerId)
        if (style.getSource(sourceId) != null) style.removeSource(sourceId)
    }

    private fun loadTemperatureMarkers() {
        val manager = symbolManager ?: return

        if (executor == null || executor?.isShutdown == true) {
            executor = Executors.newFixedThreadPool(4)
        }

        executor?.execute {
            val latch = CountDownLatch(CITIES.size)
            val results = Collections.synchronizedList(mutableListOf<DoubleArray>())

            for (city in CITIES) {
                val lat = city[0]
                val lon = city[1]

                weatherRepository.fetchWeatherByCoords(
                    lat, lon,
                    object : WeatherRepository.WeatherCallback {
                        override fun onSuccess(w: WeatherResponse?, geo: GeoLocation?) {
                            try {
                                if (w != null && w.main != null) {
                                    results.add(doubleArrayOf(lat, lon, w.main.temp))
                                }
                            } finally {
                                latch.countDown()
                            }
                        }

                        override fun onError(message: String) {
                            latch.countDown()
                        }
                    }
                )
            }

            try {
                latch.await()
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                return@execute
            }

            mainHandler.post {
                if (!isAdded || symbolManager == null || !isViewCreated) return@post
                clearTemperatureMarkers()

                val style = mapLibreMap?.style
                if (style == null) return@post

                if (style.getImage(TEMP_ICON_ID) == null) {
                    style.addImage(TEMP_ICON_ID, createTransparentBitmap())
                }

                val tempUnit = appPreferences.getTempUnit()
                for (r in results) {
                    val text = TemperatureConverter.formatWhole(r[2], tempUnit)
                    tempSymbols.add(
                        symbolManager!!.create(
                            SymbolOptions()
                                .withLatLng(LatLng(r[0], r[1]))
                                .withIconImage(TEMP_ICON_ID)
                                .withTextField(text)
                                .withTextSize(14f)
                                .withTextColor(tempColor(r[2]))
                                .withTextHaloColor("rgba(255,255,255,1)")
                                .withTextHaloWidth(2f)
                                .withTextOffset(arrayOf(0f, 0f))
                        )
                    )
                }
            }
        }
    }

    private fun tempColor(temp: Double): String {
        return when {
            temp <= 0 -> "rgba(50,120,220,1)"
            temp <= 10 -> "rgba(80,180,180,1)"
            temp <= 20 -> "rgba(60,160,60,1)"
            temp <= 28 -> "rgba(220,150,0,1)"
            else -> "rgba(210,50,30,1)"
        }
    }

    private fun createTransparentBitmap(): Bitmap {
        return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.TRANSPARENT)
        }
    }

    private fun clearTemperatureMarkers() {
        val manager = symbolManager
        if (manager != null && tempSymbols.isNotEmpty()) {
            manager.delete(tempSymbols)
            tempSymbols.clear()
        }
    }

    private fun updateButtonStates() {
        setActive(btnNone, activeLayer == null)
        setActive(btnTemp, activeLayer == WeatherTileLayer.Layer.TEMPERATURE)
        setActive(btnPrecip, activeLayer == WeatherTileLayer.Layer.PRECIPITATION)
        setActive(btnClouds, activeLayer == WeatherTileLayer.Layer.CLOUDS)
        setActive(btnWind, activeLayer == WeatherTileLayer.Layer.WIND)
    }

    private fun setActive(btn: TextView?, active: Boolean) {
        btn?.setBackgroundResource(
            if (active) R.drawable.layer_btn_active else R.drawable.layer_btn_inactive
        )
    }

    @SuppressLint("MissingPermission")
    fun moveToLocation(lat: Double, lon: Double) {
        lastLat = lat
        lastLon = lon
        mapLibreMap?.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .target(LatLng(lat, lon))
                    .zoom(10.0)
                    .build()
            ), 800
        )
    }

    fun updateWeatherCard(w: WeatherResponse?, cityName: String?) {
        if (isAdded && isViewCreated) {
            w?.let {
                cachedWeather = it
                cachedCityName = cityName
                showWeatherCard(it, cityName)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
        
        // Show cached weather when returning to fragment
        if (cachedWeather != null) {
            showWeatherCard(cachedWeather, cachedCityName)
        }
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onSaveInstanceState(@NonNull outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
        outState.putDouble(STATE_LAT, lastLat)
        outState.putDouble(STATE_LON, lastLon)
        activeLayer?.let { outState.putString(STATE_LAYER, it.name) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isViewCreated = false
        executor?.shutdownNow()
        executor = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }
}
