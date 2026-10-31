package com.home.myweather.data.repository;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.model.WeatherResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class WeatherStorageAndroidTest {

    private Context context;
    private WeatherStorage storage;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        storage = new WeatherStorage(context);
        storage.clear();
    }

    @After
    public void tearDown() {
        storage.clear();
    }

    @Test
    public void saveAndLoadWeather_roundTrip() {
        WeatherResponse weather = new WeatherResponse();
        weather.name = "Test City";

        GeoLocation geo = new GeoLocation();
        geo.lat = 55.75;
        geo.lon = 37.62;
        geo.name = "Test City";

        List<ForecastItem> hourly = new ArrayList<>();
        ForecastItem item = new ForecastItem();
        item.timestamp = 1000;
        hourly.add(item);

        storage.save(weather, geo, hourly);

        WeatherResponse loadedWeather = storage.loadWeather();
        GeoLocation loadedGeo = storage.loadGeo();
        List<ForecastItem> loadedHourly = storage.loadHourly();

        assertNotNull(loadedWeather);
        assertEquals("Test City", loadedWeather.name);

        assertNotNull(loadedGeo);
        assertEquals(55.75, loadedGeo.lat, 0.01);
        assertEquals("Test City", loadedGeo.name);

        assertNotNull(loadedHourly);
        assertEquals(1, loadedHourly.size());
    }

    @Test
    public void saveWeather_replacesCurrentWeatherAndGeoTogether() {
        WeatherResponse original = new WeatherResponse();
        original.name = "Original City";
        GeoLocation originalGeo = new GeoLocation();
        originalGeo.name = "Original City";
        storage.saveWeather(original, originalGeo);

        WeatherResponse updated = new WeatherResponse();
        updated.name = "New City";
        GeoLocation newGeo = new GeoLocation();
        newGeo.name = "New City";
        newGeo.lat = 1.0;
        newGeo.lon = 2.0;
        storage.saveWeather(updated, newGeo);

        assertEquals("New City", storage.loadWeather().name);
        assertEquals("New City", storage.loadGeo().name);
        assertEquals(2.0, storage.loadGeo().lon, 0.001);
    }

    @Test
    public void saveForecast_onlyUpdatesHourly() {
        WeatherResponse weather = new WeatherResponse();
        weather.name = "Test City";
        GeoLocation geo = new GeoLocation();
        geo.name = "Test City";

        List<ForecastItem> hourly1 = new ArrayList<>();
        hourly1.add(new ForecastItem());
        storage.save(weather, geo, hourly1);

        List<ForecastItem> hourly2 = new ArrayList<>();
        hourly2.add(new ForecastItem());
        hourly2.add(new ForecastItem());
        storage.saveForecast(hourly2, geo);

        List<ForecastItem> loaded = storage.loadHourly();
        assertEquals(2, loaded.size());

        WeatherResponse loadedWeather = storage.loadWeather();
        assertEquals("Test City", loadedWeather.name);
    }

    @Test
    public void saveForecast_storesForecastGeoSeparately() {
        GeoLocation forecastGeo = new GeoLocation();
        forecastGeo.name = "Forecast City";
        forecastGeo.lat = 10.0;
        forecastGeo.lon = 20.0;

        storage.saveForecast(new ArrayList<>(), forecastGeo);

        GeoLocation loaded = storage.loadForecastGeo();
        assertNotNull(loaded);
        assertEquals("Forecast City", loaded.name);
        assertEquals(10.0, loaded.lat, 0.001);
    }

    @Test
    public void loadEmpty_returnsNullOrEmpty() {
        WeatherStorage freshStorage = new WeatherStorage(ApplicationProvider.getApplicationContext());
        freshStorage.clear();

        assertNull(freshStorage.loadWeather());
        assertNull(freshStorage.loadGeo());
        assertNull(freshStorage.loadForecastGeo());
        assertTrue(freshStorage.loadHourly().isEmpty());
        assertFalse(freshStorage.hasHourly());
    }

    @Test
    public void hasHourly_afterSave_returnsTrue() {
        List<ForecastItem> hourly = new ArrayList<>();
        hourly.add(new ForecastItem());
        storage.saveForecast(hourly, new GeoLocation());
        assertTrue(storage.hasHourly());

        storage.clear();
        assertFalse(storage.hasHourly());
    }

    @Test
    public void clear_removesAllData() {
        WeatherResponse weather = new WeatherResponse();
        weather.name = "Test";
        GeoLocation geo = new GeoLocation();
        geo.name = "Test";
        List<ForecastItem> hourly = new ArrayList<>();
        hourly.add(new ForecastItem());

        storage.save(weather, geo, hourly);
        storage.clear();

        assertNull(storage.loadWeather());
        assertNull(storage.loadGeo());
        assertNull(storage.loadForecastGeo());
        assertTrue(storage.loadHourly().isEmpty());
    }
}
