package com.home.myweather;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Адаптер для почасового (на самом деле каждые 3 часа) прогноза.
 * Горизонтальный RecyclerView.
 */
public class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.ViewHolder> {

    private final List<ForecastItem> items = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public void setItems(List<ForecastItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hourly, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        ForecastItem item = items.get(position);

        String time = timeFormat.format(new Date(item.timestamp * 1000L));
        h.tvTime.setText(time);

        if (item.main != null) {
            h.tvTemp.setText(String.format(Locale.getDefault(), "%.0f°", item.main.temp));
        } else {
            h.tvTemp.setText("—");
        }

        if (item.weather != null && item.weather.length > 0 && item.weather[0] != null) {
            String desc = item.weather[0].description;
            h.tvDesc.setText(desc != null ? desc : "—");
        } else {
            h.tvDesc.setText("—");
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTime, tvTemp, tvDesc;
        ViewHolder(View v) {
            super(v);
            tvTime = v.findViewById(R.id.tv_time);
            tvTemp = v.findViewById(R.id.tv_temp);
            tvDesc = v.findViewById(R.id.tv_desc);
        }
    }
}
