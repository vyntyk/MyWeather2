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
 * Фрагмент «Настройки» — заглушка.
 */
public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        TextView tv = v.findViewById(R.id.tv_settings_placeholder);
        tv.setText("⚙ Настройки\n\n• Светлая / тёмная тема\n• Единицы измерения (°C / °F)\n• Язык интерфейса\n• Уведомления о погоде\n• О приложении");
        return v;
    }
}
