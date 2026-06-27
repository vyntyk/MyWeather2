package com.home.myweather.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;

public class FavoriteCitiesManager {

    private static final String PREF_NAME = "favorite_cities";
    private static final String KEY_CITIES = "cities_list";

    private final SharedPreferences prefs;

    public FavoriteCitiesManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void addCity(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) return;
        
        List<String> cities = getCities();
        String trimmedName = cityName.trim();
        if (!cities.contains(trimmedName)) {
            cities.add(trimmedName);
            saveCities(cities);
        }
    }

    public void removeCity(String cityName) {
        List<String> cities = getCities();
        cities.remove(cityName);
        saveCities(cities);
    }

    public List<String> getCities() {
        String data = prefs.getString(KEY_CITIES, "");
        List<String> cities = new ArrayList<>();
        
        if (!data.isEmpty()) {
            try {
                JSONArray jsonArray = new JSONArray(data);
                for (int i = 0; i < jsonArray.length(); i++) {
                    String city = jsonArray.getString(i);
                    if (!city.trim().isEmpty()) {
                        cities.add(city.trim());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        
        return cities;
    }

    private void saveCities(List<String> cities) {
        try {
            JSONArray jsonArray = new JSONArray(cities);
            prefs.edit().putString(KEY_CITIES, jsonArray.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

