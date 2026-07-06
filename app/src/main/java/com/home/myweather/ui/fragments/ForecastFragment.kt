package com.home.myweather.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.home.myweather.R
import com.home.myweather.data.model.DailyData
import com.home.myweather.data.model.GeoLocation
import com.home.myweather.ui.adapters.DailyAdapter
import com.home.myweather.ui.viewmodel.ForecastUiState
import com.home.myweather.ui.viewmodel.ForecastViewModel
import com.home.myweather.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.home.myweather.data.repository.WeatherRepository
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

@AndroidEntryPoint
class ForecastFragment : Fragment() {

    companion object {
        private const val STATE_GEO = "geo"
        private const val STATE_DAYS = "days"
        private const val STATE_GEO_SOURCE = "geo_source"
    }

    private lateinit var rvDaily: RecyclerView
    private lateinit var tvPlaceholder: TextView
    private lateinit var tvForecastCity: TextView
    private lateinit var dailyAdapter: DailyAdapter
    
    private var currentGeo: GeoLocation? = null
    private var geoSource: String? = null
    private var pendingGeo: GeoLocation? = null
    private var cachedDays: ArrayList<DailyData> = ArrayList()
    
    private lateinit var viewModel: ForecastViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_forecast, container, false)
        
        rvDaily = v.findViewById(R.id.rv_daily)
        tvPlaceholder = v.findViewById(R.id.tv_placeholder)
        tvForecastCity = v.findViewById(R.id.tv_forecast_city)

        dailyAdapter = DailyAdapter(requireContext())
        dailyAdapter.setOnDayClickListener { day ->
            // Call activity to show DayDetailFragment
            try {
                val mainActivity = activity as? MainActivity
                mainActivity?.openDayDetail(day)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        rvDaily.layoutManager = LinearLayoutManager(requireContext())
        rvDaily.adapter = dailyAdapter

        // For Hilt ViewModels, use activity-scoped scope
        // Use requireActivity() but handle potential null gracefully
        val activity = activity
        if (activity == null) {
            return v
        }
        try {
            viewModel = ViewModelProvider(activity).get(ForecastViewModel::class.java)
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка инициализации: ${e.message}", Toast.LENGTH_LONG).show()
            return v
        }

        // Observe ViewModel state
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            handleUiState(state)
        }

        if (savedInstanceState != null) {
            currentGeo = savedInstanceState.getSerializable(STATE_GEO) as? GeoLocation
            val restoredDays = savedInstanceState.getSerializable(STATE_DAYS) as? ArrayList<DailyData>
            restoredDays?.let { cachedDays = it }
            geoSource = savedInstanceState.getString(STATE_GEO_SOURCE)
        }

        // Load weather if we have saved data or if MainActivity has geo location
        if (cachedDays.isNotEmpty()) {
            updateCityTitle()
            showCachedDays()
        } else {
            // Check if MainActivity has geo location we can use
            val mainActivity = activity as? MainActivity
            mainActivity?.getGeoLocation()?.let { geo ->
                currentGeo = geo
                geoSource = mainActivity.lastGeoSource
                updateCityTitle()
                setGeoLocation(geo, geoSource)
            } ?: run {
                // If MainActivity doesn't have geo, try to trigger location request
                mainActivity?.requestGeoLocation()
                showPlaceholder()
            }
        }

        return v
    }

    private fun handleUiState(state: ForecastUiState) {
        if (state.isLoading) {
            tvPlaceholder.text = "Загрузка..."
            tvPlaceholder.visibility = View.VISIBLE
            rvDaily.visibility = View.GONE
            return
        }

        state.error?.let { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            return
        }

        if (state.days.isEmpty()) {
            showPlaceholder()
        } else {
            cachedDays = ArrayList(state.days)
            dailyAdapter.submitList(ArrayList(cachedDays))
            updateCityTitle()
            showList()
        }
    }

    fun setGeoLocation(geo: GeoLocation, source: String? = null) {
        if (geo == null) return

        currentGeo = geo
        geoSource = source ?: geo.name.takeIf { it.isNotEmpty() }
        updateCityTitle()

        cachedDays.clear()
        showList()
        viewModel.loadForecast(geo.lat, geo.lon)
    }

    fun showPlaceholder() {
        rvDaily.visibility = View.GONE
        tvPlaceholder.visibility = View.VISIBLE
        tvPlaceholder.text = "Сначала найдите погоду на вкладке «Сейчас»\n\nЗатем вернитесь сюда для прогноза на 5 дней"
    }

    private fun showCachedDays() {
        updateCityTitle()
        showList()
        dailyAdapter.submitList(ArrayList(cachedDays))
    }

    private fun updateCityTitle() {
        val city = currentGeo?.name?.takeIf { it.isNotEmpty() } ?: ""
        // Only show source if it's different from city name (e.g., GPS)
        val source = geoSource?.takeIf { it.isNotEmpty() && it != city }?.let { " ($it)" } ?: ""
        tvForecastCity.text = "$city$source"
    }

    private fun showList() {
        rvDaily.visibility = View.VISIBLE
        tvPlaceholder.visibility = View.GONE
    }

    fun refresh() {
        viewModel.refresh()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        currentGeo?.let { outState.putSerializable(STATE_GEO, it) }
        outState.putSerializable(STATE_DAYS, cachedDays)
        geoSource?.let { outState.putString(STATE_GEO_SOURCE, it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        // ViewModel handles cleanup automatically
    }
}
