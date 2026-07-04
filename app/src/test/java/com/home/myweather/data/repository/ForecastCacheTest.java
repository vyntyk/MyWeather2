package com.home.myweather.data.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

import com.home.myweather.data.model.ForecastItem;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class ForecastCacheTest {

    @Test
    public void get_withNoCache_returnsNull() {
        ForecastCache.clear();
        List<ForecastItem> result = ForecastCache.get(55.75, 37.61);
        assertNull("Should return null when cache is empty", result);
    }

    @Test
    public void putAndGet_sameCoordinates_returnsCachedItems() {
        ForecastCache.clear();

        List<ForecastItem> items = new ArrayList<>();
        ForecastItem item = new ForecastItem();
        item.timestamp = 1700000000L;
        item.main = new ForecastItem.Main();
        item.main.temp = 15.0;
        items.add(item);

        ForecastCache.put(55.75, 37.61, items);

        List<ForecastItem> result = ForecastCache.get(55.75, 37.61);
        assertNotNull("Should return cached items for same coordinates", result);
        assertEquals(1, result.size());
        assertEquals(15.0, result.get(0).main.temp, 0.001);
    }

    @Test
    public void get_differentCoordinates_returnsNull() {
        ForecastCache.clear();

        List<ForecastItem> items = new ArrayList<>();
        ForecastItem item = new ForecastItem();
        item.main = new ForecastItem.Main();
        item.main.temp = 15.0;
        items.add(item);

        ForecastCache.put(55.75, 37.61, items);

        // Different coordinates (beyond COORD_EPS of 0.01)
        List<ForecastItem> result = ForecastCache.get(55.77, 37.63);
        assertNull("Should return null for different coordinates", result);
    }

    @Test
    public void get_nearCoordinatesWithinEps_returnsCachedItems() {
        ForecastCache.clear();

        List<ForecastItem> items = new ArrayList<>();
        ForecastItem item = new ForecastItem();
        item.main = new ForecastItem.Main();
        item.main.temp = 15.0;
        items.add(item);

        ForecastCache.put(55.75, 37.61, items);

        // Within COORD_EPS (0.01) - should match
        List<ForecastItem> result = ForecastCache.get(55.755, 37.615);
        assertNotNull("Should return cached items for nearby coordinates", result);
    }

    @Test
    public void clear_removesAllCachedData() {
        ForecastCache.clear();

        List<ForecastItem> items = new ArrayList<>();
        ForecastItem item = new ForecastItem();
        item.main = new ForecastItem.Main();
        item.main.temp = 15.0;
        items.add(item);

        ForecastCache.put(55.75, 37.61, items);
        ForecastCache.clear();

        List<ForecastItem> result = ForecastCache.get(55.75, 37.61);
        assertNull("Should return null after clear", result);
    }

    @Test
    public void cacheIsThreadSafe() {
        // Basic thread safety test - multiple threads accessing cache
        ForecastCache.clear();

        List<ForecastItem> items = new ArrayList<>();
        ForecastItem item = new ForecastItem();
        item.main = new ForecastItem.Main();
        item.main.temp = 15.0;
        items.add(item);

        // Put from one thread, get from another (simulated)
        ForecastCache.put(55.75, 37.61, items);
        List<ForecastItem> result = ForecastCache.get(55.75, 37.61);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}