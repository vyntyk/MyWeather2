package com.home.myweather.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class WeatherIconTest {

    @Test
    public void getResId_clearDay_returnsCorrectResource() {
        int resId = WeatherIcon.getResId("01d");
        // 01d is also the default, so just verify it returns a valid resource ID (not 0)
        assertNotEquals("Should return a valid resource", 0, resId);
    }

    @Test
    public void getResId_clearNight_returnsCorrectResource() {
        int resId = WeatherIcon.getResId("01n");
        assertNotEquals("Should return a valid resource", 0, resId);
    }

    @Test
    public void getResId_fewCloudsDay_returnsCorrectResource() {
        int resId = WeatherIcon.getResId("02d");
        int resId01d = WeatherIcon.getResId("01d");
        assertNotEquals("02d should return different resource than 01d", resId01d, resId);
    }

    @Test
    public void getResId_fewCloudsNight_returnsCorrectResource() {
        int resId = WeatherIcon.getResId("02n");
        int resId01n = WeatherIcon.getResId("01n");
        assertNotEquals("02n should return different resource than 01n", resId01n, resId);
    }

    @Test
    public void getResId_scatteredClouds_returnsCorrectResource() {
        int resId03d = WeatherIcon.getResId("03d");
        int resId03n = WeatherIcon.getResId("03n");
        int resId01d = WeatherIcon.getResId("01d");
        assertNotEquals("03d should return different resource than 01d", resId01d, resId03d);
        assertNotEquals("03n should return different resource than 01d", resId01d, resId03n);
    }

    @Test
    public void getResId_brokenClouds_returnsCorrectResource() {
        int resId04d = WeatherIcon.getResId("04d");
        int resId04n = WeatherIcon.getResId("04n");
        int resId01d = WeatherIcon.getResId("01d");
        assertNotEquals("04d should return different resource than 01d", resId01d, resId04d);
        assertNotEquals("04n should return different resource than 01d", resId01d, resId04n);
    }

    @Test
    public void getResId_showerRain_returnsCorrectResource() {
        int resId09d = WeatherIcon.getResId("09d");
        int resId09n = WeatherIcon.getResId("09n");
        int resId01d = WeatherIcon.getResId("01d");
        assertNotEquals("09d should return different resource than 01d", resId01d, resId09d);
        assertNotEquals("09n should return different resource than 01d", resId01d, resId09n);
    }

    @Test
    public void getResId_rain_returnsCorrectResource() {
        int resId10d = WeatherIcon.getResId("10d");
        int resId10n = WeatherIcon.getResId("10n");
        int resId01d = WeatherIcon.getResId("01d");
        assertNotEquals("10d should return different resource than 01d", resId01d, resId10d);
        assertNotEquals("10n should return different resource than 01d", resId01d, resId10n);
    }

    @Test
    public void getResId_thunderstorm_returnsCorrectResource() {
        int resId11d = WeatherIcon.getResId("11d");
        int resId11n = WeatherIcon.getResId("11n");
        int resId01d = WeatherIcon.getResId("01d");
        assertNotEquals("11d should return different resource than 01d", resId01d, resId11d);
        assertNotEquals("11n should return different resource than 01d", resId01d, resId11n);
    }

    @Test
    public void getResId_snow_returnsCorrectResource() {
        int resId13d = WeatherIcon.getResId("13d");
        int resId13n = WeatherIcon.getResId("13n");
        int resId01d = WeatherIcon.getResId("01d");
        assertNotEquals("13d should return different resource than 01d", resId01d, resId13d);
        assertNotEquals("13n should return different resource than 01d", resId01d, resId13n);
    }

    @Test
    public void getResId_mist_returnsCorrectResource() {
        int resId50d = WeatherIcon.getResId("50d");
        int resId50n = WeatherIcon.getResId("50n");
        int resId01d = WeatherIcon.getResId("01d");
        assertNotEquals("50d should return different resource than 01d", resId01d, resId50d);
        assertNotEquals("50n should return different resource than 01d", resId01d, resId50n);
    }

    @Test
    public void getResId_null_returnsDefault() {
        int resId = WeatherIcon.getResId(null);
        int defaultResId = WeatherIcon.getResId("unknown");
        assertEquals("Should return default for null", defaultResId, resId);
    }

    @Test
    public void getResId_unknownCode_returnsDefault() {
        int resId = WeatherIcon.getResId("99x");
        int defaultResId = WeatherIcon.getResId("unknown");
        assertEquals("Should return default for unknown code", defaultResId, resId);
    }

    @Test
    public void getResId_emptyString_returnsDefault() {
        int resId = WeatherIcon.getResId("");
        int defaultResId = WeatherIcon.getResId("unknown");
        assertEquals("Should return default for empty string", defaultResId, resId);
    }

    @Test
    public void getResId_allKnownCodes_returnNonDefault() {
        String[] knownCodes = {"01d", "01n", "02d", "02n", "03d", "03n",
                               "04d", "04n", "09d", "09n", "10d", "10n",
                               "11d", "11n", "13d", "13n", "50d", "50n"};
        int resId01d = WeatherIcon.getResId("01d");

        for (String code : knownCodes) {
            int resId = WeatherIcon.getResId(code);
            assertNotEquals("Code " + code + " should return a valid resource", 0, resId);
            // Most codes should differ from 01d, except 01d itself
            if (!code.equals("01d")) {
                assertNotEquals("Code " + code + " should return different resource than 01d", resId01d, resId);
            }
        }
    }
}