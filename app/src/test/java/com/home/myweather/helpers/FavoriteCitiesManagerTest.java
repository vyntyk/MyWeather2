package com.home.myweather.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.home.myweather.helpers.FavoriteCitiesManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class FavoriteCitiesManagerTest {

    private FavoriteCitiesManager manager;
    private Context context;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        context = RuntimeEnvironment.getApplication();
        manager = new FavoriteCitiesManager(context);
    }

    @Test
    public void getCities_emptyStorage_returnsEmptyList() {
        List<String> cities = manager.getCities();
        assertNotNull(cities);
        assertTrue(cities.isEmpty());
    }

    @Test
    public void addCity_addsCityToList() {
        manager.addCity("Moscow");
        List<String> cities = manager.getCities();
        assertEquals(1, cities.size());
        assertEquals("Moscow", cities.get(0));
    }

    @Test
    public void addCity_trimsWhitespace() {
        manager.addCity("  Saint Petersburg  ");
        List<String> cities = manager.getCities();
        assertEquals(1, cities.size());
        assertEquals("Saint Petersburg", cities.get(0));
    }

    @Test
    public void addCity_nullOrEmpty_doesNotAdd() {
        manager.addCity(null);
        manager.addCity("");
        manager.addCity("   ");
        List<String> cities = manager.getCities();
        assertTrue(cities.isEmpty());
    }

    @Test
    public void addCity_duplicate_doesNotAddTwice() {
        manager.addCity("Moscow");
        manager.addCity("Moscow");
        List<String> cities = manager.getCities();
        assertEquals(1, cities.size());
    }

    @Test
    public void removeCity_removesExistingCity() {
        manager.addCity("Moscow");
        manager.addCity("London");
        manager.removeCity("Moscow");
        List<String> cities = manager.getCities();
        assertEquals(1, cities.size());
        assertEquals("London", cities.get(0));
    }

    @Test
    public void removeCity_nonExistent_noEffect() {
        manager.addCity("Moscow");
        manager.removeCity("London");
        List<String> cities = manager.getCities();
        assertEquals(1, cities.size());
        assertEquals("Moscow", cities.get(0));
    }

    @Test
    public void addMultipleCities_preservesOrder() {
        manager.addCity("Moscow");
        manager.addCity("London");
        manager.addCity("Paris");
        List<String> cities = manager.getCities();
        assertEquals(3, cities.size());
        assertEquals("Moscow", cities.get(0));
        assertEquals("London", cities.get(1));
        assertEquals("Paris", cities.get(2));
    }
}