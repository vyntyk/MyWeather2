package com.home.myweather.data.mapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.model.OpenMeteoForecastResponse;
import com.home.myweather.data.model.WeatherResponse;
import com.home.myweather.utils.WmoWeatherCode;

/**
 * Маппер для преобразования ответов Open-Meteo API в модели приложения.
 * 
 * Open-Meteo использует WMO-коды (0–99), которые преобразуются в:
 * - OpenWeatherMap иконки (01d, 10n и т.д.) через WmoWeatherCode
 * - Русские описания через WmoWeatherCode
 * 
 * UI-слой работает с WeatherResponse и ForecastItem, поэтому не требует изменений.
 */
public final class OpenMeteoMapper {

    private OpenMeteoMapper() {}

    /**
     * Преобразует Open-Meteo ответ (current + daily) в WeatherResponse.
     * Используется для отображения текущей погоды в NowFragment и MapFragment.
     */
    public static WeatherResponse toWeatherResponse(OpenMeteoForecastResponse response, GeoLocation geo) {
        if (response == null || response.current == null) {
            return null;
        }

        WeatherResponse wr = new WeatherResponse();
        OpenMeteoForecastResponse.Current current = response.current;

        // Main — основные данные о погоде
        WeatherResponse.Main main = new WeatherResponse.Main();
        main.temp = current.temperature2m;
        main.feelsLike = current.apparentTemperature;
        main.pressure = (int) Math.round(current.surfacePressure);
        main.humidity = current.relativeHumidity2m;
        
        // Min/Max из daily (если доступно)
        if (response.daily != null
                && response.daily.temperature2mMin != null
                && !response.daily.temperature2mMin.isEmpty()) {
            main.tempMin = response.daily.temperature2mMin.get(0);
            main.tempMax = response.daily.temperature2mMax.get(0);
        } else {
            main.tempMin = current.temperature2m;
            main.tempMax = current.temperature2m;
        }
        wr.main = main;

        // Wind — ветер
        WeatherResponse.Wind wind = new WeatherResponse.Wind();
        wind.speed = current.windSpeed10m;
        wind.deg = current.windDirection10m;
        wr.wind = wind;

        // Clouds — облачность
        WeatherResponse.Clouds clouds = new WeatherResponse.Clouds();
        clouds.all = current.cloudCover;
        wr.clouds = clouds;

        // Coord — координаты
        WeatherResponse.Coord coord = new WeatherResponse.Coord();
        coord.lat = response.latitude;
        coord.lon = response.longitude;
        wr.coord = coord;

        // Weather — текущее состояние (WMO → иконка + описание)
        boolean isDay = current.isDay == 1;
        WeatherResponse.WeatherCondition condition = new WeatherResponse.WeatherCondition();
        condition.id = current.weatherCode;
        condition.icon = WmoWeatherCode.getIconCode(current.weatherCode, isDay);
        condition.description = WmoWeatherCode.getDescription(current.weatherCode);
        condition.main = condition.description;
        wr.weather = new WeatherResponse.WeatherCondition[]{condition};

        // Sys — системная информация
        WeatherResponse.Sys sys = new WeatherResponse.Sys();
        if (response.daily != null
                && response.daily.sunrise != null
                && !response.daily.sunrise.isEmpty()) {
            sys.sunrise = parseIsoToEpochSeconds(response.daily.sunrise.get(0));
            sys.sunset = parseIsoToEpochSeconds(response.daily.sunset.get(0));
        }
        sys.country = (geo != null && geo.country != null) ? geo.country : "";
        wr.sys = sys;

        // Прочие поля
        wr.name = (geo != null && geo.name != null) ? geo.name : "";
        wr.dt = System.currentTimeMillis() / 1000;
        wr.timezone = response.utcOffsetSeconds;
        wr.visibility = 10000;

        return wr;
    }

    /**
     * Преобразует Open-Meteo hourly-данные в список ForecastItem.
     * Используется для почасового прогноза в HourlyAdapter и ForecastFragment.
     */
    public static List<ForecastItem> toForecastItems(OpenMeteoForecastResponse response) {
        List<ForecastItem> result = new ArrayList<>();
        
        if (response == null || response.hourly == null || response.hourly.time == null) {
            return result;
        }

        int count = response.hourly.time.size();
        for (int i = 0; i < count; i++) {
            String timeStr = response.hourly.time.get(i);
            ForecastItem item = new ForecastItem();
            
            // Время (Unix-секунды)
            item.timestamp = parseIsoToEpochSeconds(timeStr);
            item.dtText = timeStr.replace("T", " ") + ":00";

            // Main — основные данные
            item.main = new ForecastItem.Main();
            item.main.temp = getDouble(response.hourly.temperature2m, i, 0.0);
            item.main.feelsLike = getDouble(response.hourly.apparentTemperature, i, item.main.temp);
            item.main.tempMin = item.main.temp;
            item.main.tempMax = item.main.temp;
            item.main.pressure = (int) Math.round(getDouble(response.hourly.surfacePressure, i, 1013.0));
            item.main.humidity = getInt(response.hourly.relativeHumidity2m, i, 0);

            // Wind — ветер
            item.wind = new ForecastItem.Wind();
            item.wind.speed = getDouble(response.hourly.windSpeed10m, i, 0.0);
            item.wind.deg = getInt(response.hourly.windDirection10m, i, 0);

            // Clouds — облачность (заглушка)
            item.clouds = new ForecastItem.Clouds();
            item.clouds.all = 0;

            // Вероятность осадков (0–100 → 0.0–1.0)
            int popPercent = getInt(response.hourly.precipitationProbability, i, 0);
            item.pop = popPercent / 100.0;

            // Видимость в метрах
            item.visibility = (int) getDouble(response.hourly.visibility, i, 10000.0);

            // Weather — состояние (WMO → иконка + описание)
            int wmoCode = getInt(response.hourly.weatherCode, i, 0);
            int hour = extractHour(timeStr);
            boolean isDay = (hour >= 6 && hour < 20);

            item.weather = new ForecastItem.WeatherCondition[1];
            item.weather[0] = new ForecastItem.WeatherCondition();
            item.weather[0].id = wmoCode;
            item.weather[0].icon = WmoWeatherCode.getIconCode(wmoCode, isDay);
            item.weather[0].description = WmoWeatherCode.getDescription(wmoCode);
            item.weather[0].main = item.weather[0].description;

            result.add(item);
        }

        return result;
    }

    /**
     * Разбирает ISO 8601 строку вида "2024-06-25T14:00" как UTC-время
     * и возвращает Unix-секунды.
     * 
     * Open-Meteo с timezone=auto возвращает местное время, которое HourlyAdapter
     * форматирует через SimpleDateFormat устройства.
     */
    public static long parseIsoToEpochSeconds(String iso) {
        if (iso == null || iso.length() < 16) {
            return 0;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sdf.parse(iso.substring(0, 16));
            return date != null ? date.getTime() / 1000 : 0;
        } catch (ParseException e) {
            return 0;
        }
    }

    /**
     * Извлекает час из ISO 8601 строки.
     * Пример: "2024-06-25T14:00" → 14
     */
    private static int extractHour(String iso) {
        if (iso == null || iso.length() < 13) {
            return 12;
        }

        try {
            return Integer.parseInt(iso.substring(11, 13));
        } catch (NumberFormatException e) {
            return 12;
        }
    }

    /**
     * Безопасно получает Double из списка с дефолтным значением.
     */
    private static double getDouble(List<Double> list, int index, double defaultValue) {
        if (list == null || index >= list.size() || list.get(index) == null) {
            return defaultValue;
        }
        return list.get(index);
    }

    /**
     * Безопасно получает Integer из списка с дефолтным значением.
     */
    private static int getInt(List<Integer> list, int index, int defaultValue) {
        if (list == null || index >= list.size() || list.get(index) == null) {
            return defaultValue;
        }
        return list.get(index);
    }
}
