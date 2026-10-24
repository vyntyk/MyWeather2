package com.home.myweather.helpers;

import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.home.myweather.data.model.WeatherResponse;
import com.home.myweather.utils.WeatherFormatter;

/**
 * Управляет отображением данных погоды в TextView и системным UI (immersive-режим).
 */
public class UiController {

    private final AppCompatActivity activity;
    private final TextView resultat, resultat2, resultat3, resultat4, resultat5;
    private final EditText cityField;
    private final SharedPreferences prefs;

    public UiController(AppCompatActivity activity,
                        TextView t1, TextView t2, TextView t3, TextView t4, TextView t5,
                        EditText cityField) {
        this.activity  = activity;
        this.resultat  = t1;
        this.resultat2 = t2;
        this.resultat3 = t3;
        this.resultat4 = t4;
        this.resultat5 = t5;
        this.cityField = cityField;
        this.prefs = activity.getSharedPreferences("myweather_prefs", 0);
    }

    public void showWeather(WeatherResponse w) {
        if (w == null || w.getMain() == null) return;
        String unit = prefs.getString("temp_unit", "C");
        resultat.setText(WeatherFormatter.temperature(w, unit));
        resultat2.setText(WeatherFormatter.wind(w));
        resultat3.setText(WeatherFormatter.pressure(w));
        resultat4.setText(WeatherFormatter.humidity(w));
        resultat5.setText(WeatherFormatter.description(w));
        if (w.getName() != null && !w.getName().isEmpty()) {
            cityField.setText(w.getName());
        }
    }

    public void showLoading(String message) {
        resultat.setText(message);
        resultat2.setText("—"); resultat3.setText("—");
        resultat4.setText("—"); resultat5.setText("—");
    }

    public void showError(String message) {
        resultat.setText(message);
        resultat2.setText("—"); resultat3.setText("—");
        resultat4.setText("—"); resultat5.setText("—");
    }

    public void hideSystemUI() {
        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(activity.getWindow(), activity.getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }
}
