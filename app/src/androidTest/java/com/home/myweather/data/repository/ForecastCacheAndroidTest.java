package com.home.myweather.data.repository;

import static org.junit.Assert.*;

import com.home.myweather.data.model.ForecastItem;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import androidx.test.ext.junit.runners.AndroidJUnit4;

/**
 * ForecastCache.get возвращает null, когда кэша нет, устарел или координаты не совпадают.
 */
@RunWith(AndroidJUnit4.class)
public class ForecastCacheAndroidTest {

    @Test
    public void putAndGet_sameCoordinates_returnsCachedList() {
        ForecastCache.clear();

        List<ForecastItem> items = new ArrayList<>();
        ForecastItem item1 = new ForecastItem();
        item1.timestamp = 1000;
        ForecastItem item2 = new ForecastItem();
        item2.timestamp = 2000;
        items.add(item1);
        items.add(item2);

        ForecastCache.put(55.75, 37.62, items);

        List<ForecastItem> cached = ForecastCache.get(55.75, 37.62);
        assertNotNull(cached);
        assertEquals(2, cached.size());
        assertEquals(1000, cached.get(0).timestamp);
        assertEquals(2000, cached.get(1).timestamp);
    }

    @Test
    public void get_differentCoordinates_returnsNull() {
        ForecastCache.clear();

        List<ForecastItem> items = new ArrayList<>();
        ForecastItem item = new ForecastItem();
        item.timestamp = 1000;
        items.add(item);

        ForecastCache.put(55.75, 37.62, items);

        assertNull(ForecastCache.get(55.90, 37.80));
    }

    @Test
    public void get_nearbyCoordinatesWithinEpsilon_returnsCachedList() {
        ForecastCache.clear();

        List<ForecastItem> items = new ArrayList<>();
        items.add(new ForecastItem());
        ForecastCache.put(55.75, 37.62, items);

        assertNotNull(ForecastCache.get(55.755, 37.625));
    }

    @Test
    public void clear_removesAllEntries() {
        List<ForecastItem> items = new ArrayList<>();
        ForecastItem item = new ForecastItem();
        item.timestamp = 1000;
        items.add(item);

        ForecastCache.put(55.75, 37.62, items);
        ForecastCache.clear();

        assertNull(ForecastCache.get(55.75, 37.62));
    }

    @Test
    public void get_emptyCache_returnsNull() {
        ForecastCache.clear();

        assertNull(ForecastCache.get(55.75, 37.62));
    }

    @Test
    public void put_overwritesPreviousValue() {
        ForecastCache.clear();

        List<ForecastItem> items1 = new ArrayList<>();
        ForecastItem item1 = new ForecastItem();
        item1.timestamp = 1000;
        items1.add(item1);
        ForecastCache.put(55.75, 37.62, items1);

        List<ForecastItem> items2 = new ArrayList<>();
        ForecastItem item2 = new ForecastItem();
        item2.timestamp = 2000;
        items2.add(item2);
        ForecastCache.put(55.75, 37.62, items2);

        List<ForecastItem> cached = ForecastCache.get(55.75, 37.62);
        assertNotNull(cached);
        assertEquals(1, cached.size());
        assertEquals(2000, cached.get(0).timestamp);
    }
}
