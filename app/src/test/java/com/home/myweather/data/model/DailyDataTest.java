package com.home.myweather.data.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class DailyDataTest {

    @Test
    public void constructor_initializesEmptyItemsList() {
        DailyData day = new DailyData();

        assertNotNull(day.items);
        assertTrue(day.items.isEmpty());
    }

    @Test
    public void fields_canBeSetAndRetrieved() throws Exception {
        DailyData day = new DailyData();

        day.dateMillis = 1700000000000L;
        day.tempMin = 10.0;
        day.tempMax = 20.0;
        day.description = "ясно";
        day.pop = 0.3;

        ForecastItem item = new ForecastItem();
        setField(item, "timestamp", 1700000000L);
        ForecastItem.Main main = new ForecastItem.Main();
        setField(main, "temp", 15.0);
        setField(item, "main", main);
        day.items = new ArrayList<>();
        day.items.add(item);

        assertEquals(1700000000000L, day.dateMillis);
        assertEquals(10.0, day.tempMin, 0.001);
        assertEquals(20.0, day.tempMax, 0.001);
        assertEquals("ясно", day.description);
        assertEquals(0.3, day.pop, 0.001);
        assertEquals(1, day.items.size());
    }

    @Test
    public void isSerializable() {
        DailyData day = new DailyData();
        assertTrue("DailyData should implement Serializable", day instanceof java.io.Serializable);
    }

    @Test
    public void items_canHoldMultipleForecastItems() throws Exception {
        DailyData day = new DailyData();
        List<ForecastItem> items = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            ForecastItem item = new ForecastItem();
            setField(item, "timestamp", 1700000000L + i * 10800);
            ForecastItem.Main main = new ForecastItem.Main();
            setField(main, "temp", 10.0 + i);
            setField(item, "main", main);
            items.add(item);
        }
        day.items = items;

        assertEquals(8, day.items.size());
        assertEquals(10.0, ((Number) getField(day.items.get(0).main, "temp")).doubleValue(), 0.001);
        assertEquals(17.0, ((Number) getField(day.items.get(7).main, "temp")).doubleValue(), 0.001);
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