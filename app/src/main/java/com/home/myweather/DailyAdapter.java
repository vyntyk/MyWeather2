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
 * Адаптер для прогноза по дням (5 дней).
 * Вертикальный RecyclerView.
 */
public class DailyAdapter extends RecyclerView.Adapter<DailyAdapter.ViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(DailyData day);
    }

    private final List<DailyData> days = new ArrayList<>();
    private final SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, d MMM", Locale.getDefault());
    private OnDayClickListener listener;

    public void setOnDayClickListener(OnDayClickListener l) { this.listener = l; }

    public void setDays(List<DailyData> newDays) {
        days.clear();
        if (newDays != null) days.addAll(newDays);
        notifyDataSetChanged();
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
        DailyData day = days.get(position);

        h.tvDay.setText(dayFormat.format(new Date(day.dateMillis)));
        h.tvTempRange.setText(String.format(Locale.getDefault(), "%.0f° / %.0f°", day.tempMin, day.tempMax));

        String desc = day.description != null ? day.description : "—";
        h.tvDesc.setText(desc);

        h.tvPop.setText(String.format(Locale.getDefault(), "💧 %.0f%%", day.pop * 100));

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDayClick(day);
        });
    }

    @Override
    public int getItemCount() { return days.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvDay, tvTempRange, tvDesc, tvPop;
        ViewHolder(View v) {
            super(v);
            tvDay = v.findViewById(R.id.tv_day);
            tvTempRange = v.findViewById(R.id.tv_temp_range);
            tvDesc = v.findViewById(R.id.tv_desc);
            tvPop = v.findViewById(R.id.tv_pop);
        }
    }
}
