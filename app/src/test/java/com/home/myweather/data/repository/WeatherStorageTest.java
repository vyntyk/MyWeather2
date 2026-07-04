package com.home.myweather.data.repository;

import android.content.Context;

import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.model.WeatherResponse;
import com.home.myweather.data.repository.WeatherStorage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class WeatherStorageTest {

    private WeatherStorage storage;
    private Context context;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        storage = new WeatherStorage(context);
    }

    @Test
    public void loadWeather_noData_returnsNull() {
        WeatherResponse weather = storage.loadWeather();
        assertNull(weather);
    }

    @Test
    public void loadGeo_noData_returnsNull() {
        GeoLocation geo = storage.loadGeo();
        assertNull(geo);
    }

    @Test
    public void loadHourly_noData_returnsEmptyList() {
        List<ForecastItem> hourly = storage.loadHourly();
        assertNotNull(hourly);
        assertTrue(hourly.isEmpty());
    }

    @Test
    public void saveAndLoadWeather_works() throws Exception {
        WeatherResponse weather = createWeatherResponse(22.5, 1013, 65, "Moscow");
        GeoLocation geo = createGeoLocation(55.75, 37.61, "Moscow", "RU");
        List<ForecastItem> hourly = createForecastItems(2);

        storage.save(weather, geo, hourly);

        WeatherResponse loadedWeather = storage.loadWeather();
        assertNotNull(loadedWeather);
        assertNotNull(loadedWeather.getMain());
        assertEquals(22.5, loadedWeather.getMain().getTemp(), 0.001);
        assertEquals(1013, loadedWeather.getMain().getPressure());
        assertEquals(65, loadedWeather.getMain().getHumidity());
        assertEquals("Moscow", loadedWeather.getName());
    }

    @Test
    public void saveAndLoadGeo_works() throws Exception {
        WeatherResponse weather = createWeatherResponse(22.5, 1013, 65);
        GeoLocation geo = createGeoLocation(55.75, 37.61, "Moscow", "RU");
        List<ForecastItem> hourly = createForecastItems(2);

        storage.save(weather, geo, hourly);

        GeoLocation loadedGeo = storage.loadGeo();
        assertNotNull(loadedGeo);
        assertEquals(55.75, loadedGeo.lat, 0.001);
        assertEquals(37.61, loadedGeo.lon, 0.001);
        assertEquals("Moscow", loadedGeo.name);
        assertEquals("RU", loadedGeo.country);
    }

    @Test
    public void saveAndLoadHourly_works() throws Exception {
        WeatherResponse weather = createWeatherResponse(22.5, 1013, 65);
        GeoLocation geo = createGeoLocation(55.75, 37.61, "Moscow", "RU");
        List<ForecastItem> hourly = createForecastItems(3);

        storage.save(weather, geo, hourly);

        List<ForecastItem> loadedHourly = storage.loadHourly();
        assertNotNull(loadedHourly);
        assertEquals(3, loadedHourly.size());
        assertEquals(10.0, getFieldAsDouble(loadedHourly.get(0).main, "temp"), 0.001);
        assertEquals(15.0, getFieldAsDouble(loadedHourly.get(1).main, "temp"), 0.001);
        assertEquals(20.0, getFieldAsDouble(loadedHourly.get(2).main, "temp"), 0.001);
    }

    @Test
    public void saveWithNulls_doesNotCrash() {
        // Should not throw exception
        storage.save(null, null, null);

        assertNull(storage.loadWeather());
        assertNull(storage.loadGeo());
        assertTrue(storage.loadHourly().isEmpty());
    }

    @Test
    public void saveWithPartialNulls_works() throws Exception {
        WeatherResponse weather = createWeatherResponse(22.5, 1013, 65);
        storage.save(weather, null, null);

        assertNotNull(storage.loadWeather());
        assertNull(storage.loadGeo());
        assertTrue(storage.loadHourly().isEmpty());
    }

    @Test
    public void overwrite_savesNewData() throws Exception {
        WeatherResponse weather1 = createWeatherResponse(20.0, 1010, 50, "Moscow");
        GeoLocation geo1 = createGeoLocation(55.75, 37.61, "Moscow", "RU");
        storage.save(weather1, geo1, createForecastItems(1));

        WeatherResponse weather2 = createWeatherResponse(25.0, 1020, 70, "Saint Petersburg");
        GeoLocation geo2 = createGeoLocation(59.93, 30.31, "Saint Petersburg", "RU");
        storage.save(weather2, geo2, createForecastItems(2));

        WeatherResponse loaded = storage.loadWeather();
        assertEquals(25.0, loaded.getMain().getTemp(), 0.001);
        assertEquals("Saint Petersburg", loaded.getName());
    }

    private WeatherResponse createWeatherResponse(double temp, int pressure, int humidity, String name) throws Exception {
        WeatherResponse w = new WeatherResponse();
        WeatherResponse.Main main = new WeatherResponse.Main();
        setField(main, "temp", temp);
        setField(main, "pressure", pressure);
        setField(main, "humidity", humidity);
        setField(w, "main", main);
        setField(w, "name", name);
        return w;
    }

    private WeatherResponse createWeatherResponse(double temp, int pressure, int humidity) throws Exception {
        return createWeatherResponse(temp, pressure, humidity, "Test City");
    }

    private GeoLocation createGeoLocation(double lat, double lon, String name, String country) {
        GeoLocation g = new GeoLocation();
        g.lat = lat;
        g.lon = lon;
        g.name = name;
        g.country = country;
        return g;
    }

    private List<ForecastItem> createForecastItems(int count) throws Exception {
        List<ForecastItem> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ForecastItem item = new ForecastItem();
            setField(item, "timestamp", 1000L + i * 3600);
            ForecastItem.Main main = new ForecastItem.Main();
            setField(main, "temp", 10.0 + i * 5.0);
            setField(main, "humidity", 60 + i);
            setField(main, "pressure", 1010 + i);
            setField(item, "main", main);
            items.add(item);
        }
        return items;
    }

    private double getFieldAsDouble(Object obj, String fieldName) throws Exception {
        return (double) getField(obj, fieldName);
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    private Object getField(Object obj, String fieldName) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
}