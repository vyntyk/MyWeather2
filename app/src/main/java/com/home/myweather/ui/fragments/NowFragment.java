package com.home.myweather.ui.fragments;

import android.os.Bundle;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.home.myweather.R;
import com.home.myweather.helpers.LocationHelper;
import com.home.myweather.helpers.UiController;
import com.home.myweather.data.repository.WeatherRepository;
import com.home.myweather.data.model.WeatherResponse;
import com.home.myweather.data.model.DailyData;
import com.home.myweather.utils.WeatherFormatter;
import com.home.myweather.data.model.ForecastResponse;
import com.home.myweather.utils.ComfortIndex;
import com.home.myweather.ui.adapters.HourlyAdapter;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.MainActivity;

public class NowFragment extends Fragment {

    private static final String STATE_WEATHER = "last_weather";
    private static final String STATE_GEO = "last_geo";
    private static final String STATE_HOURLY = "hourly";
    private static final String STATE_BG_RES = "bg_res";

    private TextView tvCity, tvTemp, tvFeels, tvDesc, tvWind, tvPressure, tvHumidity;
    private TextView tvComfort;
    private RecyclerView rvHourly;
    private EditText cityField;
    private ConstraintLayout mBackground;

    private HourlyAdapter hourlyAdapter;
    private WeatherRepository weatherRepository;
    private WeatherResponse lastWeather;
    private GeoLocation lastGeo;
    private ArrayList<ForecastItem> cachedHourly = new ArrayList<>();
    private double cachedHourlyLat = Double.NaN;
    private double cachedHourlyLon = Double.NaN;
    private int selectedBgRes = R.drawable.foto4;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_now, container, false);

        tvCity = v.findViewById(R.id.tv_city);
        tvTemp = v.findViewById(R.id.tv_temp);
        tvFeels = v.findViewById(R.id.tv_feels);
        tvDesc = v.findViewById(R.id.tv_desc);
        tvWind = v.findViewById(R.id.tv_wind);
        tvPressure = v.findViewById(R.id.tv_pressure);
        tvHumidity = v.findViewById(R.id.tv_humidity);
        tvComfort = v.findViewById(R.id.tv_comfort);
        rvHourly = v.findViewById(R.id.rv_hourly);
        cityField = v.findViewById(R.id.user_field);
        mBackground = v.findViewById(R.id.background);

        hourlyAdapter = new HourlyAdapter();
        rvHourly.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL, false));
        rvHourly.setAdapter(hourlyAdapter);

        weatherRepository = new WeatherRepository();

        if (savedInstanceState != null) {
            lastWeather = (WeatherResponse) savedInstanceState.getSerializable(STATE_WEATHER);
            lastGeo = (GeoLocation) savedInstanceState.getSerializable(STATE_GEO);
            ArrayList<ForecastItem> restoredHourly =
                    (ArrayList<ForecastItem>) savedInstanceState.getSerializable(STATE_HOURLY);
            if (restoredHourly != null) cachedHourly = restoredHourly;
            selectedBgRes = savedInstanceState.getInt(STATE_BG_RES, R.drawable.foto4);
            if (lastGeo != null && !cachedHourly.isEmpty()) {
                cachedHourlyLat = lastGeo.lat;
                cachedHourlyLon = lastGeo.lon;
            }
        }

        if (lastWeather != null) showWeather(lastWeather);
        hourlyAdapter.setItems(cachedHourly);
        mBackground.setBackgroundResource(selectedBgRes);

        v.findViewById(R.id.main_btn).setOnClickListener(vv -> onSearchClick());
        v.findViewById(R.id.geo_btn).setOnClickListener(vv -> onGeoClick());
        v.findViewById(R.id.btn1).setOnClickListener(vv -> setBackground(R.drawable.foto1));
        v.findViewById(R.id.btn2).setOnClickListener(vv -> setBackground(R.drawable.foto2));
        v.findViewById(R.id.btn3).setOnClickListener(vv -> setBackground(R.drawable.foto3));

        return v;
    }

    private void setBackground(int resId) {
        selectedBgRes = resId;
        if (mBackground != null) mBackground.setBackgroundResource(resId);
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
        tvTemp.setText("Загрузка...");
        tvFeels.setText("—");
        tvWind.setText("—");
        tvPressure.setText("—");
        tvHumidity.setText("—");
        tvDesc.setText("—");
        tvComfort.setText("—");
        cachedHourly.clear();
        cachedHourlyLat = Double.NaN;
        cachedHourlyLon = Double.NaN;
        hourlyAdapter.setItems(null);
    }

    private void showWeather(WeatherResponse w) {
        if (w == null || w.getMain() == null) return;

        tvCity.setText(w.getName() != null ? w.getName() : "—");
        tvTemp.setText(String.format(Locale.getDefault(), "%.1f°C", w.getMain().getTemp()));
        tvFeels.setText(String.format(Locale.getDefault(), "Ощущается: %.1f°C", w.getMain().getFeelsLike()));
        tvPressure.setText(String.format(Locale.getDefault(), "Давление: %d гПа", w.getMain().getPressure()));
        tvHumidity.setText(String.format(Locale.getDefault(), "Влажность: %d%%", w.getMain().getHumidity()));

        if (w.getWind() != null) {
            tvWind.setText(String.format(Locale.getDefault(), "Ветер: %.1f м/с", w.getWind().getSpeed()));
        } else {
            tvWind.setText("Ветер: нет данных");
        }

        WeatherResponse.WeatherCondition[] wc = w.getWeather();
        if (wc != null && wc.length > 0 && wc[0] != null) {
            tvDesc.setText(wc[0].getDescription() != null ? wc[0].getDescription() : "—");
        } else {
            tvDesc.setText("—");
        }

        double temp = w.getMain().getTemp();
        double windSpeed = w.getWind() != null ? w.getWind().getSpeed() : 0;
        int humidity = w.getMain().getHumidity();
        int pressure = w.getMain().getPressure();
        double pop = cachedHourly.isEmpty() ? 0 : cachedHourly.get(0).pop;
        int weatherId = (wc != null && wc.length > 0 && wc[0] != null) ? wc[0].getId() : 800;

        tvComfort.setText(ComfortIndex.getComfortMessage(temp, windSpeed, humidity, pressure, pop));

        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        boolean isDay = hour >= 6 && hour < 20;
        selectedBgRes = ComfortIndex.getBackgroundResource(weatherId, isDay);
        mBackground.setBackgroundResource(selectedBgRes);
    }

    private void loadForecast(double lat, double lon) {
        if (Double.compare(cachedHourlyLat, lat) == 0
                && Double.compare(cachedHourlyLon, lon) == 0
                && !cachedHourly.isEmpty()) {
            hourlyAdapter.setItems(cachedHourly);
            return;
        }

        weatherRepository.fetchForecast(lat, lon, new WeatherRepository.ForecastCallback() {
            @Override
            public void onSuccess(ForecastResponse forecast) {
                if (isAdded() && getActivity() != null && !getActivity().isDestroyed()) {
                    requireActivity().runOnUiThread(() -> {
                        cachedHourly = new ArrayList<>();
                        List<ForecastItem> source = forecast != null ? forecast.list : null;
                        int count = source != null ? Math.min(source.size(), 8) : 0;
                        for (int i = 0; i < count; i++) cachedHourly.add(source.get(i));
                        cachedHourlyLat = lat;
                        cachedHourlyLon = lon;
                        hourlyAdapter.setItems(cachedHourly);
                        if (lastWeather != null) showWeather(lastWeather);
                    });
                }
            }

            @Override
            public void onError(String message) {
                // The current weather is still useful even if the hourly forecast fails.
            }
        });
    }

    private void notifyActivityAboutGeo(GeoLocation geo) {
        if (geo != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onWeatherLocationLoaded(geo);
        }
    }

    private GeoLocation geoFromWeather(WeatherResponse w) {
        if (w == null || w.getCoord() == null) return null;
        GeoLocation geo = new GeoLocation();
        geo.lat = w.getCoord().getLat();
        geo.lon = w.getCoord().getLon();
        geo.name = w.getName();
        return geo;
    }

    private final WeatherRepository.WeatherCallback weatherCallback =
            new WeatherRepository.WeatherCallback() {
                @Override
                public void onSuccess(WeatherResponse w, GeoLocation geo) {
                    if (isAdded() && getActivity() != null && !getActivity().isDestroyed()) {
                        requireActivity().runOnUiThread(() -> {
                            lastWeather = w;
                            GeoLocation resolvedGeo = geo != null ? geo : geoFromWeather(w);
                            lastGeo = resolvedGeo;
                            showWeather(w);
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
                            tvTemp.setText("Нет соединения с интернетом");
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                        });
                    }
                }
            };

    @Override
    public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        if (lastWeather != null) out.putSerializable(STATE_WEATHER, lastWeather);
        if (lastGeo != null) out.putSerializable(STATE_GEO, lastGeo);
        out.putSerializable(STATE_HOURLY, cachedHourly);
        out.putInt(STATE_BG_RES, selectedBgRes);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (weatherRepository != null) weatherRepository.cancelPendingRequests();
    }
}
