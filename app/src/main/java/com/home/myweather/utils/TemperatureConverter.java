package com.home.myweather.utils;

import java.util.Locale;

/**
 * Конвертация и форматирование температуры.
 */
public final class TemperatureConverter {

    private TemperatureConverter() {}

    public static double toDisplay(double celsius, String unit) {
        return "F".equals(unit) ? celsius * 9 / 5 + 32 : celsius;
    }

    public static String format(double celsius, String unit) {
        double value = toDisplay(celsius, unit);
        String suffix = "F".equals(unit) ? "°F" : "°C";
        return String.format(Locale.US, "%.1f%s", value, suffix);
    }

    public static String formatWhole(double celsius, String unit) {
        double value = toDisplay(celsius, unit);
        String suffix = "F".equals(unit) ? "°F" : "°C";
        return String.format(Locale.US, "%.0f%s", value, suffix);
    }

    public static String formatRange(double minCelsius, double maxCelsius, String unit) {
        double min = toDisplay(minCelsius, unit);
        double max = toDisplay(maxCelsius, unit);
        String suffix = "F".equals(unit) ? "°F" : "°C";
        return String.format(Locale.US, "%.0f%s / %.0f%s", min, suffix, max, suffix);
    }

    public static String formatFeelsLike(double celsius, String unit) {
        return "Ощущается: " + format(celsius, unit);
    }
}
