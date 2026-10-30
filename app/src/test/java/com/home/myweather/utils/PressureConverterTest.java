package com.home.myweather.utils;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class PressureConverterTest {

    @Test
    public void toMmHg_standardPressure_convertsCorrectly() {
        // 1013.25 hPa = 760 mmHg
        int result = PressureConverter.toMmHg(1013);
        assertThat(result).isEqualTo(760);
    }

    @Test
    public void toMmHg_lowPressure_convertsCorrectly() {
        int result = PressureConverter.toMmHg(980);
        assertThat(result).isEqualTo(735);
    }

    @Test
    public void toMmHg_highPressure_convertsCorrectly() {
        int result = PressureConverter.toMmHg(1040);
        assertThat(result).isEqualTo(780);
    }

    @Test
    public void toMmHg_zero_returnsZero() {
        int result = PressureConverter.toMmHg(0);
        assertThat(result).isEqualTo(0);
    }

    @Test
    public void toMmHg_negative_returnsNegative() {
        int result = PressureConverter.toMmHg(-100);
        assertThat(result).isLessThan(0);
    }

    @Test
    public void toMmHg_roundsCorrectly() {
        // 1013.25 hPa = 760.0 mmHg
        int result = PressureConverter.toMmHg(1013);
        assertThat(result).isEqualTo(760);
        
        // 1014 hPa ≈ 760.5 mmHg → rounds to 761
        result = PressureConverter.toMmHg(1014);
        assertThat(result).isEqualTo(761);
    }

    @Test
    public void toMmHg_precision_isInteger() {
        int result = PressureConverter.toMmHg(1013);
        assertThat(result).isInstanceOf(Integer.class);
    }
}