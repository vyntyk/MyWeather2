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
import com.home.myweather.ui.viewmodel.ForecastViewModel
import com.home.myweather.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.ViewModelProvider

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
    private var cachedDays: ArrayList<DailyData> = ArrayList()
    private var isViewCreated = false
    
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
            try {
                val mainActivity = activity as? MainActivity
                mainActivity?.openDayDetail(day)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        rvDaily.layoutManager = LinearLayoutManager(requireContext())
        rvDaily.adapter = dailyAdapter

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

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            handleUiState(state)
        }

        if (savedInstanceState != null) {
            currentGeo = savedInstanceState.getSerializable(STATE_GEO) as? GeoLocation
            val restoredDays = savedInstanceState.getSerializable(STATE_DAYS) as? ArrayList<DailyData>
            restoredDays?.let { 
                cachedDays = it
                if (isViewCreated && cachedDays.isNotEmpty()) {
                    showCachedDays()
                }
            }
            geoSource = savedInstanceState.getString(STATE_GEO_SOURCE)
        }

        if (cachedDays.isNotEmpty()) {
            updateCityTitle()
            showCachedDays()
        } else {
            val mainActivity = activity as? MainActivity
            mainActivity?.lastGeo?.let { geo ->
                currentGeo = geo
                geoSource = mainActivity.lastGeoSource
                updateCityTitle()
                setGeoLocation(geo, geoSource)
            } ?: run {
                mainActivity?.requestGeoLocation()
                showPlaceholder()
            }
        }

        isViewCreated = true
        return v
    }

    private fun handleUiState(state: com.home.myweather.ui.viewmodel.ForecastUiState) {
        if (!isViewCreated) return
        
        if (state.isLoading) {
            if (cachedDays.isEmpty()) {
                tvPlaceholder.text = "Загрузка..."
                tvPlaceholder.visibility = View.VISIBLE
                rvDaily.visibility = View.GONE
            }
            return
        }

        state.error?.let { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            if (cachedDays.isEmpty()) {
                showPlaceholder()
            }
            return
        }

        if (state.days.isEmpty()) {
            if (cachedDays.isEmpty()) {
                showPlaceholder()
            }
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
        if (!isViewCreated) return
        rvDaily.visibility = View.GONE
        tvPlaceholder.visibility = View.VISIBLE
        tvPlaceholder.text = "Сначала найдите погоду на вкладке «Сейчас»\n\nЗатем вернитесь сюда для прогноза на 5 дней"
    }

    private fun showCachedDays() {
        if (!isViewCreated) return
        updateCityTitle()
        showList()
        dailyAdapter.submitList(ArrayList(cachedDays))
    }

    private fun updateCityTitle() {
        if (!isViewCreated) return
        tvForecastCity.text = currentGeo?.name?.takeIf { it.isNotEmpty() } ?: ""
    }

    private fun showList() {
        if (!isViewCreated) return
        rvDaily.visibility = View.VISIBLE
        tvPlaceholder.visibility = View.GONE
    }

    fun refresh() {
        if (currentGeo != null) {
            viewModel.loadForecast(currentGeo!!.lat, currentGeo!!.lon)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (currentGeo != null) outState.putSerializable(STATE_GEO, currentGeo)
        if (cachedDays.isNotEmpty()) outState.putSerializable(STATE_DAYS, cachedDays)
        if (geoSource != null) outState.putString(STATE_GEO_SOURCE, geoSource)
    }
}
