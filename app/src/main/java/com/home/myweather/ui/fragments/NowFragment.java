package com.home.myweather.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import com.home.myweather.R;
import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.model.WeatherResponse;
import com.home.myweather.data.repository.ForecastCache;
import com.home.myweather.data.repository.WeatherRepository;
import com.home.myweather.data.repository.WeatherStorage;
import com.home.myweather.utils.AppPreferences;
import com.home.myweather.utils.ComfortIndex;
import com.home.myweather.utils.PressureConverter;
import com.home.myweather.utils.TemperatureConverter;
import com.home.myweather.utils.WeatherIcon;
import com.home.myweather.ui.adapters.HourlyAdapter;
import com.home.myweather.ui.viewmodel.NowViewModel;
import com.home.myweather.ui.viewmodel.WeatherUiState;
import com.home.myweather.MainActivity;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class NowFragment extends Fragment {

    private static final String STATE_GEO = "last_geo";
    private static final String STATE_HOURLY = "hourly";

    private SwipeRefreshLayout swipeRefresh;
    private ImageView ivWeatherIcon;
    private TextView tvTemp, tvFeels, tvDesc, tvComfort;
    private TextView tvWindValue, tvPressureValue, tvHumidityValue;
    private RecyclerView rvHourly;
    private EditText cityField;

    private HourlyAdapter hourlyAdapter;
    private NowViewModel viewModel;
    private WeatherResponse lastWeather;
    private GeoLocation lastGeo;
    private ArrayList<ForecastItem> cachedHourly = new ArrayList<>();
    private double cachedHourlyLat = Double.NaN;
    private double cachedHourlyLon = Double.NaN;

    @Inject
    AppPreferences appPreferences;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_now, container, false);

        swipeRefresh = v.findViewById(R.id.swipe_refresh);
        ivWeatherIcon = v.findViewById(R.id.tv_weather_icon);
        tvTemp = v.findViewById(R.id.tv_temp);
        tvFeels = v.findViewById(R.id.tv_feels);
        tvDesc = v.findViewById(R.id.tv_desc);
        tvComfort = v.findViewById(R.id.tv_comfort);
        tvWindValue = v.findViewById(R.id.tv_wind_value);
        tvPressureValue = v.findViewById(R.id.tv_pressure_value);
        tvHumidityValue = v.findViewById(R.id.tv_humidity_value);
        rvHourly = v.findViewById(R.id.rv_hourly);
        cityField = v.findViewById(R.id.user_field);

        hourlyAdapter = new HourlyAdapter(requireContext());
        rvHourly.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL, false));
        rvHourly.setAdapter(hourlyAdapter);

        viewModel = new ViewModelProvider(getActivity()).get(NowViewModel.class);

        // Обновляем UI при изменении состояния
        viewModel.getUiState().observe(getViewLifecycleOwner(), this::updateUi);

        // Pull-to-refresh
        swipeRefresh.setColorSchemeResources(R.color.accent_blue);
        swipeRefresh.setOnRefreshListener(() -> {
            if (lastGeo != null) {
                viewModel.fetchWeatherByCoords(lastGeo.lat, lastGeo.lon);
            } else {
                swipeRefresh.setRefreshing(false);
            }
        });

        if (savedInstanceState != null) {
            GeoLocation restoredGeo = (GeoLocation) savedInstanceState.getSerializable(STATE_GEO);
            ArrayList<ForecastItem> restoredHourly =
                    (ArrayList<ForecastItem>) savedInstanceState.getSerializable(STATE_HOURLY);
            if (restoredGeo != null) {
                lastGeo = restoredGeo;
                viewModel.fetchWeatherByCoords(restoredGeo.lat, restoredGeo.lon);
            }
            if (restoredHourly != null) {
                cachedHourly = restoredHourly;
                hourlyAdapter.submitList(new ArrayList<>(cachedHourly));
            }
        } else {
            // Загружаем сохраненную погоду
            WeatherStorage weatherStorage = new WeatherStorage(requireContext());
            WeatherResponse savedWeather = weatherStorage.loadWeather();
            GeoLocation savedGeo = weatherStorage.loadGeo();
            ArrayList<ForecastItem> savedHourly = weatherStorage.loadHourly();
            
            if (savedWeather != null) {
                lastWeather = savedWeather;
                updateUi(new WeatherUiState(savedWeather, savedGeo, false, null));
                cachedHourly = savedHourly != null ? savedHourly : new ArrayList<>();
                hourlyAdapter.submitList(new ArrayList<>(cachedHourly));
            }
            if (savedGeo != null) {
                lastGeo = savedGeo;
                // Notify activity about loaded geo location
                notifyActivityAboutGeo(savedGeo);
            }
        }

        v.findViewById(R.id.main_btn).setOnClickListener(vv -> onSearchClick());
        v.findViewById(R.id.geo_btn).setOnClickListener(vv -> onGeoClick());

        return v;
    }

    private void updateUi(WeatherUiState state) {
        if (state.isLoading()) {
            tvTemp.setText("—");
            tvFeels.setText("—");
            tvDesc.setText("—");
            tvComfort.setText("Загрузка...");
            tvWindValue.setText("—");
            tvPressureValue.setText("—");
            tvHumidityValue.setText("—");
            ivWeatherIcon.setImageResource(R.drawable.ow_01d);
            swipeRefresh.setRefreshing(true);
            return;
        }

        swipeRefresh.setRefreshing(false);

        if (state.getError() != null) {
            tvTemp.setText("Нет соединения");
            Toast.makeText(requireContext(), state.getError(), Toast.LENGTH_LONG).show();
            return;
        }

        WeatherResponse weather = state.getWeather();
        GeoLocation geo = state.getGeo();

        if (weather == null || weather.getMain() == null) return;

        lastWeather = weather;
        lastGeo = geo;

        String tempUnit = appPreferences.getTempUnit();

        tvTemp.setText(TemperatureConverter.format(weather.getMain().getTemp(), tempUnit));
        tvFeels.setText(TemperatureConverter.formatFeelsLike(weather.getMain().getFeelsLike(), tempUnit));

        WeatherResponse.WeatherCondition[] wc = weather.getWeather();
        if (wc != null && wc.length > 0 && wc[0] != null) {
            tvDesc.setText(wc[0].getDescription() != null ? wc[0].getDescription() : "—");
            ivWeatherIcon.setImageResource(WeatherIcon.getResId(wc[0].getIcon()));
        } else {
            tvDesc.setText("—");
            ivWeatherIcon.setImageResource(R.drawable.ow_01d);
        }

        if (weather.getWind() != null) {
            tvWindValue.setText(String.format("%.1f м/с", weather.getWind().getSpeed()));
        } else {
            tvWindValue.setText("—");
        }

        int pressureMmHg = PressureConverter.toMmHg(weather.getMain().getPressure());
        tvPressureValue.setText(String.valueOf(pressureMmHg));
        tvHumidityValue.setText(String.valueOf(weather.getMain().getHumidity()) + "%");

        double windSpeed = weather.getWind() != null ? weather.getWind().getSpeed() : 0;
        double pop = cachedHourly.isEmpty() ? 0 : cachedHourly.get(0).pop;
        double temp = TemperatureConverter.toDisplay(weather.getMain().getTemp(), tempUnit);
        tvComfort.setText(ComfortIndex.getComfortMessage(
                temp, windSpeed, weather.getMain().getHumidity(), weather.getMain().getPressure(), pop));

        if (geo != null) {
            notifyActivityAboutGeo(geo);
            loadForecast(geo.lat, geo.lon);
        }
    }

    private void onSearchClick() {
        String city = cityField.getText().toString().trim();
        if (city.isEmpty()) {
            Toast.makeText(requireContext(), "Введите название города", Toast.LENGTH_SHORT).show();
            return;
        }
        hideKeyboard();
        viewModel.fetchWeatherByCity(city);
    }

    private void hideKeyboard() {
        cityField.clearFocus();
        InputMethodManager imm =
                (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getView() != null) {
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }

    private void onGeoClick() {
        if (getActivity() == null) {
            Toast.makeText(requireContext(), "Ошибка: активность недоступна", Toast.LENGTH_SHORT).show();
            return;
        }
        if (getActivity() instanceof MainActivity) {
            Toast.makeText(requireContext(), "Определение местоположения...", Toast.LENGTH_SHORT).show();
            ((MainActivity) getActivity()).requestGeoLocation();
        }
    }

    public void loadWeatherByCoords(double lat, double lon) {
        lastGeo = new GeoLocation();
        lastGeo.lat = lat;
        lastGeo.lon = lon;
        lastGeo.name = "GPS";
        viewModel.fetchWeatherByCoords(lat, lon);
    }

    public void loadWeatherByCity(String cityName) {
        viewModel.fetchWeatherByCity(cityName);
    }

    private void loadForecast(double lat, double lon) {
        if (Double.compare(cachedHourlyLat, lat) == 0
                && Double.compare(cachedHourlyLon, lon) == 0
                && !cachedHourly.isEmpty()) {
            hourlyAdapter.submitList(new ArrayList<>(cachedHourly));
            return;
        }

        List<ForecastItem> sharedCache = ForecastCache.get(lat, lon);
        if (sharedCache != null) {
            applyHourly(sharedCache, lat, lon);
            return;
        }

        // Прямой вызов репозитория для прогноза (ViewModel пока только для текущей погоды)
        viewModel.getWeatherRepository().fetchForecast(lat, lon, new WeatherRepository.ForecastCallback() {
            @Override
            public void onSuccess(com.home.myweather.data.model.ForecastResponse forecast) {
                if (isAdded() && getActivity() != null && !getActivity().isDestroyed()) {
                    requireActivity().runOnUiThread(() -> {
                        List<ForecastItem> source = forecast != null ? forecast.list : null;
                        ForecastCache.put(lat, lon, source);
                        applyHourly(source, lat, lon);
                    });
                }
            }

            @Override
            public void onError(String message) { /* тихо */ }
        });
    }

    private void applyHourly(List<ForecastItem> source, double lat, double lon) {
        cachedHourly = new ArrayList<>();
        int count = source != null ? Math.min(source.size(), 8) : 0;
        for (int i = 0; i < count; i++) cachedHourly.add(source.get(i));
        cachedHourlyLat = lat;
        cachedHourlyLon = lon;
        hourlyAdapter.submitList(new ArrayList<>(cachedHourly));
        if (lastWeather != null) {
            WeatherStorage weatherStorage = new WeatherStorage(requireContext());
            weatherStorage.save(lastWeather, lastGeo, cachedHourly);
        }
    }

    private void notifyActivityAboutGeo(GeoLocation geo) {
        if (geo != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onWeatherLocationLoaded(geo);
        }
    }

    public void refresh() {
        if (lastWeather != null) {
            updateUi(new WeatherUiState(lastWeather, lastGeo, false, null));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        if (lastGeo != null) out.putSerializable(STATE_GEO, lastGeo);
        out.putSerializable(STATE_HOURLY, cachedHourly);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (viewModel != null) {
            viewModel.clearRequests();
        }
    }
}
