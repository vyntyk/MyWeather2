package com.home.myweather.utils;

import java.util.Locale;
import com.home.myweather.data.model.ForecastItem;

/**
 * Форматирует поля ForecastItem (Open-Meteo модель) в читаемые строки для UI.
 * Использует TemperatureConverter, PressureConverter и другие утилиты.
 * Чистая утилита — никаких зависимостей на Android.
 */
public final class ForecastFormatter {

    private ForecastFormatter() {}

    /**
     * Форматирует температуру ForecastItem с учётом единицы измерения.
     */
    public static String temperature(ForecastItem item, String unit) {
        if (item == null || item.main == null) return "—";
        return "Температура: " + TemperatureConverter.format(item.main.temp, unit);
    }

    /**
     * Форматирует "ощущаемую" температуру.
     */
    public static String feelsLike(ForecastItem item, String unit) {
        if (item == null || item.main == null) return "—";
        return TemperatureConverter.formatFeelsLike(item.main.feelsLike, unit);
    }

    /**
     * Форматирует диапазон температур (мин/макс).
     */
    public static String temperatureRange(double minTemp, double maxTemp, String unit) {
        return TemperatureConverter.formatRange(minTemp, maxTemp, unit);
    }

    /**
     * Форматирует ветер.
     */
    public static String wind(ForecastItem item) {
        if (item == null || item.wind == null) return "—";
        return String.format(Locale.getDefault(), "Ветер: %.1f м/с", item.wind.speed);
    }

    /**
     * Форматирует давление в мм рт. ст.
     */
    public static String pressure(ForecastItem item) {
        if (item == null || item.main == null) return "—";
        int mmHg = PressureConverter.toMmHg((int) item.main.pressure);
        return String.format(Locale.getDefault(), "Давление: %d мм рт. ст.", mmHg);
    }

    /**
     * Форматирует давление в гПа.
     */
    public static String pressureHpa(ForecastItem item) {
        if (item == null || item.main == null) return "—";
        return String.format(Locale.getDefault(), "Давление: %.0f гПа", item.main.pressure);
    }

    /**
     * Форматирует влажность.
     */
    public static String humidity(ForecastItem item) {
        if (item == null || item.main == null) return "—";
        return String.format(Locale.getDefault(), "Влажность: %d%%", item.main.humidity);
    }

    /**
     * Форматирует вероятность осадков.
     */
    public static String precipitationProbability(ForecastItem item) {
        if (item == null) return "—";
        return String.format(Locale.getDefault(), "Осадки: %.0f%%", item.pop * 100);
    }

    /**
     * Форматирует описание погоды.
     */
    public static String description(ForecastItem item) {
        if (item == null || item.weather == null || item.weather.length == 0) return "—";
        String desc = item.weather[0].description;
        return (desc != null && !desc.isEmpty()) ? desc : "—";
    }

    /**
     * Форматирует видимость в км.
     */
    public static String visibility(ForecastItem item) {
        if (item == null || item.visibility <= 0) return "—";
        return String.format(Locale.getDefault(), "Видимость: %.1f км", item.visibility / 1000.0);
    }

    /**
     * Форматирует облачность.
     */
    public static String cloudCover(ForecastItem item) {
        if (item == null) return "—";
        return String.format(Locale.getDefault(), "Облачность: %d%%", item.clouds);
    }
}
