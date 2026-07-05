package com.home.myweather.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.data.model.DailyData;
import com.home.myweather.R;
import com.home.myweather.utils.AppPreferences;
import com.home.myweather.utils.PressureConverter;
import com.home.myweather.utils.TemperatureConverter;
import com.home.myweather.utils.WeatherIcon;

/**
 * Адаптер для отображения прогноза по дням.
 * Использует утилиты TemperatureConverter и PressureConverter для единообразного форматирования.
 */
public class DailyAdapter extends ListAdapter<DailyData, DailyAdapter.ViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(DailyData day);
    }

    private final SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, d MMM", Locale.getDefault());
    private OnDayClickListener listener;
    private Context context;
    private AppPreferences appPreferences;

    public DailyAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.appPreferences = new AppPreferences(context);
    }

    public DailyAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setContext(Context context) {
        this.context = context;
        this.appPreferences = new AppPreferences(context);
    }

    private static final DiffUtil.ItemCallback<DailyData> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<DailyData>() {
                @Override
                public boolean areItemsTheSame(@NonNull DailyData old, @NonNull DailyData newDay) {
                    return old.dateMillis == newDay.dateMillis;
                }

                @Override
                public boolean areContentsTheSame(@NonNull DailyData old, @NonNull DailyData newDay) {
                    return Double.compare(old.tempMin, newDay.tempMin) == 0
                            && Double.compare(old.tempMax, newDay.tempMax) == 0
                            && Double.compare(old.pop, newDay.pop) == 0
                            && (old.description != null ? old.description.equals(newDay.description)
                                    : newDay.description == null);
                }
            };

    public void setOnDayClickListener(OnDayClickListener l) {
        this.listener = l;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) context = parent.getContext();
        if (appPreferences == null) appPreferences = new AppPreferences(context);
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_daily, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        DailyData day = getItem(position);
        if (day == null) return;

        String tempUnit = appPreferences != null ? appPreferences.getTempUnit() : "C";

        // День и дата
        h.tvDay.setText(dayFormat.format(new Date(day.dateMillis)));
        
        // Диапазон температур (используем TemperatureConverter)
        h.tvTempRange.setText(TemperatureConverter.formatRange(day.tempMin, day.tempMax, tempUnit));
        
        // Описание
        h.tvDesc.setText(day.description != null ? day.description : "—");
        
        // Осадки (в процентах)
        h.tvPop.setText(String.format(Locale.getDefault(), "Осадки: %.0f%%", day.pop * 100));
        
        // Максимальная скорость ветра
        double maxWind = getMaxWindSpeed(day);
        h.tvWind.setText(String.format(Locale.getDefault(), "Ветер: %.1f м/с", maxWind));
        
        // Среднее давление (используем PressureConverter)
        double avgPressureHpa = getAveragePressureHpa(day);
        int avgPressureMmHg = PressureConverter.toMmHg((int) avgPressureHpa);
        h.tvPressure.setText(String.format(Locale.getDefault(), "Давление: %d мм рт. ст.", avgPressureMmHg));
        
        // Видимость
        double avgVisibility = getAverageVisibilityKm(day);
        h.tvVisibility.setText(String.format(Locale.getDefault(), "Видимость: %.1f км", avgVisibility));
        
        // Влажность
        double avgHumidity = getAverageHumidity(day);
        h.tvHumidity.setText(String.format(Locale.getDefault(), "Влажность: %.0f%%", avgHumidity));

        // Иконка погоды
        h.ivWeather.setImageResource(WeatherIcon.getResId(getWeatherIcon(day)));

        // Клик по дню
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDayClick(day);
        });
    }

    /**
     * Получает иконку первого по времени элемента с погодой.
     */
    private String getWeatherIcon(DailyData day) {
        if (day.items == null || day.items.isEmpty()) return null;
        for (ForecastItem item : day.items) {
            if (item.weather != null && item.weather.length > 0 && item.weather[0] != null) {
                return item.weather[0].icon;
            }
        }
        return null;
    }

    /**
     * Получает максимальную скорость ветра за день.
     */
    private double getMaxWindSpeed(DailyData day) {
        double maxWind = 0;
        if (day.items == null) return maxWind;
        for (ForecastItem item : day.items) {
            if (item.wind != null) {
                maxWind = Math.max(maxWind, item.wind.speed);
            }
        }
        return maxWind;
    }

    /**
     * Получает среднее давление за день (в гПа).
     */
    private double getAveragePressureHpa(DailyData day) {
        int count = 0;
        double pressureSum = 0;
        if (day.items != null) {
            for (ForecastItem item : day.items) {
                if (item.main != null && item.main.pressure > 0) {
                    pressureSum += item.main.pressure;
                    count++;
                }
            }
        }
        if (count == 0) return 0;
        return pressureSum / count;
    }

    /**
     * Получает среднюю видимость за день (в км).
     */
    private double getAverageVisibilityKm(DailyData day) {
        int count = 0;
        double visibilitySum = 0;
        if (day.items != null) {
            for (ForecastItem item : day.items) {
                if (item.visibility > 0) {
                    visibilitySum += item.visibility;
                    count++;
                }
            }
        }
        if (count == 0) return 0;
        return (visibilitySum / count) / 1000.0; // конвертируем из метров в километры
    }

    /**
     * Получает среднюю влажность за день.
     */
    private double getAverageHumidity(DailyData day) {
        int count = 0;
        double humiditySum = 0;
        if (day.items != null) {
            for (ForecastItem item : day.items) {
                if (item.main != null && item.main.humidity >= 0) {
                    humiditySum += item.main.humidity;
                    count++;
                }
            }
        }
        if (count == 0) return 0;
        return humiditySum / count;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivWeather;
        final TextView tvDay, tvTempRange, tvDesc, tvPop, tvWind, tvPressure, tvVisibility, tvHumidity;

        ViewHolder(View v) {
            super(v);
            ivWeather = v.findViewById(R.id.iv_weather);
            tvDay = v.findViewById(R.id.tv_day);
            tvTempRange = v.findViewById(R.id.tv_temp_range);
            tvDesc = v.findViewById(R.id.tv_desc);
            tvPop = v.findViewById(R.id.tv_pop);
            tvWind = v.findViewById(R.id.tv_wind);
            tvPressure = v.findViewById(R.id.tv_pressure);
            tvVisibility = v.findViewById(R.id.tv_visibility);
            tvHumidity = v.findViewById(R.id.tv_humidity);
        }
    }
}
