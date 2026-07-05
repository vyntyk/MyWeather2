package com.home.myweather.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Единая точка доступа к настройкам приложения.
 */
public final class AppPreferences {

    public static final String PREFS_NAME = "myweather_prefs";
    public static final String KEY_DARK_THEME = "dark_theme";
    public static final String KEY_TEMP_UNIT = "temp_unit";

    private final SharedPreferences prefs;

    public AppPreferences(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isDarkTheme() {
        return prefs.getBoolean(KEY_DARK_THEME, false);
    }

    public void setDarkTheme(boolean isDark) {
        prefs.edit().putBoolean(KEY_DARK_THEME, isDark).apply();
    }

    public String getTempUnit() {
        return prefs.getString(KEY_TEMP_UNIT, "C");
    }

    public void setTempUnit(String unit) {
        prefs.edit().putString(KEY_TEMP_UNIT, unit).apply();
    }

    public void applyStoredTheme() {
        AppCompatDelegate.setDefaultNightMode(
                isDarkTheme() ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public void applyTheme(boolean isDark) {
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
