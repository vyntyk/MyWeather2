package com.home.myweather.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.home.myweather.R;
import com.home.myweather.data.model.DailyData;
import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.utils.AppPreferences;
import com.home.myweather.utils.PressureConverter;
import com.home.myweather.utils.TemperatureConverter;
import com.home.myweather.utils.WeatherIcon;

/**
 * Фрагмент деталей дня — подробный прогноз на выбранный день в стиле тёмной карточки
 * с колонками Утро/День/Вечер/Ночь и секциями Температура/Ветер/Влажность/Давление.
 */
public class DayDetailFragment extends Fragment {

    private static final String ARG_DAY = "day";

    public static DayDetailFragment newInstance(DailyData day) {
        DayDetailFragment f = new DayDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DAY, day);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_day_detail, container, false);

        // Find all views
        ImageView ivWeatherMorning = v.findViewById(R.id.iv_weather_morning);
        ImageView ivWeatherDay = v.findViewById(R.id.iv_weather_day);
        ImageView ivWeatherEvening = v.findViewById(R.id.iv_weather_evening);
        ImageView ivWeatherNight = v.findViewById(R.id.iv_weather_night);

        TextView tvDayTitle = v.findViewById(R.id.tv_day_title);

        TextView tvTempMorning = v.findViewById(R.id.tv_temp_morning);
        TextView tvTempDay = v.findViewById(R.id.tv_temp_day);
        TextView tvTempEvening = v.findViewById(R.id.tv_temp_evening);
        TextView tvTempNight = v.findViewById(R.id.tv_temp_night);

        TextView tvFeelsMorning = v.findViewById(R.id.tv_feels_morning);
        TextView tvFeelsDay = v.findViewById(R.id.tv_feels_day);
        TextView tvFeelsEvening = v.findViewById(R.id.tv_feels_evening);
        TextView tvFeelsNight = v.findViewById(R.id.tv_feels_night);

        TextView tvWindMorning = v.findViewById(R.id.tv_wind_morning);
        TextView tvWindDay = v.findViewById(R.id.tv_wind_day);
        TextView tvWindEvening = v.findViewById(R.id.tv_wind_evening);
        TextView tvWindNight = v.findViewById(R.id.tv_wind_night);

        TextView tvDirMorning = v.findViewById(R.id.tv_dir_morning);
        TextView tvDirDay = v.findViewById(R.id.tv_dir_day);
        TextView tvDirEvening = v.findViewById(R.id.tv_dir_evening);
        TextView tvDirNight = v.findViewById(R.id.tv_dir_night);

        TextView tvHumMorning = v.findViewById(R.id.tv_hum_morning);
        TextView tvHumDay = v.findViewById(R.id.tv_hum_day);
        TextView tvHumEvening = v.findViewById(R.id.tv_hum_evening);
        TextView tvHumNight = v.findViewById(R.id.tv_hum_night);

        TextView tvPressMorning = v.findViewById(R.id.tv_press_morning);
        TextView tvPressDay = v.findViewById(R.id.tv_press_day);
        TextView tvPressEvening = v.findViewById(R.id.tv_press_evening);
        TextView tvPressNight = v.findViewById(R.id.tv_press_night);

        View btnBack = v.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(vv -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                    View fragmentContainer = getActivity().findViewById(R.id.fragment_container);
                    if (fragmentContainer != null) {
                        fragmentContainer.setVisibility(View.GONE);
                    }
                }
            });
        }

        DailyData day = null;
        if (getArguments() != null) {
            day = (DailyData) getArguments().getSerializable(ARG_DAY);
        }

        if (day != null && requireContext() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault());
            tvDayTitle.setText(sdf.format(new Date(day.dateMillis)));

            AppPreferences appPreferences = new AppPreferences(requireContext());
            String tempUnit = appPreferences.getTempUnit();

            // Group hourly items by time of day
            List<ForecastItem> items = day.items;
            if (items != null && !items.isEmpty()) {
                // Find representative item for each period
                ForecastItem morning = findItemForHour(items, 9);   // 09:00
                ForecastItem dayItem = findItemForHour(items, 15);  // 15:00
                ForecastItem evening = findItemForHour(items, 21);  // 21:00
                ForecastItem night = findItemForHour(items, 3);     // 03:00

                // Temperature + weather icon
                bindTemperature(morning, ivWeatherMorning, tvTempMorning, tempUnit);
                bindTemperature(dayItem, ivWeatherDay, tvTempDay, tempUnit);
                bindTemperature(evening, ivWeatherEvening, tvTempEvening, tempUnit);
                bindTemperature(night, ivWeatherNight, tvTempNight, tempUnit);

                // Feels like
                bindFeelsLike(morning, tvFeelsMorning, tempUnit);
                bindFeelsLike(dayItem, tvFeelsDay, tempUnit);
                bindFeelsLike(evening, tvFeelsEvening, tempUnit);
                bindFeelsLike(night, tvFeelsNight, tempUnit);

                // Wind
                bindWind(morning, tvWindMorning, tvDirMorning);
                bindWind(dayItem, tvWindDay, tvDirDay);
                bindWind(evening, tvWindEvening, tvDirEvening);
                bindWind(night, tvWindNight, tvDirNight);

                // Humidity
                bindHumidity(morning, tvHumMorning);
                bindHumidity(dayItem, tvHumDay);
                bindHumidity(evening, tvHumEvening);
                bindHumidity(night, tvHumNight);

                // Pressure
                bindPressure(morning, tvPressMorning);
                bindPressure(dayItem, tvPressDay);
                bindPressure(evening, tvPressEvening);
                bindPressure(night, tvPressNight);
            }
        }

        return v;
    }

    /** Найти прогноз ближайший к заданному часу (0-23) */
    private ForecastItem findItemForHour(List<ForecastItem> items, int targetHour) {
        ForecastItem best = null;
        int bestDiff = 24;
        SimpleDateFormat hf = new SimpleDateFormat("HH", Locale.getDefault());
        for (ForecastItem item : items) {
            String timeStr = hf.format(new Date(item.timestamp * 1000L));
            int hour = Integer.parseInt(timeStr);
            int diff = Math.abs(hour - targetHour);
            if (diff < bestDiff) {
                bestDiff = diff;
                best = item;
            }
        }
        return best != null ? best : items.get(0);
    }

    private void bindTemperature(ForecastItem item, ImageView ivWeather, TextView tvTemp, String tempUnit) {
        if (item == null) return;
        String iconCode = null;
        double temp = 0;
        if (item.weather != null && item.weather.length > 0 && item.weather[0] != null) {
            iconCode = item.weather[0].icon;
        }
        if (item.main != null) {
            temp = item.main.temp;
        }
        if (iconCode != null) {
            ivWeather.setImageResource(WeatherIcon.getResId(iconCode));
        }
        tvTemp.setText(TemperatureConverter.format(temp, tempUnit));
    }

    private void bindFeelsLike(ForecastItem item, TextView tv, String tempUnit) {
        if (item != null && item.main != null) {
            tv.setText(TemperatureConverter.format(item.main.feelsLike, tempUnit));
        } else {
            tv.setText("—");
        }
    }

    private void bindWind(ForecastItem item, TextView tvSpeed, TextView tvDir) {
        if (item != null && item.wind != null) {
            tvSpeed.setText(String.format(Locale.getDefault(), "%.1f м/с", item.wind.speed));
            tvDir.setText(getWindDirection(item.wind.deg));
        } else {
            tvSpeed.setText("—");
            tvDir.setText("");
        }
    }

    private String getWindDirection(int deg) {
        String[] dirs = {"С", "СВ", "В", "ЮВ", "Ю", "ЮЗ", "З", "СЗ"};
        int idx = Math.round(deg / 45f) % 8;
        return dirs[idx];
    }

    private void bindHumidity(ForecastItem item, TextView tv) {
        if (item != null && item.main != null) {
            tv.setText(item.main.humidity + "%");
        } else {
            tv.setText("—");
        }
    }

    private void bindPressure(ForecastItem item, TextView tv) {
        if (item != null && item.main != null && item.main.pressure > 0) {
            int mmHg = PressureConverter.toMmHg(item.main.pressure);
            tv.setText(String.valueOf(mmHg));
        } else {
            tv.setText("—");
        }
    }
}