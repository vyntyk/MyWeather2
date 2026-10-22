package com.home.myweather.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.model.WeatherResponse;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Постоянное хранилище последних погодных данных.
 * Данные сохраняются в SharedPreferences и переживают закрытие приложения.
 * Обновляются только после нового успешного запроса к API.
 */
public final class WeatherStorage {

    private static final String PREFS_NAME = "myweather_cache";
    private static final String KEY_WEATHER = "weather";
    private static final String KEY_GEO = "geo";
    private static final String KEY_HOURLY = "hourly";
    private static final String KEY_FORECAST_GEO = "forecast_geo";
    private static final String TAG = "WeatherStorage";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    @Inject
    public WeatherStorage(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void save(WeatherResponse weather, GeoLocation geo, List<ForecastItem> hourly) {
        SharedPreferences.Editor ed = prefs.edit();
        ed.putString(KEY_WEATHER, weather != null ? gson.toJson(weather) : null);
        ed.putString(KEY_GEO, geo != null ? gson.toJson(geo) : null);
        ed.putString(KEY_HOURLY, hourly != null ? gson.toJson(hourly) : null);
        ed.putString(KEY_FORECAST_GEO, geo != null ? gson.toJson(geo) : null);
        commit(ed, "weather snapshot");
    }

    public WeatherResponse loadWeather() {
        String json = prefs.getString(KEY_WEATHER, null);
        return json != null ? gson.fromJson(json, WeatherResponse.class) : null;
    }

    public GeoLocation loadGeo() {
        String json = prefs.getString(KEY_GEO, null);
        return json != null ? gson.fromJson(json, GeoLocation.class) : null;
    }

    public ArrayList<ForecastItem> loadHourly() {
        String json = prefs.getString(KEY_HOURLY, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<ArrayList<ForecastItem>>() {}.getType();
        ArrayList<ForecastItem> result = gson.fromJson(json, type);
        return result != null ? result : new ArrayList<>();
    }

    public GeoLocation loadForecastGeo() {
        String json = prefs.getString(KEY_FORECAST_GEO, null);
        return json != null ? gson.fromJson(json, GeoLocation.class) : null;
    }

    public void saveForecast(List<ForecastItem> hourly, GeoLocation geo) {
        SharedPreferences.Editor ed = prefs.edit();
        ed.putString(KEY_HOURLY, hourly != null ? gson.toJson(hourly) : null);
        ed.putString(KEY_FORECAST_GEO, geo != null ? gson.toJson(geo) : null);
        commit(ed, "forecast");
    }

    public boolean hasHourly() {
        return prefs.contains(KEY_HOURLY);
    }

    private static void commit(SharedPreferences.Editor editor, String dataType) {
        if (!editor.commit()) {
            Log.e(TAG, "Failed to persist " + dataType);
        }
    }
}
