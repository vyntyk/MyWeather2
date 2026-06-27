package com.home.myweather;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Фрагмент «Избранные города» — заглушка.
 */
public class CitiesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cities, container, false);
        TextView tv = v.findViewById(R.id.tv_cities_placeholder);
        tv.setText("⭐ Избранные города\n\nЗдесь будет список сохранённых городов с быстрым доступом к погоде.");
        return v;
    }
}
