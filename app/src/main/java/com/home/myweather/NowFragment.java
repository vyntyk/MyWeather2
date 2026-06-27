package com.home.myweather;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * Фрагмент «Сейчас» — главный экран.
 * Показывает текущую погоду, почасовой прогноз (3-часовой) и краткий 5-дневный.
 */
public class NowFragment extends Fragment {

    private TextView tvCity, tvTemp, tvFeels, tvDesc, tvWind, tvPressure, tvHumidity;
    private TextView tvComfort;
    private RecyclerView rvHourly;
    private EditText cityField;
    private ConstraintLayout mBackground;

    private HourlyAdapter hourlyAdapter;
    private WeatherRepository weatherRepository;
    private WeatherResponse lastWeather;
    private GeoLocation lastGeo;
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

        // Загрузка сохранённого состояния
        if (savedInstanceState != null) {
            lastWeather = (WeatherResponse) savedInstanceState.getSerializable("last_weather");
            selectedBgRes = savedInstanceState.getInt("bg_res", R.drawable.foto4);
            if (lastWeather != null) showWeather(lastWeather);
        }
        mBackground.setBackgroundResource(selectedBgRes);

        // Кнопки поиска и геолокации
        v.findViewById(R.id.main_btn).setOnClickListener(vv -> onSearchClick());
        v.findViewById(R.id.geo_btn).setOnClickListener(vv -> onGeoClick());

        // Кнопки фона
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
        showLoading();
        weatherRepository.fetchWeather(city, null, weatherCallback);
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

    private void showLoading() {
        tvTemp.setText("Загрузка...");
        tvFeels.setText("—");
        tvWind.setText("—");
        tvPressure.setText("—");
        tvHumidity.setText("—");
        tvDesc.setText("—");
        tvComfort.setText("—");
        hourlyAdapter.setItems(null);
    }

    private void showWeather(WeatherResponse w) {
        if (w == null || w.getMain() == null) return;

        tvCity.setText(w.getName() != null ? w.getName() : "—");
        tvTemp.setText(String.format(java.util.Locale.getDefault(), "%.1f°C", w.getMain().getTemp()));
        tvFeels.setText(String.format(java.util.Locale.getDefault(), "Ощущается: %.1f°C", w.getMain().getFeelsLike()));
        tvPressure.setText(String.format(java.util.Locale.getDefault(), "Давление: %d гПа", w.getMain().getPressure()));
        tvHumidity.setText(String.format(java.util.Locale.getDefault(), "Влажность: %d%%", w.getMain().getHumidity()));

        if (w.getWind() != null) {
            tvWind.setText(String.format(java.util.Locale.getDefault(), "Ветер: %.1f м/с", w.getWind().getSpeed()));
        } else {
            tvWind.setText("Ветер: нет данных");
        }

        WeatherResponse.WeatherCondition[] wc = w.getWeather();
        if (wc != null && wc.length > 0 && wc[0] != null) {
            tvDesc.setText(wc[0].getDescription() != null ? wc[0].getDescription() : "—");
        } else {
            tvDesc.setText("—");
        }

        // Индекс комфорта
        double temp = w.getMain().getTemp();
        double windSpeed = w.getWind() != null ? w.getWind().getSpeed() : 0;
        int humidity = w.getMain().getHumidity();
        int pressure = w.getMain().getPressure();
        double pop = 0;
        int weatherId = (wc != null && wc.length > 0 && wc[0] != null) ? wc[0].getId() : 800;

        tvComfort.setText(ComfortIndex.getComfortMessage(temp, windSpeed, humidity, pressure, pop));

        // Фон по погоде
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        boolean isDay = hour >= 6 && hour < 20;
        selectedBgRes = ComfortIndex.getBackgroundResource(weatherId, isDay);
        mBackground.setBackgroundResource(selectedBgRes);
    }

    private void loadForecast(double lat, double lon) {
        weatherRepository.fetchForecast(lat, lon, new WeatherRepository.ForecastCallback() {
            @Override
            public void onSuccess(ForecastResponse forecast) {
                if (isAdded() && getActivity() != null && !getActivity().isDestroyed()) {
                    requireActivity().runOnUiThread(() -> {
                        // Берём первые 8 записей (24 часа с шагом 3)
                        List<ForecastItem> hourly = new ArrayList<>();
                        int count = Math.min(forecast.list.size(), 8);
                        for (int i = 0; i < count; i++) hourly.add(forecast.list.get(i));
                        hourlyAdapter.setItems(hourly);
                    });
                }
            }
            @Override
            public void onError(String message) {
                // Тихо игнорируем ошибку прогноза — текущая погода уже показана
            }
        });
    }

    private final WeatherRepository.WeatherCallback weatherCallback =
            new WeatherRepository.WeatherCallback() {
                @Override
                public void onSuccess(WeatherResponse w, GeoLocation geo) {
                    if (isAdded() && getActivity() != null && !getActivity().isDestroyed()) {
                        requireActivity().runOnUiThread(() -> {
                            lastWeather = w;
                            lastGeo = geo;
                            showWeather(w);
                            // Загружаем прогноз по тем же координатам
                            if (geo != null) {
                                loadForecast(geo.lat, geo.lon);
                            } else if (w.getCoord() != null) {
                                loadForecast(w.getCoord().getLat(), w.getCoord().getLon());
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
        if (lastWeather != null) out.putSerializable("last_weather", lastWeather);
        out.putInt("bg_res", selectedBgRes);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (weatherRepository != null) weatherRepository.cancelPendingRequests();
    }
}
