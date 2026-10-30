package com.home.myweather.utils;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class TemperatureConverterTest {

    @Test
    public void format_celsius_returnsWithDegree() {
        String result = TemperatureConverter.format(20.0, "C");
        assertThat(result).contains("20");
        assertThat(result).contains("°");
    }

    @Test
    public void format_fahrenheit_convertsAndFormats() {
        String result = TemperatureConverter.format(20.0, "F");
        assertThat(result).contains("68");
        assertThat(result).contains("°");
    }

    @Test
    public void format_kelvin_convertsAndFormats() {
        String result = TemperatureConverter.format(20.0, "K");
        assertThat(result).contains("293");
        assertThat(result).contains("°");
    }

    @Test
    public void format_negativeCelsius_formatsCorrectly() {
        String result = TemperatureConverter.format(-10.0, "C");
        assertThat(result).contains("-10");
    }

    @Test
    public void formatRange_celsius_formatsBoth() {
        String result = TemperatureConverter.formatRange(5.0, 15.0, "C");
        assertThat(result).contains("5");
        assertThat(result).contains("15");
    }

    @Test
    public void formatRange_sameMinMax_showsSingle() {
        String result = TemperatureConverter.formatRange(10.0, 10.0, "C");
        assertThat(result).contains("10");
    }

    @Test
    public void format_fractional_roundsCorrectly() {
        String result = TemperatureConverter.format(20.6, "C");
        assertThat(result).contains("21");
        
        result = TemperatureConverter.format(20.4, "C");
        assertThat(result).contains("20");
    }

    @Test
    public void format_unknownUnit_defaultsToCelsius() {
        String result = TemperatureConverter.format(20.0, "X");
        assertThat(result).contains("20");
    }

    @Test
    public void format_zero_handlesCorrectly() {
        String result = TemperatureConverter.format(0.0, "C");
        assertThat(result).contains("0");
    }
}