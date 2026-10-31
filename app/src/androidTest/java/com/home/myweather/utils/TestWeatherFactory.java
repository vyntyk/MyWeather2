package com.home.myweather.utils;

import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.data.model.WeatherResponse;

import java.lang.reflect.Field;

/**
 * Фабрика тестовых данных для инструментальных (androidTest) тестов.
 * Отдельная копия, потому что классы из src/test недоступны в наборе androidTest.
 */
public final class TestWeatherFactory {

    private TestWeatherFactory() {}

    /** Погода с заданной температурой (поля модели приватные, поэтому через рефлексию). */
    public static WeatherResponse createWeather(double temp) throws Exception {
        WeatherResponse weather = new WeatherResponse();
        WeatherResponse.Main main = new WeatherResponse.Main();
        setPrivateField(main, "temp", temp);
        setPrivateField(weather, "main", main);
        return weather;
    }

    public static ForecastItem createForecastItem(
            long timestamp, double temp, double feelsLike, int humidity, int pressure,
            double windSpeed, int windDeg, double pop) {
        ForecastItem item = new ForecastItem();
        item.timestamp = timestamp;

        ForecastItem.Main main = new ForecastItem.Main();
        main.temp = temp;
        main.feelsLike = feelsLike;
        main.humidity = humidity;
        main.pressure = pressure;
        item.main = main;

        ForecastItem.Wind wind = new ForecastItem.Wind();
        wind.speed = windSpeed;
        wind.deg = windDeg;
        item.wind = wind;

        item.pop = pop;

        ForecastItem.WeatherCondition condition = new ForecastItem.WeatherCondition();
        condition.id = 800;
        condition.main = "Clear";
        condition.description = "Clear sky";
        condition.icon = "01d";
        item.weather = new ForecastItem.WeatherCondition[]{condition};

        return item;
    }

    private static void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}
