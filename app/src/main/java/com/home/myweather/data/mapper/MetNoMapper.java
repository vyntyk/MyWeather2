package com.home.myweather.data.mapper;

import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.model.MetNoForecastResponse;
import com.home.myweather.data.model.WeatherResponse;
import com.home.myweather.utils.WmoWeatherCode;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MetNoMapper {

    private MetNoMapper() {
    }

    public static WeatherResponse toWeatherResponse(
            MetNoForecastResponse response,
            GeoLocation geo
    ) {
        MetNoForecastResponse.TimeSeries current = firstTimeSeries(response);
        if (current == null || current.data == null || current.data.instant == null
                || current.data.instant.details == null) {
            return null;
        }

        MetNoForecastResponse.Details details = current.data.instant.details;
        WeatherResponse weather = new WeatherResponse();
        weather.main = new WeatherResponse.Main();
        weather.main.temp = value(details.airTemperature);
        weather.main.feelsLike = weather.main.temp;
        weather.main.tempMin = weather.main.temp;
        weather.main.tempMax = weather.main.temp;
        weather.main.pressure = (int) Math.round(value(details.airPressure));
        weather.main.humidity = (int) Math.round(value(details.humidity));

        weather.wind = new WeatherResponse.Wind();
        weather.wind.speed = value(details.windSpeed);
        weather.wind.deg = (int) Math.round(value(details.windDirection));

        weather.clouds = new WeatherResponse.Clouds();
        weather.clouds.all = (int) Math.round(value(details.cloudCover));

        weather.coord = new WeatherResponse.Coord();
        weather.coord.lat = geo != null ? geo.lat : 0;
        weather.coord.lon = geo != null ? geo.lon : 0;

        String symbol = symbolCode(current.data);
        int code = toWmoCode(symbol);
        WeatherResponse.WeatherCondition condition = new WeatherResponse.WeatherCondition();
        condition.id = code;
        condition.main = WmoWeatherCode.getDescription(code);
        condition.description = condition.main;
        condition.icon = WmoWeatherCode.getIconCode(code, isDay(symbol));
        weather.weather = new WeatherResponse.WeatherCondition[]{condition};

        weather.sys = new WeatherResponse.Sys();
        weather.sys.country = geo != null && geo.country != null ? geo.country : "";
        weather.name = geo != null && geo.name != null ? geo.name : "";
        weather.dt = epochSeconds(current.time);
        weather.visibility = 10000;
        return weather;
    }

    public static List<ForecastItem> toForecastItems(MetNoForecastResponse response) {
        List<ForecastItem> result = new ArrayList<>();
        if (response == null || response.properties == null
                || response.properties.timeSeries == null) {
            return result;
        }

        for (MetNoForecastResponse.TimeSeries timeSeries : response.properties.timeSeries) {
            if (timeSeries == null || timeSeries.data == null
                    || timeSeries.data.instant == null
                    || timeSeries.data.instant.details == null) {
                continue;
            }

            MetNoForecastResponse.Details details = timeSeries.data.instant.details;
            String symbol = symbolCode(timeSeries.data);
            int code = toWmoCode(symbol);

            ForecastItem item = new ForecastItem();
            item.timestamp = epochSeconds(timeSeries.time);
            item.dtText = timeSeries.time != null
                    ? timeSeries.time.replace("T", " ").replace("Z", "")
                    : "";
            item.main = new ForecastItem.Main();
            item.main.temp = value(details.airTemperature);
            item.main.feelsLike = item.main.temp;
            item.main.tempMin = item.main.temp;
            item.main.tempMax = item.main.temp;
            item.main.pressure = (int) Math.round(value(details.airPressure));
            item.main.humidity = (int) Math.round(value(details.humidity));

            item.wind = new ForecastItem.Wind();
            item.wind.speed = value(details.windSpeed);
            item.wind.deg = (int) Math.round(value(details.windDirection));

            item.clouds = new ForecastItem.Clouds();
            item.clouds.all = (int) Math.round(value(details.cloudCover));
            item.pop = value(precipitationProbability(timeSeries.data)) / 100.0;
            item.visibility = 10000;

            ForecastItem.WeatherCondition condition = new ForecastItem.WeatherCondition();
            condition.id = code;
            condition.main = WmoWeatherCode.getDescription(code);
            condition.description = condition.main;
            condition.icon = WmoWeatherCode.getIconCode(code, isDay(symbol));
            item.weather = new ForecastItem.WeatherCondition[]{condition};
            result.add(item);
        }
        return result;
    }

    private static MetNoForecastResponse.TimeSeries firstTimeSeries(
            MetNoForecastResponse response
    ) {
        if (response == null || response.properties == null
                || response.properties.timeSeries == null
                || response.properties.timeSeries.isEmpty()) {
            return null;
        }
        return response.properties.timeSeries.get(0);
    }

    private static Double precipitationProbability(MetNoForecastResponse.TimeData data) {
        MetNoForecastResponse.Period period = forecastPeriod(data);
        return period != null && period.details != null
                ? period.details.precipitationProbability : null;
    }

    private static String symbolCode(MetNoForecastResponse.TimeData data) {
        MetNoForecastResponse.Period period = forecastPeriod(data);
        return period != null && period.summary != null
                ? period.summary.symbolCode : "";
    }

    private static MetNoForecastResponse.Period forecastPeriod(
            MetNoForecastResponse.TimeData data
    ) {
        if (data.next1Hours != null) return data.next1Hours;
        if (data.next6Hours != null) return data.next6Hours;
        return data.next12Hours;
    }

    private static int toWmoCode(String symbol) {
        String normalized = symbol == null ? "" : symbol.toLowerCase(Locale.ROOT);
        if (normalized.contains("thunderstorm")) return 95;
        if (normalized.contains("snowshowers")) return 85;
        if (normalized.contains("rainshowers")) return 80;
        if (normalized.contains("sleet")) return 66;
        if (normalized.contains("heavysnow")) return 75;
        if (normalized.contains("snow")) return 73;
        if (normalized.contains("heavyrain")) return 65;
        if (normalized.contains("lightrain")) return 61;
        if (normalized.contains("rain")) return 63;
        if (normalized.contains("drizzle")) return 51;
        if (normalized.contains("fog")) return 45;
        if (normalized.contains("partlycloudy")) return 2;
        if (normalized.contains("cloudy")) return 3;
        if (normalized.contains("fair")) return 1;
        return 0;
    }

    private static boolean isDay(String symbol) {
        return symbol != null && (symbol.endsWith("_day")
                || symbol.endsWith("_polartwilight"));
    }

    private static double value(Double value) {
        return value != null ? value : 0;
    }

    private static long epochSeconds(String time) {
        if (time == null) return 0;
        try {
            return Instant.parse(time).getEpochSecond();
        } catch (DateTimeParseException ignored) {
            return 0;
        }
    }
}
