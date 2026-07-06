package com.home.myweather.ui.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.home.myweather.R;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.model.WeatherResponse;
import com.home.myweather.helpers.LocationHelper;
import com.home.myweather.ui.adapters.HourlyAdapter;
import com.home.myweather.ui.viewmodel.NowViewModel;
import com.home.myweather.utils.AppPreferences;
import com.home.myweather.utils.ComfortIndex;
import com.home.myweather.utils.PressureConverter;
import com.home.myweather.utils.TemperatureConverter;
import com.home.myweather.utils.WeatherIcon;
import com.home.myweather.MainActivity;

import java.util.Locale;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Фрагмент текущей погоды.
 * Отображает текущие условия, используя NowViewModel для управления состоянием.
 */
@AndroidEntryPoint
public class NowFragment extends Fragment {

    private NowViewModel viewModel;
    private AppPreferences appPreferences;
    private LocationHelper locationHelper;

    private SwipeRefreshLayout swipeRefresh;
    private TextInputEditText userField;
    private ImageButton mainBtn, geoBtn;
    private ImageView tvWeatherIcon;
    private TextView tvDesc, tvTemp, tvFeels, tvComfort, tvWindValue, tvPressureValue, tvHumidityValue;
    private RecyclerView rvHourly;
    private HourlyAdapter hourlyAdapter;

    private static final int LOCATION_PERMISSION_REQUEST = 101;
    private boolean locationHelperInitialized = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(NowViewModel.class);
        appPreferences = new AppPreferences(requireContext());
        locationHelper = new LocationHelper((AppCompatActivity) requireActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_now, container, false);
        
        // Prevent NestedScrollView from stealing focus
        v.findViewById(R.id.background).setFocusable(true);
        v.findViewById(R.id.background).setFocusableInTouchMode(true);
        v.findViewById(R.id.background).requestFocus();
        
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        userField = view.findViewById(R.id.user_field);
        mainBtn = view.findViewById(R.id.main_btn);
        geoBtn = view.findViewById(R.id.geo_btn);
        tvWeatherIcon = view.findViewById(R.id.tv_weather_icon);
        tvDesc = view.findViewById(R.id.tv_desc);
        tvTemp = view.findViewById(R.id.tv_temp);
        tvFeels = view.findViewById(R.id.tv_feels);
        tvComfort = view.findViewById(R.id.tv_comfort);
        tvWindValue = view.findViewById(R.id.tv_wind_value);
        tvPressureValue = view.findViewById(R.id.tv_pressure_value);
        tvHumidityValue = view.findViewById(R.id.tv_humidity_value);
        rvHourly = view.findViewById(R.id.rv_hourly);
        
        // Set focus to search field to show keyboard
        userField.requestFocus();

        // Initialize location helper only once
        if (!locationHelperInitialized) {
            locationHelper.init(this);
            locationHelperInitialized = true;
        }

        setupRecyclerView();
        setupListeners();
        observeViewModel();

        GeoLocation lastGeo = viewModel.lastGeo;
        if (lastGeo != null) {
            viewModel.fetchWeatherByCoords(lastGeo.lat, lastGeo.lon);
        }
    }

    private void setupRecyclerView() {
        hourlyAdapter = new HourlyAdapter();
        rvHourly.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvHourly.setAdapter(hourlyAdapter);
    }

    private void setupListeners() {
        mainBtn.setOnClickListener(v -> searchWeather());
        geoBtn.setOnClickListener(v -> requestLocationAndFetchWeather());

        swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refresh();
            swipeRefresh.setRefreshing(false);
        });
    }

    private void searchWeather() {
        String city = userField.getText().toString().trim();
        if (city.isEmpty()) {
            Toast.makeText(requireContext(), "Введите название города", Toast.LENGTH_SHORT).show();
            return;
        }
        hideKeyboard();
        loadWeatherByCity(city);
    }

    private void hideKeyboard() {
        userField.clearFocus();
        if (getActivity() != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager)
                    getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null && getView() != null) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }
    }

    private void requestLocationAndFetchWeather() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fetchWeatherByLocation();
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    private void fetchWeatherByLocation() {
        locationHelper.requestLocation(new LocationHelper.Callback() {
            @Override
            public void onLocationReady(double lat, double lon) {
                loadWeatherByCoords(lat, lon);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchWeatherByLocation();
        }
    }

    private void observeViewModel() {
        viewModel.getUiStateLiveData().observe(getViewLifecycleOwner(), state -> {
            if (state.isLoading) {
                showLoading();
            } else if (state.error != null) {
                showError(state.error);
            } else if (state.weather != null) {
                showWeather(state.weather, state.geo);
            }
        });
        
        // Observe hourly forecast
        viewModel.getHourlyForecast().observe(getViewLifecycleOwner(), forecast -> {
            setHourlyForecast(forecast);
        });
    }

    private void showLoading() {
        swipeRefresh.setRefreshing(true);
    }

    private void showError(String message) {
        swipeRefresh.setRefreshing(false);
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showWeather(WeatherResponse weather, GeoLocation geo) {
        swipeRefresh.setRefreshing(false);
        String tempUnit = appPreferences.getTempUnit();

        if (weather.main != null) {
            tvTemp.setText(TemperatureConverter.format(weather.main.temp, tempUnit));
            tvFeels.setText("Ощущается: " + TemperatureConverter.format(weather.main.feelsLike, tempUnit));
            tvHumidityValue.setText(String.valueOf(weather.main.humidity));

            if (weather.main.pressure > 0) {
                int pressureMmHg = PressureConverter.toMmHg(weather.main.pressure);
                tvPressureValue.setText(String.valueOf(pressureMmHg));
            }
        }

        if (weather.wind != null) {
            tvWindValue.setText(String.format(Locale.getDefault(), "%.1f", weather.wind.speed));
        }

        if (weather.weather != null && weather.weather.length > 0) {
            tvDesc.setText(weather.weather[0].description);
            String iconCode = weather.weather[0].icon;
            int iconRes = WeatherIcon.getResId(iconCode);
            tvWeatherIcon.setImageResource(iconRes);
        }

        String comfortMessage = ComfortIndex.getComfortMessage(
                weather.main != null ? weather.main.temp : 0,
                weather.wind != null ? weather.wind.speed : 0,
                weather.main != null ? weather.main.humidity : 0,
                weather.main != null ? weather.main.pressure : 0,
                0.0
        );
        tvComfort.setText(comfortMessage);

        if (geo != null) {
            userField.setText(geo.name);
            // Inform MainActivity about the weather location and source
            if (getActivity() instanceof MainActivity) {
                MainActivity ma = (MainActivity) getActivity();
                ma.onWeatherLocationLoaded(geo, "Search");
            }
        }

        // Load hourly forecast for next 24 hours
        viewModel.loadForecast(weather.coord.lat, weather.coord.lon);
    }

    public void setHourlyForecast(List<com.home.myweather.data.model.ForecastItem> forecast) {
        if (forecast != null && !forecast.isEmpty()) {
            // Show first 24 hours
            int displayCount = Math.min(forecast.size(), 24);
            hourlyAdapter.submitList(forecast.subList(0, displayCount));
        } else {
            hourlyAdapter.submitList(java.util.Collections.emptyList());
        }
    }

    // Методы для MainActivity
    public void loadWeatherByCoords(double lat, double lon) {
        viewModel.fetchWeatherByCoords(lat, lon);
    }

    public void loadWeatherByCity(String cityName) {
        viewModel.fetchWeatherByCity(cityName);
    }

    public void refresh() {
        viewModel.refresh();
    }
}
