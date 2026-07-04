package com.home.myweather.data.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class GeoLocationTest {

    @Test
    public void fields_canBeSetAndRetrieved() {
        GeoLocation geo = new GeoLocation();

        geo.name = "Moscow";
        geo.lat = 55.7558;
        geo.lon = 37.6173;
        geo.country = "RU";
        geo.state = "Moscow";

        assertEquals("Moscow", geo.name);
        assertEquals(55.7558, geo.lat, 0.0001);
        assertEquals(37.6173, geo.lon, 0.0001);
        assertEquals("RU", geo.country);
        assertEquals("Moscow", geo.state);
    }

    @Test
    public void fields_defaultToNullOrZero() {
        GeoLocation geo = new GeoLocation();

        assertNull(geo.name);
        assertEquals(0.0, geo.lat, 0.0);
        assertEquals(0.0, geo.lon, 0.0);
        assertNull(geo.country);
        assertNull(geo.state);
    }

    @Test
    public void isSerializable() {
        GeoLocation geo = new GeoLocation();
        assertTrue("GeoLocation should implement Serializable", geo instanceof java.io.Serializable);
    }

    @Test
    public void canCreateMultipleInstances() {
        GeoLocation geo1 = new GeoLocation();
        geo1.name = "London";
        geo1.lat = 51.5074;
        geo1.lon = -0.1278;
        geo1.country = "GB";

        GeoLocation geo2 = new GeoLocation();
        geo2.name = "Paris";
        geo2.lat = 48.8566;
        geo2.lon = 2.3522;
        geo2.country = "FR";

        assertEquals("London", geo1.name);
        assertEquals("Paris", geo2.name);
        assertNotEquals(geo1.lat, geo2.lat, 0.0001);
    }
}