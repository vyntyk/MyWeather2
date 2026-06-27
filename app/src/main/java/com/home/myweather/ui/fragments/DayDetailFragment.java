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

/**
 * Фрагмент деталей дня — подробный прогноз на выбранный день.
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

            StringBuilder sb = new StringBuilder();
            sb.append(String.format(Locale.getDefault(), "🌡 Температура: %.0f° / %.0f°\n\n", day.tempMin, day.tempMax));
            sb.append(String.format(Locale.getDefault(), "💧 Вероятность осадков: %.0f%%\n\n", day.pop * 100));
            sb.append(String.format(Locale.getDefault(), "📝 Описание: %s\n\n", day.description != null ? day.description : "—"));

            if (day.items != null && !day.items.isEmpty()) {
                sb.append("📊 По часам:\n");
                SimpleDateFormat hf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                for (ForecastItem item : day.items) {
                    String time = hf.format(new Date(item.timestamp * 1000L));
                    double temp = item.main != null ? item.main.temp : 0;
                    double wind = item.wind != null ? item.wind.speed : 0;
                    int hum = item.main != null ? item.main.humidity : 0;
                    int press = item.main != null ? item.main.pressure : 0;
                    sb.append(String.format(Locale.getDefault(), "   %s — %.0f°, %.1f м/с, %d%%, %d гПа\n",
                            time, temp, wind, hum, press));
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
