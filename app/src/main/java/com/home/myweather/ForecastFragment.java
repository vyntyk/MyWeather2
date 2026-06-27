package com.home.myweather;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ForecastFragment extends Fragment {

    private static final String STATE_GEO = "geo";
    private static final String STATE_DAYS = "days";

    private RecyclerView rvDaily;
    private TextView tvPlaceholder;
    private TextView tvForecastCity;
    private DailyAdapter dailyAdapter;
    private WeatherRepository weatherRepository;
    private GeoLocation currentGeo;
    private ArrayList<DailyData> cachedDays = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_forecast, container, false);
        rvDaily = v.findViewById(R.id.rv_daily);
        tvPlaceholder = v.findViewById(R.id.tv_placeholder);
        tvForecastCity = v.findViewById(R.id.tv_forecast_city);

        dailyAdapter = new DailyAdapter();
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

        return v;
    }

    public void setGeoLocation(GeoLocation geo) {
        if (geo == null) return;

        boolean sameGeo = currentGeo != null
                && Double.compare(currentGeo.lat, geo.lat) == 0
                && Double.compare(currentGeo.lon, geo.lon) == 0;
        currentGeo = geo;
        updateCityTitle();

        if (rvDaily == null || tvPlaceholder == null || dailyAdapter == null) return;

        if (sameGeo && !cachedDays.isEmpty()) {
            showCachedDays();
            return;
        }

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
        dailyAdapter.setDays(cachedDays);
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
        weatherRepository.fetchForecast(lat, lon, new WeatherRepository.ForecastCallback() {
            @Override
            public void onSuccess(ForecastResponse forecast) {
                if (isAdded() && getActivity() != null && !getActivity().isDestroyed()) {
                    requireActivity().runOnUiThread(() -> {
                        List<ForecastItem> source = forecast != null ? forecast.list : null;
                        cachedDays = new ArrayList<>(groupByDay(source));
                        dailyAdapter.setDays(cachedDays);
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

    private List<DailyData> groupByDay(List<ForecastItem> items) {
        List<DailyData> result = new ArrayList<>();
        if (items == null || items.isEmpty()) return result;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDay = "";
        DailyData current = null;

        for (ForecastItem item : items) {
            String day = sdf.format(new Date(item.timestamp * 1000L));
            if (!day.equals(currentDay)) {
                current = new DailyData();
                current.dateMillis = item.timestamp * 1000L;
                current.tempMin = item.main != null ? item.main.temp : 0;
                current.tempMax = item.main != null ? item.main.temp : 0;
                current.pop = item.pop;
                current.items = new ArrayList<>();
                current.items.add(item);
                if (item.weather != null && item.weather.length > 0 && item.weather[0] != null) {
                    current.description = item.weather[0].description;
                }
                result.add(current);
                currentDay = day;
            } else if (current != null) {
                current.items.add(item);
                if (item.main != null) {
                    current.tempMin = Math.min(current.tempMin, item.main.temp);
                    current.tempMax = Math.max(current.tempMax, item.main.temp);
                }
                current.pop = Math.max(current.pop, item.pop);
            }
        }
        return result;
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
