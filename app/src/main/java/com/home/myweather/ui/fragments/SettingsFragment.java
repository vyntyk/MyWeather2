package com.home.myweather.ui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.radiobutton.MaterialRadioButton;
import com.home.myweather.R;
import com.home.myweather.MainActivity;

/**
 * SettingsFragment — реальные настройки приложения.
 */
public class SettingsFragment extends Fragment {

    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        
        prefs = requireContext().getSharedPreferences("myweather_prefs", 0);
        
        // Переключатель тёмной темы
        SwitchCompat swDarkTheme = v.findViewById(R.id.sw_dark_theme);
        boolean isDarkTheme = prefs.getBoolean("dark_theme", false);
        swDarkTheme.setChecked(isDarkTheme);
        swDarkTheme.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean("dark_theme", isChecked).apply();
            applyTheme(isChecked);
            requireActivity().recreate();
        });
        
        // Единицы измерения: Цельсий / Фаренгейт
        MaterialRadioButton rbCelsius = v.findViewById(R.id.rb_celsius);
        MaterialRadioButton rbFahrenheit = v.findViewById(R.id.rb_fahrenheit);
        
        String unit = prefs.getString("temp_unit", "C");
        if ("F".equals(unit)) {
            rbFahrenheit.setChecked(true);
        } else {
            rbCelsius.setChecked(true);
        }
        
        rbCelsius.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) {
                prefs.edit().putString("temp_unit", "C").apply();
                notifyDataChanged();
            }
        });
        
        rbFahrenheit.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) {
                prefs.edit().putString("temp_unit", "F").apply();
                notifyDataChanged();
            }
        });
        
        return v;
    }

    private void applyTheme(boolean isDark) {
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void notifyDataChanged() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).refreshWeatherDisplay();
        }
    }
}
