package com.home.myweather.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.home.myweather.R;

/**
 * ФИКС 1.3: ListAdapter + DiffUtil вместо notifyDataSetChanged()
 */
public class FavoriteCitiesAdapter extends ListAdapter<String, FavoriteCitiesAdapter.ViewHolder> {

    public interface OnCityClickListener {
        void onCityClick(String cityName);
        void onCityRemove(String cityName);
    }

    private OnCityClickListener listener;

    public FavoriteCitiesAdapter(OnCityClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<String> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<String>() {
                @Override
                public boolean areItemsTheSame(@NonNull String old, @NonNull String newItem) {
                    return old.equals(newItem);
                }

                @Override
                public boolean areContentsTheSame(@NonNull String old, @NonNull String newItem) {
                    return old.equals(newItem);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_city, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String city = getItem(position);
        if (city == null) return;

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
