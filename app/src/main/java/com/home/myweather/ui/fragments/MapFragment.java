package com.home.myweather.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.home.myweather.R;

/**
 * Фрагмент «Карта» — заглушка.
 * В полной реализации: WebView с OpenWeatherMap tiles или Google Maps.
 */
public class MapFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        TextView tv = v.findViewById(R.id.tv_map_placeholder);
        tv.setText("🗺 Карта погоды\n\nЗдесь будет отображаться карта с осадками, температурой, облачностью и ветром.\n\nДля реализации подключите WebView с погодными тайлами OpenWeatherMap или Google Maps.");
        return v;
    }
}
