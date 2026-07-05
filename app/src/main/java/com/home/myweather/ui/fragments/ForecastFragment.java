package com.home.myweather.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.home.myweather.R;
import com.home.myweather.data.repository.WeatherRepository;
import com.home.myweather.data.repository.ForecastCache;
import com.home.myweather.data.model.ForecastResponse;
import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.ui.adapters.DailyAdapter;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.model.DailyData;
import com.home.myweather.utils.ForecastGrouper;
import com.home.myweather.MainActivity;

public class ForecastFragment extends Fragment {

    private static final String STATE_GEO = "geo";
    private static final String STATE_DAYS = "days";

    private RecyclerView rvDaily;
    private TextView tvPlaceholder;
    private TextView tvForecastCity;
    private DailyAdapter dailyAdapter;
    private WeatherRepository weatherRepository;
    private GeoLocation currentGeo;
    private GeoLocation pendingGeo;
    private ArrayList<DailyData> cachedDays = new ArrayList<>();
    private boolean isViewCreated = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_forecast, container, false);
        rvDaily = v.findViewById(R.id.rv_daily);
        tvPlaceholder = v.findViewById(R.id.tv_placeholder);
        tvForecastCity = v.findViewById(R.id.tv_forecast_city);

        dailyAdapter = new DailyAdapter(requireContext());
        dailyAdapter.setOnDayClickListener(day -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDayDetail(day);
            }
        });
        rvDaily.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvDaily.setAdapter(dailyAdapter);

        weatherRepository = new WeatherRepository();

        if (savedInstanceState != null) {
            currentGeo = (GeoLocation) savedInstanceState.getSerializable(STATE_GEO);
            ArrayList<DailyData> restoredDays =
                    (ArrayList<DailyData>) savedInstanceState.getSerializable(STATE_DAYS);
            if (restoredDays != null) cachedDays = restoredDays;
        }

        if (!cachedDays.isEmpty()) {
            updateCityTitle();
            showCachedDays();
        } else if (currentGeo != null) {
            setGeoLocation(currentGeo);
        } else {
            showPlaceholder();
        }

        isViewCreated = true;

        if (pendingGeo != null) {
            setGeoLocation(pendingGeo);
            pendingGeo = null;
        }

        return v;
    }

    public void setGeoLocation(GeoLocation geo) {
        if (geo == null) return;

        if (!isViewCreated) {
            pendingGeo = geo;
            return;
        }

        currentGeo = geo;
        updateCityTitle();

        if (rvDaily == null || tvPlaceholder == null || dailyAdapter == null) return;

        cachedDays.clear();
        showList();
        loadForecast(geo.lat, geo.lon);
    }

    public void showPlaceholder() {
        if (rvDaily == null || tvPlaceholder == null) return;
        rvDaily.setVisibility(View.GONE);
        tvPlaceholder.setVisibility(View.VISIBLE);
        tvPlaceholder.setText("Сначала найдите погоду на вкладке «Сейчас»\n\nЗатем вернитесь сюда для прогноза на 5 дней");
    }

    private void showCachedDays() {
        updateCityTitle();
        showList();
        dailyAdapter.submitList(new ArrayList<>(cachedDays));
    }

    private void updateCityTitle() {
        if (tvForecastCity == null) return;
        if (currentGeo != null && currentGeo.name != null && !currentGeo.name.trim().isEmpty()) {
            tvForecastCity.setText(currentGeo.name);
        } else {
            tvForecastCity.setText("");
        }
    }

    private void showList() {
        if (rvDaily == null || tvPlaceholder == null) return;
        rvDaily.setVisibility(View.VISIBLE);
        tvPlaceholder.setVisibility(View.GONE);
    }

    private void loadForecast(double lat, double lon) {
        List<ForecastItem> sharedCache = ForecastCache.get(lat, lon);
        if (sharedCache != null) {
            cachedDays = new ArrayList<>(ForecastGrouper.groupByDay(sharedCache));
            dailyAdapter.submitList(new ArrayList<>(cachedDays));
            return;
        }

        weatherRepository.fetchForecast(lat, lon, new WeatherRepository.ForecastCallback() {
            @Override
            public void onSuccess(ForecastResponse forecast) {
                if (isAdded() && getActivity() != null && !getActivity().isDestroyed()) {
                    requireActivity().runOnUiThread(() -> {
                        List<ForecastItem> source = forecast != null ? forecast.list : null;
                        ForecastCache.put(lat, lon, source);
                        cachedDays = new ArrayList<>(ForecastGrouper.groupByDay(source));
                        dailyAdapter.submitList(new ArrayList<>(cachedDays));
                    });
                }
            }

            @Override
            public void onError(String message) {
                if (isAdded() && getActivity() != null && !getActivity().isDestroyed()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    public void refresh() {
        if (!cachedDays.isEmpty()) {
            showCachedDays();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentGeo != null) outState.putSerializable(STATE_GEO, currentGeo);
        outState.putSerializable(STATE_DAYS, cachedDays);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isViewCreated = false;
        rvDaily = null;
        tvPlaceholder = null;
        tvForecastCity = null;
        dailyAdapter = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (weatherRepository != null) weatherRepository.cancelPendingRequests();
    }
}
