package com.home.myweather.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.home.myweather.R;
public class FavoriteCitiesAdapter extends RecyclerView.Adapter<FavoriteCitiesAdapter.ViewHolder> {

    public interface OnCityClickListener {
        void onCityClick(String cityName);
        void onCityRemove(String cityName);
    }

    private List<String> cities;
    private OnCityClickListener listener;

    public FavoriteCitiesAdapter(List<String> cities, OnCityClickListener listener) {
        this.cities = cities;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_city, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String city = cities.get(position);
        holder.tvCity.setText(city);
        
        holder.tvCity.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCityClick(city);
            }
        });
        
        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCityRemove(city);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cities != null ? cities.size() : 0;
    }

    public void updateCities(List<String> newCities) {
        this.cities = newCities;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCity;
        ImageButton btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCity = itemView.findViewById(R.id.tv_city_name);
            btnRemove = itemView.findViewById(R.id.btn_remove_city);
        }
    }
}
