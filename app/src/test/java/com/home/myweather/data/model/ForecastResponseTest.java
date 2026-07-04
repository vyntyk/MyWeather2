package com.home.myweather.data.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class ForecastResponseTest {

    @Test
    public void cityInfo_canBeSetAndRetrieved() throws Exception {
        ForecastResponse response = new ForecastResponse();
        response.city = new ForecastResponse.CityInfo();
        response.city.id = 12345;
        response.city.name = "Moscow";
        response.city.country = "RU";
        response.city.sunrise = 1699930000L;
        response.city.sunset = 1699960000L;
        response.city.coord = new GeoLocation();
        response.city.coord.lat = 55.75;
        response.city.coord.lon = 37.61;

        assertEquals(12345, response.city.id);
        assertEquals("Moscow", response.city.name);
        assertEquals("RU", response.city.country);
        assertEquals(1699930000L, response.city.sunrise);
        assertEquals(1699960000L, response.city.sunset);
        assertEquals(55.75, response.city.coord.lat, 0.001);
        assertEquals(37.61, response.city.coord.lon, 0.001);
    }

    @Test
    public void list_canHoldForecastHoldForecastItems() throws Exception {
        ForecastResponse response = new ForecastResponse();
        List<ForecastItem> items = new ArrayList<>();

        ForecastItem item1 = new ForecastItem();
        setField(item1, "timestamp", 1000L);
        ForecastItem.Main main1 = new ForecastItem.Main();
        setField(main1, "temp", 15.0);
        setField(item1, "main", main1);
        items.add(item1);

        ForecastItem item2 = new ForecastItem();
        setField(item2, "timestamp", 10000L);
        ForecastItem.Main main2 = new ForecastItem.Main();
        setField(main2, "temp", 18.0);
        setField(item2, "main", main2);
        items.add(item2);

        response.list = items;
        response.count = 2;

        assertEquals(2, response.list.size());
        assertEquals(2, response.count);
        assertEquals(15.0, ((Number) getField(response.list.get(0).main, "temp")).doubleValue(), 0.001);
        assertEquals(18.0, ((Number) getField(response.list.get(1).main, "temp")).doubleValue(), 0.001);
    }

    @Test
    public void isSerializable() {
        ForecastResponse response = new ForecastResponse();
        assertTrue("ForecastResponse should implement Serializable", response instanceof java.io.Serializable);
    }

    @Test
    public void cityInfo_isSerializable() {
        ForecastResponse.CityInfo city = new ForecastResponse.CityInfo();
        assertTrue("CityInfo should implement Serializable", city instanceof java.io.Serializable);
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