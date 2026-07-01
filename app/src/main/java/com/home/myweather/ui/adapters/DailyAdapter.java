package com.home.myweather.ui.adapters;

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
import java.util.List;
import java.util.Locale;
import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.data.model.DailyData;
import com.home.myweather.R;
import com.home.myweather.utils.WeatherIcon;

/**
 * ФИКС 1.3: ListAdapter + DiffUtil вместо notifyDataSetChanged()
 */
public class DailyAdapter extends ListAdapter<DailyData, DailyAdapter.ViewHolder> {

    private static final double HPA_TO_MMHG = 0.750062;

    public interface OnDayClickListener {
        void onDayClick(DailyData day);
    }

    private final SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, d MMM", Locale.getDefault());
    private OnDayClickListener listener;

    public DailyAdapter() {
        super(DIFF_CALLBACK);
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
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        DailyData day = getItem(position);
        if (day == null) return;

        h.tvDay.setText(dayFormat.format(new Date(day.dateMillis)));
        h.tvTempRange.setText(String.format(Locale.getDefault(), "%.0f° / %.0f°", day.tempMin, day.tempMax));
        h.tvDesc.setText(day.description != null ? day.description : "—");
        h.tvPop.setText(String.format(Locale.getDefault(), "Осадки %.0f%%", day.pop * 100));
        h.tvWind.setText(String.format(Locale.getDefault(), "Скорость ветра: %.1f м/с", getMaxWindSpeed(day)));
        h.tvPressure.setText(String.format(Locale.getDefault(), "Давление: %.0f мм рт. ст.", getAveragePressureMmHg(day)));
        h.tvVisibility.setText(String.format(Locale.getDefault(), "Видимость: %.1f км", getAverageVisibilityKm(day)));
        h.tvHumidity.setText(String.format(Locale.getDefault(), "Влажность: %.0f%%", getAverageHumidity(day)));

        h.ivWeather.setImageResource(WeatherIcon.getResId(getWeatherIcon(day)));

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDayClick(day);
        });
    }

    private String getWeatherIcon(DailyData day) {
        if (day.items == null || day.items.isEmpty()) return null;
        for (ForecastItem item : day.items) {
            if (item.weather != null && item.weather.length > 0 && item.weather[0] != null) {
                return item.weather[0].icon;
            }
        }
        return null;
    }

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

    private double getAveragePressureMmHg(DailyData day) {
        int count = 0;
        int pressureSum = 0;
        if (day.items != null) {
            for (ForecastItem item : day.items) {
                if (item.main != null && item.main.pressure > 0) {
                    pressureSum += item.main.pressure;
                    count++;
                }
            }
        }
        if (count == 0) return 0;
        return (pressureSum / (double) count) * HPA_TO_MMHG;
    }

    private double getAverageVisibilityKm(DailyData day) {
        int count = 0;
        int visibilitySum = 0;
        if (day.items != null) {
            for (ForecastItem item : day.items) {
                if (item.visibility > 0) {
                    visibilitySum += item.visibility;
                    count++;
                }
            }
        }
        if (count == 0) return 0;
        return (visibilitySum / (double) count) / 1000.0;
    }

    private double getAverageHumidity(DailyData day) {
        int count = 0;
        int humiditySum = 0;
        if (day.items != null) {
            for (ForecastItem item : day.items) {
                if (item.main != null && item.main.humidity >= 0) {
                    humiditySum += item.main.humidity;
                    count++;
                }
            }
        }
        if (count == 0) return 0;
        return humiditySum / (double) count;
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
