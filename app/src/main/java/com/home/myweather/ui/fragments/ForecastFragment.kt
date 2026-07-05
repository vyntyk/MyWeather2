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
    }

    private lateinit var rvDaily: RecyclerView
    private lateinit var tvPlaceholder: TextView
    private lateinit var tvForecastCity: TextView
    private lateinit var dailyAdapter: DailyAdapter
    
    private var currentGeo: GeoLocation? = null
    private var pendingGeo: GeoLocation? = null
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
            if (activity is MainActivity) {
                (activity as MainActivity).openDayDetail(day)
            }
        }
        rvDaily.layoutManager = LinearLayoutManager(requireContext())
        rvDaily.adapter = dailyAdapter

        viewModel = ViewModelProvider(this).get(ForecastViewModel::class.java)

        // Observe ViewModel state
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            handleUiState(state)
        }

        if (savedInstanceState != null) {
            currentGeo = savedInstanceState.getSerializable(STATE_GEO) as? GeoLocation
            val restoredDays = savedInstanceState.getSerializable(STATE_DAYS) as? ArrayList<DailyData>
            restoredDays?.let { cachedDays = it }
        }

        if (cachedDays.isNotEmpty()) {
            updateCityTitle()
            showCachedDays()
        } else if (currentGeo != null) {
            setGeoLocation(currentGeo!!)
        } else {
            showPlaceholder()
        }

        isViewCreated = true

        pendingGeo?.let { geo ->
            setGeoLocation(geo)
            pendingGeo = null
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
            showCachedDays()
        }
    }

    fun setGeoLocation(geo: GeoLocation) {
        if (geo == null) return

        if (!isViewCreated) {
            pendingGeo = geo
            return
        }

        currentGeo = geo
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
        tvForecastCity.text = currentGeo?.name?.takeIf { it.isNotEmpty() } ?: ""
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isViewCreated = false
    }

    override fun onDestroy() {
        super.onDestroy()
        // ViewModel handles cleanup automatically
    }
}
