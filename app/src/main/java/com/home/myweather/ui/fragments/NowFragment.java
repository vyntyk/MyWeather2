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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.home.myweather.R;
import com.home.myweather.data.repository.WeatherRepository;
import com.home.myweather.data.repository.ForecastCache;
import com.home.myweather.data.repository.WeatherStorage;
import com.home.myweather.data.model.WeatherResponse;
import com.home.myweather.data.model.ForecastResponse;
import com.home.myweather.utils.ComfortIndex;
import com.home.myweather.utils.WeatherIcon;
import com.home.myweather.ui.adapters.HourlyAdapter;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.MainActivity;

public class NowFragment extends Fragment {

    private static final String STATE_WEATHER = "last_weather";
    private static final String STATE_GEO     = "last_geo";
    private static final String STATE_HOURLY  = "hourly";
    private static final double HPA_TO_MMHG   = 0.750062;

    private SwipeRefreshLayout swipeRefresh;
    private ImageView ivWeatherIcon;
    private TextView tvTemp, tvFeels, tvDesc, tvComfort;
    private TextView tvWindValue, tvPressureValue, tvHumidityValue;
    private RecyclerView rvHourly;
    private EditText cityField;

    private HourlyAdapter      hourlyAdapter;
    private WeatherRepository  weatherRepository;
    private WeatherStorage     weatherStorage;
    private WeatherResponse    lastWeather;
    private GeoLocation        lastGeo;
    private ArrayList<ForecastItem> cachedHourly    = new ArrayList<>();
    private double cachedHourlyLat = Double.NaN;
    private double cachedHourlyLon = Double.NaN;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_now, container, false);

        swipeRefresh    = v.findViewById(R.id.swipe_refresh);
        ivWeatherIcon   = v.findViewById(R.id.tv_weather_icon);
        tvTemp          = v.findViewById(R.id.tv_temp);
        tvFeels         = v.findViewById(R.id.tv_feels);
        tvDesc          = v.findViewById(R.id.tv_desc);
        tvComfort       = v.findViewById(R.id.tv_comfort);
        tvWindValue     = v.findViewById(R.id.tv_wind_value);
        tvPressureValue = v.findViewById(R.id.tv_pressure_value);
        tvHumidityValue = v.findViewById(R.id.tv_humidity_value);
        rvHourly        = v.findViewById(R.id.rv_hourly);
        cityField       = v.findViewById(R.id.user_field);

        hourlyAdapter = new HourlyAdapter(requireContext());
        rvHourly.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL, false));
        rvHourly.setAdapter(hourlyAdapter);

        weatherRepository = new WeatherRepository();
        weatherStorage    = new WeatherStorage(requireContext());

        // Pull-to-refresh
        swipeRefresh.setColorSchemeResources(R.color.accent_blue);
        swipeRefresh.setOnRefreshListener(this::refreshCurrentWeather);

        if (savedInstanceState != null) {
            lastWeather = (WeatherResponse) savedInstanceState.getSerializable(STATE_WEATHER);
            lastGeo     = (GeoLocation) savedInstanceState.getSerializable(STATE_GEO);
            ArrayList<ForecastItem> restored =
                    (ArrayList<ForecastItem>) savedInstanceState.getSerializable(STATE_HOURLY);
            if (restored != null) cachedHourly = restored;
            if (lastGeo != null && !cachedHourly.isEmpty()) {
                cachedHourlyLat = lastGeo.lat;
                cachedHourlyLon = lastGeo.lon;
            }
        } else {
            lastWeather  = weatherStorage.loadWeather();
            lastGeo      = weatherStorage.loadGeo();
            cachedHourly = weatherStorage.loadHourly();
            if (lastGeo != null && !cachedHourly.isEmpty()) {
                cachedHourlyLat = lastGeo.lat;
                cachedHourlyLon = lastGeo.lon;
            }
        }

        if (lastWeather != null) showWeather(lastWeather);
        hourlyAdapter.submitList(new ArrayList<>(cachedHourly));

        if (lastGeo != null) notifyActivityAboutGeo(lastGeo);

        v.findViewById(R.id.main_btn).setOnClickListener(vv -> onSearchClick());
        v.findViewById(R.id.geo_btn).setOnClickListener(vv -> onGeoClick());

        return v;
    }

    /** Повторный запрос для текущей локации (pull-to-refresh). */
    private void refreshCurrentWeather() {
        if (lastGeo != null) {
            weatherRepository.fetchWeatherByCoords(lastGeo.lat, lastGeo.lon, weatherCallback);
        } else {
            swipeRefresh.setRefreshing(false);
        }
    }

    private void onSearchClick() {
        String city = cityField.getText().toString().trim();
        if (city.isEmpty()) {
            Toast.makeText(requireContext(), "Введите название города", Toast.LENGTH_SHORT).show();
            return;
        }
        hideKeyboard();
        showLoading();
        weatherRepository.fetchWeather(city, null, weatherCallback);
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
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).requestGeoLocation();
        }
    }

    public void loadWeatherByCoords(double lat, double lon) {
        showLoading();
        weatherRepository.fetchWeatherByCoords(lat, lon, weatherCallback);
    }

    public void loadWeatherByCity(String cityName) {
        showLoading();
        weatherRepository.fetchWeather(cityName, null, weatherCallback);
    }

    private void showLoading() {
        tvTemp.setText("—");
        tvFeels.setText("—");
        tvDesc.setText("—");
        tvComfort.setText("Загрузка...");
        tvWindValue.setText("—");
        tvPressureValue.setText("—");
        tvHumidityValue.setText("—");
        ivWeatherIcon.setImageResource(R.drawable.ow_01d);
        cachedHourly.clear();
        cachedHourlyLat = Double.NaN;
        cachedHourlyLon = Double.NaN;
        hourlyAdapter.submitList(null);
    }

    private void showWeather(WeatherResponse w) {
        if (w == null || w.getMain() == null) return;

        String unit = requireContext().getSharedPreferences("myweather_prefs", 0)
                .getString("temp_unit", "C");

        double temp      = w.getMain().getTemp();
        double feelsLike = w.getMain().getFeelsLike();

        if ("F".equals(unit)) {
            temp      = temp      * 9 / 5 + 32;
            feelsLike = feelsLike * 9 / 5 + 32;
            tvTemp.setText(String.format(Locale.US, "%.1f°F", temp));
            tvFeels.setText(String.format(Locale.US, "Ощущается: %.1f°F", feelsLike));
        } else {
            tvTemp.setText(String.format(Locale.US, "%.1f°C", temp));
            tvFeels.setText(String.format(Locale.US, "Ощущается: %.1f°C", feelsLike));
        }

        WeatherResponse.WeatherCondition[] wc = w.getWeather();
        if (wc != null && wc.length > 0 && wc[0] != null) {
            tvDesc.setText(wc[0].getDescription() != null ? wc[0].getDescription() : "—");
            ivWeatherIcon.setImageResource(WeatherIcon.getResId(wc[0].getIcon()));
        } else {
            tvDesc.setText("—");
            ivWeatherIcon.setImageResource(R.drawable.ow_01d);
        }

        if (w.getWind() != null) {
            tvWindValue.setText(String.format(Locale.US, "%.1f", w.getWind().getSpeed()));
        } else {
            tvWindValue.setText("—");
        }

        int pressureMmHg = (int) Math.round(w.getMain().getPressure() * HPA_TO_MMHG);
        tvPressureValue.setText(String.valueOf(pressureMmHg));
        tvHumidityValue.setText(String.valueOf(w.getMain().getHumidity()));

        double windSpeed = w.getWind() != null ? w.getWind().getSpeed() : 0;
        double pop       = cachedHourly.isEmpty() ? 0 : cachedHourly.get(0).pop;
        tvComfort.setText(ComfortIndex.getComfortMessage(
                temp, windSpeed, w.getMain().getHumidity(), w.getMain().getPressure(), pop));
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

        weatherRepository.fetchForecast(lat, lon, new WeatherRepository.ForecastCallback() {
            @Override
            public void onSuccess(ForecastResponse forecast) {
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
            showWeather(lastWeather);
            weatherStorage.save(lastWeather, lastGeo, cachedHourly);
        }
    }

    private void notifyActivityAboutGeo(GeoLocation geo) {
        if (geo != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onWeatherLocationLoaded(geo);
        }
    }

    private GeoLocation geoFromWeather(WeatherResponse w) {
        if (w == null || w.getCoord() == null) return null;
        GeoLocation geo = new GeoLocation();
        geo.lat  = w.getCoord().getLat();
        geo.lon  = w.getCoord().getLon();
        geo.name = w.getName();
        return geo;
    }

    private final WeatherRepository.WeatherCallback weatherCallback =
            new WeatherRepository.WeatherCallback() {
                @Override
                public void onSuccess(WeatherResponse w, GeoLocation geo) {
                    if (isAdded() && getActivity() != null && !getActivity().isDestroyed()) {
                        requireActivity().runOnUiThread(() -> {
                            swipeRefresh.setRefreshing(false);
                            lastWeather = w;
                            GeoLocation resolvedGeo = geo != null ? geo : geoFromWeather(w);
                            lastGeo = resolvedGeo;
                            showWeather(w);
                            weatherStorage.save(w, resolvedGeo, cachedHourly);
                            notifyActivityAboutGeo(resolvedGeo);
                            if (resolvedGeo != null) {
                                loadForecast(resolvedGeo.lat, resolvedGeo.lon);
                            }
                        });
                    }
                }

                @Override
                public void onError(String message) {
                    if (isAdded() && getActivity() != null && !getActivity().isDestroyed()) {
                        requireActivity().runOnUiThread(() -> {
                            swipeRefresh.setRefreshing(false);
                            tvTemp.setText("Нет соединения");
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                        });
                    }
                }
            };

    public void refresh() {
        if (lastWeather != null) showWeather(lastWeather);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        if (lastWeather != null) out.putSerializable(STATE_WEATHER, lastWeather);
        if (lastGeo     != null) out.putSerializable(STATE_GEO,     lastGeo);
        out.putSerializable(STATE_HOURLY, cachedHourly);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (weatherRepository != null) weatherRepository.cancelPendingRequests();
    }
}
