package com.home.myweather;

import java.util.Locale;

/**
 * Форматирует поля WeatherResponse в читаемые строки для UI.
 * Чистая утилита — никаких зависимостей на Android.
 */
public final class WeatherFormatter {

    private WeatherFormatter() {}

    public static String temperature(WeatherResponse w) {
        if (w.getMain() == null) return "—";
        return String.format(Locale.getDefault(), "Температура: %.1f°C", w.getMain().getTemp());
    }

    public static String wind(WeatherResponse w) {
        if (w.getWind() == null) return "Ветер: нет данных";
        return String.format(Locale.getDefault(), "Ветер: %.1f м/с", w.getWind().getSpeed());
    }

    public static String pressure(WeatherResponse w) {
        if (w.getMain() == null) return "—";
        return String.format(Locale.getDefault(), "Давление: %d гПа", w.getMain().getPressure());
    }

    public static String humidity(WeatherResponse w) {
        if (w.getMain() == null) return "—";
        return String.format(Locale.getDefault(), "Влажность: %d%%", w.getMain().getHumidity());
    }

    public static String description(WeatherResponse w) {
        WeatherResponse.WeatherCondition[] c = w.getWeather();
        if (c == null || c.length == 0 || c[0] == null) return "—";
        String desc = c[0].getDescription();
        return (desc != null && !desc.isEmpty()) ? desc : "—";
    }
}
