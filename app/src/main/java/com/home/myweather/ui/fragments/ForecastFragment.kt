package com.home.myweather.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.home.myweather.MainActivity
import com.home.myweather.R
import com.home.myweather.data.model.GeoLocation
import com.home.myweather.ui.adapters.DailyAdapter
import com.home.myweather.ui.viewmodel.ForecastUiState // ✅ Убедитесь, что этот импорт есть
import com.home.myweather.ui.viewmodel.ForecastViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForecastFragment : Fragment() {

    private lateinit var rvDaily: RecyclerView
    private lateinit var tvPlaceholder: TextView
    private lateinit var tvForecastCity: TextView
    private lateinit var dailyAdapter: DailyAdapter
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
                (activity as? MainActivity)?.openDayDetail(day)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        rvDaily.layoutManager = LinearLayoutManager(requireContext())
        rvDaily.adapter = dailyAdapter

        val activity = activity ?: return v

        try {
            viewModel = ViewModelProvider(activity).get(ForecastViewModel::class.java)
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка инициализации: ${e.message}", Toast.LENGTH_LONG).show()
            return v
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            handleUiState(state)
        }

        val mainActivity = activity as? MainActivity
        mainActivity?.lastGeo?.let { geo ->
            setGeoLocation(geo)
        } ?: run {
            mainActivity?.requestGeoLocation()
            showPlaceholder()
        }

        isViewCreated = true
        return v
    }

    private fun handleUiState(state: ForecastUiState) {
        if (!isViewCreated) return

        if (state.isLoading) {
            tvPlaceholder.text = "Загрузка..."
            tvPlaceholder.visibility = View.VISIBLE
            rvDaily.visibility = View.GONE
            return
        }

        state.error?.let { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            showPlaceholder()
            return
        }

        if (state.days.isEmpty()) {
            showPlaceholder()
        } else {
            dailyAdapter.submitList(state.days.toList())
            updateCityTitle()
            showList()
        }
    }

    fun setGeoLocation(geo: GeoLocation) {
        updateCityTitle(geo)
        viewModel.setGeoLocation(geo)
    }

    fun showPlaceholder() {
        if (!isViewCreated) return
        rvDaily.visibility = View.GONE
        tvPlaceholder.visibility = View.VISIBLE
        tvPlaceholder.text = "Сначала найдите погоду на вкладке «Сейчас»\n\nЗатем вернитесь сюда для прогноза на 5 дней"
    }

    private fun showList() {
        if (!isViewCreated) return
        rvDaily.visibility = View.VISIBLE
        tvPlaceholder.visibility = View.GONE
    }

    fun refresh() {
        viewModel.refresh()
    }

    private fun updateCityTitle(geo: GeoLocation? = null) {
        if (!isViewCreated) return
        tvForecastCity.text = geo?.name?.takeIf { it.isNotEmpty() } ?: ""
    }
}