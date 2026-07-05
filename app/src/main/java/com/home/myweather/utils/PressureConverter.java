package com.home.myweather.utils;

/**
 * Конвертация атмосферного давления.
 */
public final class PressureConverter {

    public static final double HPA_TO_MMHG = 0.750062;

    private PressureConverter() {}

    public static int toMmHg(int hPa) {
        return (int) Math.round(hPa * HPA_TO_MMHG);
    }

    public static double toMmHg(double hPa) {
        return hPa * HPA_TO_MMHG;
    }
}
