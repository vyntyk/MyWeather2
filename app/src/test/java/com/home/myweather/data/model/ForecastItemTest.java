package com.home.myweather.data.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class ForecastItemTest {

    @Test
    public void mainFields_canBeSetAndRetrieved() throws Exception {
        ForecastItem item = new ForecastItem();

        setField(item, "timestamp", 1700000000L);
        setField(item, "dtText", "2024-01-01 12:00:00");
        setField(item, "pop", 0.5);
        setField(item, "visibility", 10000);

        ForecastItem.Main main = new ForecastItem.Main();
        setField(main, "temp", 15.0);
        setField(main, "feelsLike", 14.0);
        setField(main, "tempMin", 12.0);
        setField(main, "tempMax", 18.0);
        setField(main, "pressure", 1013);
        setField(main, "humidity", 65);
        setField(item, "main", main);

        ForecastItem.WeatherCondition[] weather = new ForecastItem.WeatherCondition[1];
        weather[0] = new ForecastItem.WeatherCondition();
        setField(weather[0], "id", 800);
        setField(weather[0], "main", "Clear");
        setField(weather[0], "description", "ясно");
        setField(weather[0], "icon", "01d");
        setField(item, "weather", weather);

        ForecastItem.Wind wind = new ForecastItem.Wind();
        setField(wind, "speed", 3.5);
        setField(wind, "deg", 180);
        setField(item, "wind", wind);

        ForecastItem.Clouds clouds = new ForecastItem.Clouds();
        setField(clouds, "all", 10);
        setField(item, "clouds", clouds);

        assertEquals(1700000000L, getField(item, "timestamp"));
        assertEquals("2024-01-01 12:00:00", getField(item, "dtText"));
        assertEquals(0.5, ((Number) getField(item, "pop")).doubleValue(), 0.001);
        assertEquals(10000, getField(item, "visibility"));

        Object mainObj = getField(item, "main");
        assertEquals(15.0, ((Number) getField(mainObj, "temp")).doubleValue(), 0.001);
        assertEquals(14.0, ((Number) getField(mainObj, "feelsLike")).doubleValue(), 0.001);
        assertEquals(12.0, ((Number) getField(mainObj, "tempMin")).doubleValue(), 0.001);
        assertEquals(18.0, ((Number) getField(mainObj, "tempMax")).doubleValue(), 0.001);
        assertEquals(1013, getField(mainObj, "pressure"));
        assertEquals(65, getField(mainObj, "humidity"));

        Object[] weatherArr = (Object[]) getField(item, "weather");
        assertEquals(800, getField(weatherArr[0], "id"));
        assertEquals("Clear", getField(weatherArr[0], "main"));
        assertEquals("ясно", getField(weatherArr[0], "description"));
        assertEquals("01d", getField(weatherArr[0], "icon"));

        Object windObj = getField(item, "wind");
        assertEquals(3.5, ((Number) getField(windObj, "speed")).doubleValue(), 0.001);
        assertEquals(180, getField(windObj, "deg"));

        Object cloudsObj = getField(item, "clouds");
        assertEquals(10, getField(cloudsObj, "all"));
    }

    @Test
    public void isSerializable() {
        ForecastItem item = new ForecastItem();
        assertTrue("ForecastItem should implement Serializable", item instanceof java.io.Serializable);
    }

    @Test
    public void mainClass_isSerializable() {
        ForecastItem.Main main = new ForecastItem.Main();
        assertTrue("ForecastItem.Main should implement Serializable", main instanceof java.io.Serializable);
    }

    @Test
    public void weatherCondition_isSerializable() {
        ForecastItem.WeatherCondition wc = new ForecastItem.WeatherCondition();
        assertTrue("ForecastItem.WeatherCondition should implement Serializable", wc instanceof java.io.Serializable);
    }

    @Test
    public void wind_isSerializable() {
        ForecastItem.Wind wind = new ForecastItem.Wind();
        assertTrue("ForecastItem.Wind should implement Serializable", wind instanceof java.io.Serializable);
    }

    @Test
    public void clouds_isSerializable() {
        ForecastItem.Clouds clouds = new ForecastItem.Clouds();
        assertTrue("ForecastItem.Clouds should implement Serializable", clouds instanceof java.io.Serializable);
    }

    // Helper methods using reflection
    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    private Object getField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
}