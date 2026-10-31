package com.home.myweather.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;

/**
 * Проверяет сохранение избранных городов в реальном SharedPreferences устройства.
 */
@RunWith(AndroidJUnit4.class)
public class FavoriteCitiesManagerAndroidTest {

    private static final String PREFS_NAME = "favorite_cities";
    private static final String KEY_CITIES = "cities_list";

    private Context context;
    private SharedPreferences prefs;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
    }

    @After
    public void tearDown() {
        prefs.edit().clear().commit();
    }

    @Test
    public void addedCities_surviveNewManagerInstance() {
        new FavoriteCitiesManager(context).addCity("Moscow");
        new FavoriteCitiesManager(context).addCity("London");

        FavoriteCitiesManager reloaded = new FavoriteCitiesManager(context);

        assertEquals(Arrays.asList("Moscow", "London"), reloaded.getCities());
    }

    @Test
    public void removedCity_isNotRestoredByNewManagerInstance() {
        FavoriteCitiesManager first = new FavoriteCitiesManager(context);
        first.addCity("Moscow");
        first.addCity("London");
        first.removeCity("Moscow");

        FavoriteCitiesManager reloaded = new FavoriteCitiesManager(context);

        assertEquals(Collections.singletonList("London"), reloaded.getCities());
    }

    @Test
    public void duplicateWithDifferentWhitespace_isStoredOnce() {
        FavoriteCitiesManager manager = new FavoriteCitiesManager(context);
        manager.addCity("Moscow");
        manager.addCity("  Moscow  ");

        assertEquals(Collections.singletonList("Moscow"), manager.getCities());
    }

    @Test
    public void corruptedStoredData_returnsEmptyListInsteadOfCrashing() {
        prefs.edit().putString(KEY_CITIES, "not-a-json-array").commit();

        FavoriteCitiesManager manager = new FavoriteCitiesManager(context);

        assertTrue(manager.getCities().isEmpty());
    }

    @Test
    public void storedBlankAndPaddedEntries_areNormalized() {
        prefs.edit().putString(KEY_CITIES, "[\" Moscow \",\"   \",\"Paris\"]").commit();

        FavoriteCitiesManager manager = new FavoriteCitiesManager(context);

        assertEquals(Arrays.asList("Moscow", "Paris"), manager.getCities());
    }
}
