package com.home.myweather.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.home.myweather.R;
import com.home.myweather.data.model.DailyData;
import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.utils.AppPreferences;
import com.home.myweather.utils.PressureConverter;
import com.home.myweather.utils.TemperatureConverter;

/**
 * Фрагмент деталей дня — подробный прогноз на выбранный день.
 * Использует AppPreferences, TemperatureConverter и PressureConverter для единообразного форматирования.
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

        TextView tvTitle = v.findViewById(R.id.tv_day_title);
        TextView tvDetails = v.findViewById(R.id.tv_day_details);

        DailyData day = null;
        if (getArguments() != null) {
            day = (DailyData) getArguments().getSerializable(ARG_DAY);
        }

        if (day != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault());
            tvTitle.setText(sdf.format(new Date(day.dateMillis)));

            AppPreferences appPreferences = new AppPreferences(requireContext());
            String tempUnit = appPreferences.getTempUnit();

            StringBuilder sb = new StringBuilder();
            
            // Диапазон температур
            sb.append("🌡 Температура: ")
              .append(TemperatureConverter.formatRange(day.tempMin, day.tempMax, tempUnit))
              .append("\n\n");
            
            // Вероятность осадков
            sb.append(String.format(Locale.getDefault(), "💧 Вероятность осадков: %.0f%%\n\n", day.pop * 100));
            
            // Описание
            sb.append(String.format(Locale.getDefault(), "📝 Описание: %s\n\n", 
                    day.description != null ? day.description : "—"));

            // Подробно по часам
            if (day.items != null && !day.items.isEmpty()) {
                sb.append("📊 По часам:\n");
                SimpleDateFormat hf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                
                for (ForecastItem item : day.items) {
                    String time = hf.format(new Date(item.timestamp * 1000L));
                    
                    // Температура
                    String tempStr = "—";
                    if (item.main != null) {
                        tempStr = TemperatureConverter.format(item.main.temp, tempUnit);
                    }
                    
                    // Ветер
                    String windStr = "—";
                    if (item.wind != null) {
                        windStr = String.format(Locale.getDefault(), "%.1f м/с", item.wind.speed);
                    }
                    
                    // Влажность
                    String humStr = "—";
                    if (item.main != null) {
                        humStr = item.main.humidity + "%";
                    }
                    
                    // Давление (в мм рт. ст.)
                    String pressureStr = "—";
                    if (item.main != null && item.main.pressure > 0) {
                        int pressureMmHg = PressureConverter.toMmHg((int) item.main.pressure);
                        pressureStr = pressureMmHg + " мм рт.ст.";
                    }
                    
                    sb.append(String.format(Locale.getDefault(), 
                            "   %s — %s, %s, %s, %s\n",
                            time, tempStr, windStr, humStr, pressureStr));
                }
            }

            tvDetails.setText(sb.toString());
        }

        v.findViewById(R.id.btn_back).setOnClickListener(vv -> {
            if (getActivity() != null) getActivity().getSupportFragmentManager().popBackStack();
        });

        return v;
    }
}
