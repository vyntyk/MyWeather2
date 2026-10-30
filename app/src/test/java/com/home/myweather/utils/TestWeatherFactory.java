package com.home.myweather.utils;

import com.home.myweather.data.model.WeatherResponse;

import java.lang.reflect.Field;

/**
 * Утилита для создания тестовых объектов WeatherResponse с использованием рефлексии,
 * так как поля в модели приватные и доступны только через геттеры.
 */
public final class TestWeatherFactory {

    private TestWeatherFactory() {}

    public static WeatherResponse createWeather(double temp) throws Exception {
        WeatherResponse w = new WeatherResponse();
        WeatherResponse.Main main = createMain(temp);
        setPrivateField(w, "main", main);
        return w;
    }

    public static void setPressure(WeatherResponse weather, int pressure) throws Exception {
        Object main = getPrivateField(weather, "main");
        if (main == null) {
            main = createMain(0);
            setPrivateField(weather, "main", main);
        }
        setPrivateField(main, "pressure", pressure);
    }

    public static void setHumidity(WeatherResponse weather, int humidity) throws Exception {
        Object main = getPrivateField(weather, "main");
        if (main == null) {
            main = createMain(0);
            setPrivateField(weather, "main", main);
        }
        setPrivateField(main, "humidity", humidity);
    }

    public static void setWind(WeatherResponse weather, double speed) throws Exception {
        WeatherResponse.Wind wind = new WeatherResponse.Wind();
        setPrivateField(wind, "speed", speed);
        setPrivateField(weather, "wind", wind);
    }

    public static void setWeatherCondition(WeatherResponse weather, String description) throws Exception {
        WeatherResponse.WeatherCondition[] conditions = new WeatherResponse.WeatherCondition[1];
        conditions[0] = createCondition(description);
        setPrivateField(weather, "weather", conditions);
    }

    public static void setWeatherConditions(WeatherResponse weather, WeatherResponse.WeatherCondition[] conditions) throws Exception {
        setPrivateField(weather, "weather", conditions);
    }

    private static WeatherResponse.Main createMain(double temp) throws Exception {
        WeatherResponse.Main main = new WeatherResponse.Main();
        setPrivateField(main, "temp", temp);
        return main;
    }

    private static WeatherResponse.WeatherCondition createCondition(String description) throws Exception {
        WeatherResponse.WeatherCondition condition = new WeatherResponse.WeatherCondition();
        if (description != null) {
            setPrivateField(condition, "description", description);
        }
        return condition;
    }

    static void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    static Object getPrivateField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    public static com.home.myweather.data.model.ForecastItem createForecastItem(
            long timestamp, double temp, double feelsLike, int humidity, int pressure,
            double windSpeed, int windDeg, double pop) {
        com.home.myweather.data.model.ForecastItem item = new com.home.myweather.data.model.ForecastItem();
        item.timestamp = timestamp;
        
        com.home.myweather.data.model.ForecastItem.Main main = new com.home.myweather.data.model.ForecastItem.Main();
        main.temp = temp;
        main.feelsLike = feelsLike;
        main.humidity = humidity;
        main.pressure = pressure;
        item.main = main;
        
        com.home.myweather.data.model.ForecastItem.Wind wind = new com.home.myweather.data.model.ForecastItem.Wind();
        wind.speed = windSpeed;
        wind.deg = windDeg;
        item.wind = wind;
        
        item.pop = pop;
        
        com.home.myweather.data.model.ForecastItem.WeatherCondition[] conditions = 
            new com.home.myweather.data.model.ForecastItem.WeatherCondition[1];
        com.home.myweather.data.model.ForecastItem.WeatherCondition condition = 
            new com.home.myweather.data.model.ForecastItem.WeatherCondition();
        condition.id = 800;
        condition.main = "Clear";
        condition.description = "Clear sky";
        condition.icon = "01d";
        conditions[0] = condition;
        item.weather = conditions;
        
        return item;
    }
}