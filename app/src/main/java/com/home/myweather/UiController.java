package com.home.myweather;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Управляет отображением данных погоды в TextView и системным UI (immersive-режим).
 */
public class UiController {

    private final AppCompatActivity activity;
    private final TextView resultat, resultat2, resultat3, resultat4, resultat5;
    private final EditText cityField;

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
    }

    public void showWeather(WeatherResponse w) {
        if (w == null || w.getMain() == null) return;
        resultat.setText(WeatherFormatter.temperature(w));
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

    @SuppressLint("WrongConstant")
    public void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController c = activity.getWindow().getInsetsController();
            if (c != null) {
                c.hide(WindowInsets.Type.systemBars());
                c.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }
}
