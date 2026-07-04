package com.home.myweather.utils;

import com.home.myweather.data.model.WeatherResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class WeatherFormatterTest {

    @Test
    public void temperature_Celsius_returnsFormattedString() throws Exception {
        WeatherResponse w = TestWeatherFactory.createWeather(22.5);
        String result = WeatherFormatter.temperature(w, "C");
        String expected = String.format(java.util.Locale.getDefault(), "Температура: %.1f°C", 22.5);
        assertEquals(expected, result);
    }

    @Test
    public void temperature_Fahrenheit_convertsAndFormats() throws Exception {
        WeatherResponse w = TestWeatherFactory.createWeather(22.5); // 22.5°C = 72.5°F
        String result = WeatherFormatter.temperature(w, "F");
        String expected = String.format(java.util.Locale.getDefault(), "Температура: %.1f°F", 72.5);
        assertEquals(expected, result);
    }

    @Test
    public void temperature_nullMain_returnsDash() {
        WeatherResponse w = new WeatherResponse();
        String result = WeatherFormatter.temperature(w, "C");
        assertEquals("—", result);
    }

    @Test
    public void wind_withData_returnsFormattedString() throws Exception {
        WeatherResponse w = TestWeatherFactory.createWeather(0);
        TestWeatherFactory.setWind(w, 5.5);
        String result = WeatherFormatter.wind(w);
        String expected = String.format(java.util.Locale.getDefault(), "Ветер: %.1f м/с", 5.5);
        assertEquals(expected, result);
    }

    @Test
    public void wind_nullWind_returnsNoData() {
        WeatherResponse w = new WeatherResponse();
        String result = WeatherFormatter.wind(w);
        assertEquals("Ветер: нет данных", result);
    }

    @Test
    public void pressure_withData_returnsFormattedString() throws Exception {
        WeatherResponse w = TestWeatherFactory.createWeather(0);
        TestWeatherFactory.setPressure(w, 1013);
        String result = WeatherFormatter.pressure(w);
        assertEquals("Давление: 1013 гПа", result);
    }

    @Test
    public void pressure_nullMain_returnsDash() {
        WeatherResponse w = new WeatherResponse();
        String result = WeatherFormatter.pressure(w);
        assertEquals("—", result);
    }

    @Test
    public void humidity_withData_returnsFormattedString() throws Exception {
        WeatherResponse w = TestWeatherFactory.createWeather(0);
        TestWeatherFactory.setHumidity(w, 65);
        String result = WeatherFormatter.humidity(w);
        assertEquals("Влажность: 65%", result);
    }

    @Test
    public void humidity_nullMain_returnsDash() {
        WeatherResponse w = new WeatherResponse();
        String result = WeatherFormatter.humidity(w);
        assertEquals("—", result);
    }

    @Test
    public void description_withData_returnsDescription() throws Exception {
        WeatherResponse w = TestWeatherFactory.createWeather(0);
        TestWeatherFactory.setWeatherCondition(w, "ясно");
        String result = WeatherFormatter.description(w);
        assertEquals("ясно", result);
    }

    @Test
    public void description_nullWeather_returnsDash() {
        WeatherResponse w = new WeatherResponse();
        String result = WeatherFormatter.description(w);
        assertEquals("—", result);
    }

    @Test
    public void description_emptyArray_returnsDash() throws Exception {
        WeatherResponse w = TestWeatherFactory.createWeather(0);
        TestWeatherFactory.setWeatherConditions(w, new WeatherResponse.WeatherCondition[0]);
        String result = WeatherFormatter.description(w);
        assertEquals("—", result);
    }

    @Test
    public void description_nullDescription_returnsDash() throws Exception {
        WeatherResponse w = TestWeatherFactory.createWeather(0);
        TestWeatherFactory.setWeatherCondition(w, null);
        String result = WeatherFormatter.description(w);
        assertEquals("—", result);
    }
}