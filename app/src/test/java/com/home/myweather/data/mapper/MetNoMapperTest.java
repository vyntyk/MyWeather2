package com.home.myweather.data.mapper;

import com.google.gson.Gson;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.model.MetNoForecastResponse;
import com.home.myweather.data.model.WeatherResponse;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MetNoMapperTest {

    private static final String RESPONSE = "{"
            + "\"properties\":{\"timeseries\":["
            + "{\"time\":\"2026-10-22T12:00:00Z\",\"data\":{"
            + "\"instant\":{\"details\":{"
            + "\"air_pressure_at_sea_level\":1027.4,"
            + "\"air_temperature\":14.6,"
            + "\"cloud_area_fraction\":99.2,"
            + "\"relative_humidity\":52.6,"
            + "\"wind_from_direction\":110.8,"
            + "\"wind_speed\":4.8}},"
            + "\"next_1_hours\":{\"summary\":{\"symbol_code\":\"partlycloudy_day\"},"
            + "\"details\":{\"probability_of_precipitation\":20.0}}"
            + "}}]}}";

    @Test
    public void mapsCurrentWeatherAndHourlyForecast() {
        MetNoForecastResponse response =
                new Gson().fromJson(RESPONSE, MetNoForecastResponse.class);
        GeoLocation geo = new GeoLocation();
        geo.lat = 51.7412;
        geo.lon = 36.1998;
        geo.name = "Kursk";

        WeatherResponse current = MetNoMapper.toWeatherResponse(response, geo);
        List<com.home.myweather.data.model.ForecastItem> forecast =
                MetNoMapper.toForecastItems(response);

        assertNotNull(current);
        assertEquals(14.6, current.main.temp, 0.001);
        assertEquals(1027, current.main.pressure);
        assertEquals("Kursk", current.name);
        assertEquals(1, forecast.size());
        assertEquals(14.6, forecast.get(0).main.temp, 0.001);
        assertEquals(0.2, forecast.get(0).pop, 0.001);
        assertEquals("02d", forecast.get(0).weather[0].icon);
    }
}
