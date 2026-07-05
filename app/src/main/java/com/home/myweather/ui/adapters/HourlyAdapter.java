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

import com.home.myweather.R;
import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.utils.AppPreferences;
import com.home.myweather.utils.TemperatureConverter;
import com.home.myweather.utils.WeatherIcon;

/**
 * Адаптер для почасового прогноза (горизонтальный RecyclerView).
 * Использует AppPreferences для единообразного доступа к настройкам.
 * Использует TemperatureConverter для форматирования температур.
 */
public class HourlyAdapter extends ListAdapter<ForecastItem, HourlyAdapter.ViewHolder> {

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private Context context;
    private AppPreferences appPreferences;

    public HourlyAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.appPreferences = new AppPreferences(context);
    }

    public HourlyAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setContext(Context context) {
        this.context = context;
        this.appPreferences = new AppPreferences(context);
    }

    /**
     * DiffUtil.ItemCallback определяет, как сравнивать два элемента:
     * - areItemsTheSame(): одинаковые ли элементы (уникальный ID)?
     * - areContentsTheSame(): одинаковое ли содержимое?
     * - getChangePayload(): что именно изменилось (опционально, для оптимизации bind)
     */
    private static final DiffUtil.ItemCallback<ForecastItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ForecastItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull ForecastItem old,
                                              @NonNull ForecastItem newItem) {
                    // Предполагаем, что timestamp уникален для каждого блока прогноза
                    return old.timestamp == newItem.timestamp;
                }

                @Override
                public boolean areContentsTheSame(@NonNull ForecastItem old,
                                                 @NonNull ForecastItem newItem) {
                    // Сравниваем основные поля: температура, вероятность осадков, иконка
                    boolean tempSame = (old.main != null && newItem.main != null)
                            ? Double.compare(old.main.temp, newItem.main.temp) == 0
                            : old.main == newItem.main;
                    
                    boolean popSame = Double.compare(old.pop, newItem.pop) == 0;
                    
                    boolean iconSame = getIconCode(old).equals(getIconCode(newItem));
                    
                    return tempSame && popSame && iconSame;
                }

                private String getIconCode(ForecastItem item) {
                    if (item.weather != null && item.weather.length > 0 
                            && item.weather[0] != null) {
                        return item.weather[0].icon;
                    }
                    return "";
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) context = parent.getContext();
        if (appPreferences == null) appPreferences = new AppPreferences(context);
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_hourly, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        ForecastItem item = getItem(position);
        if (item == null) return;

        // Время
        h.tvTime.setText(timeFormat.format(new Date(item.timestamp * 1000L)));

        // Температура (используем TemperatureConverter)
        if (item.main != null) {
            String tempUnit = appPreferences != null ? appPreferences.getTempUnit() : "C";
            h.tvTemp.setText(TemperatureConverter.format(item.main.temp, tempUnit));
        } else {
            h.tvTemp.setText("—");
        }

        // Вероятность осадков
        h.tvPop.setText(String.format(Locale.getDefault(), "%.0f%%", item.pop * 100));

        // SVG-иконка погоды
        String iconCode = null;
        if (item.weather != null && item.weather.length > 0 && item.weather[0] != null) {
            iconCode = item.weather[0].icon;
        }
        h.ivIcon.setImageResource(WeatherIcon.getResId(iconCode));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTime, tvTemp, tvPop;
        final ImageView ivIcon;

        ViewHolder(View v) {
            super(v);
            tvTime = v.findViewById(R.id.tv_time);
            tvTemp = v.findViewById(R.id.tv_temp);
            tvPop = v.findViewById(R.id.tv_pop);
            ivIcon = v.findViewById(R.id.iv_icon);
        }
    }
}
