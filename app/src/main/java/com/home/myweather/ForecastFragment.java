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

/**
 * Фрагмент «5 дней» — полный прогноз на 5 дней с карточками по дням.
 */
public class ForecastFragment extends Fragment {

    private RecyclerView rvDaily;
    private TextView tvPlaceholder;
    private DailyAdapter dailyAdapter;
    private WeatherRepository weatherRepository;
    private GeoLocation currentGeo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_forecast, container, false);
        rvDaily = v.findViewById(R.id.rv_daily);
        tvPlaceholder = v.findViewById(R.id.tv_placeholder);

        dailyAdapter = new DailyAdapter();
        dailyAdapter.setOnDayClickListener(day -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDayDetail(day);
            }
        });
        rvDaily.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvDaily.setAdapter(dailyAdapter);

        weatherRepository = new WeatherRepository();

        return v;
    }

    public void setGeoLocation(GeoLocation geo) {
        this.currentGeo = geo;
        if (geo != null) {
            showList();
            loadForecast(geo.lat, geo.lon);
        }
    }

    public void showPlaceholder() {
        rvDaily.setVisibility(View.GONE);
        tvPlaceholder.setVisibility(View.VISIBLE);
        tvPlaceholder.setText("Сначала найдите погоду на вкладке «Сейчас»\n\nЗатем вернитесь сюда для прогноза на 5 дней");
    }

    private void showList() {
        rvDaily.setVisibility(View.VISIBLE);
        tvPlaceholder.setVisibility(View.GONE);
    }

    private void loadForecast(double lat, double lon) {
        weatherRepository.fetchForecast(lat, lon, new WeatherRepository.ForecastCallback() {
            @Override
            public void onSuccess(ForecastResponse forecast) {
                if (isAdded() && getActivity() != null && !getActivity().isDestroyed()) {
                    requireActivity().runOnUiThread(() -> {
                        List<DailyData> days = groupByDay(forecast.list);
                        dailyAdapter.setDays(days);
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

    /**
     * Группирует 3-часовые блоки по дням.
     */
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
            } else {
                if (current != null) {
                    current.items.add(item);
                    if (item.main != null) {
                        current.tempMin = Math.min(current.tempMin, item.main.temp);
                        current.tempMax = Math.max(current.tempMax, item.main.temp);
                    }
                    current.pop = Math.max(current.pop, item.pop);
                }
            }
        }
        return result;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (weatherRepository != null) weatherRepository.cancelPendingRequests();
    }
}
