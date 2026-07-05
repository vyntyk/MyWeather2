package com.home.myweather.data.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class WeatherResponseTest {

    @Test
    public void getters_returnCorrectValues() throws Exception {
        WeatherResponse response = new WeatherResponse();

        // Set up nested objects using reflection
        WeatherResponse.Coord coord = new WeatherResponse.Coord();
        setField(coord, "lat", 55.75);
        setField(coord, "lon", 37.61);
        setField(response, "coord", coord);

        WeatherResponse.Main main = new WeatherResponse.Main();
        setField(main, "temp", 20.0);
        setField(main, "feelsLike", 19.5);
        setField(main, "tempMin", 18.0);
        setField(main, "tempMax", 22.0);
        setField(main, "pressure", 1013);
        setField(main, "humidity", 60);
        setField(response, "main", main);

        WeatherResponse.Wind wind = new WeatherResponse.Wind();
        setField(wind, "speed", 4.0);
        setField(wind, "deg", 225);
        setField(response, "wind", wind);

        WeatherResponse.WeatherCondition[] conditions = new WeatherResponse.WeatherCondition[1];
        conditions[0] = new WeatherResponse.WeatherCondition();
        setField(conditions[0], "id", 801);
        setField(conditions[0], "main", "Clouds");
        setField(conditions[0], "description", "небольшая облачность");
        setField(conditions[0], "icon", "02d");
        setField(response, "weather", conditions);

        setField(response, "dt", 1700000000L);
        setField(response, "name", "Moscow");
        setField(response, "visibility", 10000);
        setField(response, "timezone", 10800);

        WeatherResponse.Sys sys = new WeatherResponse.Sys();
        setField(sys, "sunrise", 1699930000L);
        setField(sys, "sunset", 1699960000L);
        setField(sys, "country", "RU");
        setField(response, "sys", sys);

        // Test getters
        assertEquals(55.75, response.getCoord().getLat(), 0.001);
        assertEquals(37.61, response.getCoord().getLon(), 0.001);

        assertEquals(20.0, response.getMain().getTemp(), 0.001);
        assertEquals(19.5, response.getMain().getFeelsLike(), 0.001);
        assertEquals(18.0, response.getMain().getTempMin(), 0.001);
        assertEquals(22.0, response.getMain().getTempMax(), 0.001);
        assertEquals(1013, response.getMain().getPressure());
        assertEquals(60, response.getMain().getHumidity());

        assertEquals(4.0, response.getWind().getSpeed(), 0.001);
        assertEquals(225, response.getWind().getDeg());

        assertEquals(801, response.getWeather()[0].getId());
        assertEquals("Clouds", response.getWeather()[0].getMain());
        assertEquals("небольшая облачность", response.getWeather()[0].getDescription());
        assertEquals("02d", response.getWeather()[0].getIcon());

        assertEquals(1700000000L, response.getDt());
        assertEquals("Moscow", response.getName());
        assertEquals(10000, response.getVisibility().intValue());
        assertEquals(10800, response.getTimezone());

        assertEquals(1699930000L, response.getSys().getSunrise());
        assertEquals(1699960000L, response.getSys().getSunset());
        assertEquals("RU", response.getSys().getCountry());
    }

    @Test
    public void getters_returnNullWhenFieldsNotSet() {
        WeatherResponse response = new WeatherResponse();

        assertNull(response.getCoord());
        assertNull(response.getMain());
        assertNull(response.getWind());
        assertNull(response.getWeather());
        assertNull(response.getClouds());
        assertNull(response.getSys());
        assertEquals(0L, response.getDt());
        assertNull(response.getName());
        assertNull(response.getVisibility());
        assertEquals(0, response.getTimezone());
    }

    @Test
    public void isSerializable() {
        WeatherResponse response = new WeatherResponse();
        assertTrue("WeatherResponse should implement Serializable", response instanceof java.io.Serializable);
    }

    @Test
    public void nestedClasses_areSerializable() {
        assertTrue(new WeatherResponse.Coord() instanceof java.io.Serializable);
        assertTrue(new WeatherResponse.Main() instanceof java.io.Serializable);
        assertTrue(new WeatherResponse.Wind() instanceof java.io.Serializable);
        assertTrue(new WeatherResponse.Clouds() instanceof java.io.Serializable);
        assertTrue(new WeatherResponse.Sys() instanceof java.io.Serializable);
        assertTrue(new WeatherResponse.WeatherCondition() instanceof java.io.Serializable);
    }

    // Helper methods using reflection
    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}