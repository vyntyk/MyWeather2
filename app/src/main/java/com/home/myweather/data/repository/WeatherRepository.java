package com.home.myweather.data.repository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.data.model.ForecastResponse;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.model.OpenMeteoForecastResponse;
import com.home.myweather.data.model.WeatherResponse;
import com.home.myweather.data.network.GeocodingApiService;
import com.home.myweather.data.network.RetrofitClient;
import com.home.myweather.data.network.WeatherApiService;
import com.home.myweather.utils.WmoWeatherCode;
import javax.inject.Inject;

/**
 * Репозиторий погоды на базе Open-Meteo.
 *
 * Один вызов getForecast() возвращает current + hourly + daily.
 * Конвертирует ответ в существующие модели WeatherResponse / ForecastItem,
 * поэтому UI-слой (фрагменты, адаптеры) не требует никаких изменений.
 *
 * API-ключ не нужен.
 */
public class WeatherRepository {

    // ── Параметры запроса ─────────────────────────────────────────────────

    private static final String CURRENT_PARAMS =
            "temperature_2m,relative_humidity_2m,apparent_temperature," +
            "precipitation_probability,precipitation,weather_code,cloud_cover," +
            "surface_pressure,wind_speed_10m,wind_direction_10m,is_day";

    private static final String HOURLY_PARAMS =
            "temperature_2m,relative_humidity_2m,apparent_temperature," +
            "precipitation_probability,weather_code,wind_speed_10m," +
            "wind_direction_10m,surface_pressure,visibility";

    private static final String DAILY_PARAMS =
            "weather_code,temperature_2m_max,temperature_2m_min," +
            "precipitation_probability_max,wind_speed_10m_max,sunrise,sunset";

    // ── Интерфейсы обратного вызова ───────────────────────────────────────

    public interface WeatherCallback {
        void onSuccess(WeatherResponse weather, GeoLocation geo);
        void onError(String message);
    }

    public interface ForecastCallback {
        void onSuccess(ForecastResponse forecast);
        void onError(String message);
    }

    // ── Поля ──────────────────────────────────────────────────────────────

    private final WeatherApiService apiService;
    private final GeocodingRepository geocoding;
    private final Object lock = new Object();

    @Inject
    public WeatherRepository(WeatherApiService apiService, GeocodingRepository geocoding) {
        this.apiService = apiService;
        this.geocoding = geocoding;
    }

    /**
     * No-args constructor for backward compatibility with existing tests.
     * Uses RetrofitClient singleton for API services.
     */
    public WeatherRepository() {
        this(RetrofitClient.getWeatherApiService(), new GeocodingRepository(RetrofitClient.getGeocodingApiService()));
    }

    private Call<OpenMeteoForecastResponse> weatherCall;
    private Call<OpenMeteoForecastResponse> forecastCall;
    private long requestId = 0L;

    // ── Публичный API ─────────────────────────────────────────────────────

    /** Текущая погода по названию города. */
    public void fetchWeather(String city, String country, WeatherCallback callback) {
        if (callback == null) throw new IllegalArgumentException("callback must not be null");
        if (city == null || city.trim().isEmpty()) {
            callback.onError("Укажите название города");
            return;
        }
        String query = (country != null && !country.trim().isEmpty())
                ? city.trim() + "," + country.trim()
                : city.trim();

        final long rid;
        synchronized (lock) { cancelLocked(); rid = ++requestId; }

        geocoding.fetch(query, rid, new GeocodingRepository.Callback2() {
            @Override public void onSuccess(GeoLocation geo) {
                fetchCurrentWeather(geo, callback, rid);
            }
            @Override public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    /** Текущая погода по GPS-координатам. */
    public void fetchWeatherByCoords(double lat, double lon, WeatherCallback callback) {
        if (callback == null) throw new IllegalArgumentException("callback must not be null");
        GeoLocation geo = new GeoLocation();
        geo.lat  = lat;
        geo.lon  = lon;
        geo.name = "GPS";

        final long rid;
        synchronized (lock) { cancelLocked(); rid = ++requestId; }
        fetchCurrentWeather(geo, callback, rid);
    }

    /** Почасовой прогноз (используется ForecastFragment). */
    public void fetchForecast(double lat, double lon, ForecastCallback callback) {
        if (callback == null) throw new IllegalArgumentException("callback must not be null");

        final long rid;
        synchronized (lock) { cancelLocked(); rid = ++requestId; }

        final Call<OpenMeteoForecastResponse> call = buildForecastCall(lat, lon);
        synchronized (lock) { forecastCall = call; }

        call.enqueue(new Callback<OpenMeteoForecastResponse>() {
            @Override
            public void onResponse(Call<OpenMeteoForecastResponse> c,
                                   Response<OpenMeteoForecastResponse> r) {
                if (c.isCanceled() || rid != requestId) return;
                OpenMeteoForecastResponse body = r.body();
                if (!r.isSuccessful() || body == null || body.hourly == null) {
                    callback.onError("Ошибка прогноза (код " + r.code() + ")");
                    return;
                }
                List<ForecastItem> items = toForecastItems(body);
                ForecastCache.put(lat, lon, items);

                ForecastResponse fr = new ForecastResponse();
                fr.list = items;
                callback.onSuccess(fr);
            }

            @Override
            public void onFailure(Call<OpenMeteoForecastResponse> c, Throwable t) {
                if (!c.isCanceled() && rid == requestId) callback.onError(networkError(t));
            }
        });
    }

    public void cancelPendingRequests() {
        synchronized (lock) { cancelLocked(); requestId++; }
    }

    // ── Внутренняя логика ─────────────────────────────────────────────────

    private void fetchCurrentWeather(GeoLocation geo, WeatherCallback callback, long rid) {
        final Call<OpenMeteoForecastResponse> call;
        synchronized (lock) {
            if (rid != requestId) return;
            call = buildForecastCall(geo.lat, geo.lon);
            weatherCall = call;
        }

        call.enqueue(new Callback<OpenMeteoForecastResponse>() {
            @Override
            public void onResponse(Call<OpenMeteoForecastResponse> c,
                                   Response<OpenMeteoForecastResponse> r) {
                if (c.isCanceled() || rid != requestId) return;
                OpenMeteoForecastResponse body = r.body();
                if (!r.isSuccessful() || body == null || body.current == null) {
                    callback.onError("Ошибка погоды (код " + r.code() + ")");
                    return;
                }
                // Кэшируем почасовые данные — ForecastFragment их возьмёт без нового запроса
                List<ForecastItem> items = toForecastItems(body);
                ForecastCache.put(geo.lat, geo.lon, items);

                WeatherResponse wr = toWeatherResponse(body, geo);
                callback.onSuccess(wr, geo);
            }

            @Override
            public void onFailure(Call<OpenMeteoForecastResponse> c, Throwable t) {
                if (!c.isCanceled() && rid == requestId) callback.onError(networkError(t));
            }
        });
    }

    private Call<OpenMeteoForecastResponse> buildForecastCall(double lat, double lon) {
        return apiService.getForecast(
                lat, lon,
                CURRENT_PARAMS, HOURLY_PARAMS, DAILY_PARAMS,
                "ms",    // скорость ветра в м/с
                "auto",  // часовой пояс по координатам
                7
        );
    }

    // ── Конвертация Open-Meteo → WeatherResponse ──────────────────────────

    private WeatherResponse toWeatherResponse(OpenMeteoForecastResponse r, GeoLocation geo) {
        WeatherResponse wr = new WeatherResponse();
        OpenMeteoForecastResponse.Current cur = r.current;

        // Main
        WeatherResponse.Main main = new WeatherResponse.Main();
        main.temp      = cur.temperature2m;
        main.feelsLike = cur.apparentTemperature;
        main.pressure  = (int) Math.round(cur.surfacePressure);
        main.humidity  = cur.relativeHumidity2m;
        if (r.daily != null
                && r.daily.temperature2mMin != null
                && !r.daily.temperature2mMin.isEmpty()) {
            main.tempMin = r.daily.temperature2mMin.get(0);
            main.tempMax = r.daily.temperature2mMax.get(0);
        } else {
            main.tempMin = cur.temperature2m;
            main.tempMax = cur.temperature2m;
        }
        wr.main = main;

        // Wind
        WeatherResponse.Wind wind = new WeatherResponse.Wind();
        wind.speed = cur.windSpeed10m;
        wind.deg   = cur.windDirection10m;
        wr.wind = wind;

        // Clouds
        WeatherResponse.Clouds clouds = new WeatherResponse.Clouds();
        clouds.all = cur.cloudCover;
        wr.clouds = clouds;

        // Coord
        WeatherResponse.Coord coord = new WeatherResponse.Coord();
        coord.lat = r.latitude;
        coord.lon = r.longitude;
        wr.coord = coord;

        // WeatherCondition (WMO → иконка + описание)
        boolean isDay = cur.isDay == 1;
        WeatherResponse.WeatherCondition cond = new WeatherResponse.WeatherCondition();
        cond.id          = cur.weatherCode;
        cond.icon        = WmoWeatherCode.getIconCode(cur.weatherCode, isDay);
        cond.description = WmoWeatherCode.getDescription(cur.weatherCode);
        cond.main        = cond.description;
        wr.weather = new WeatherResponse.WeatherCondition[]{cond};

        // Sys
        WeatherResponse.Sys sys = new WeatherResponse.Sys();
        if (r.daily != null
                && r.daily.sunrise != null
                && !r.daily.sunrise.isEmpty()) {
            sys.sunrise = parseIsoToEpochSeconds(r.daily.sunrise.get(0));
            sys.sunset  = parseIsoToEpochSeconds(r.daily.sunset.get(0));
        }
        sys.country = (geo != null && geo.country != null) ? geo.country : "";
        wr.sys = sys;

        // Прочее
        wr.name       = (geo != null && geo.name != null) ? geo.name : "";
        wr.dt         = System.currentTimeMillis() / 1000;
        wr.timezone   = r.utcOffsetSeconds;
        wr.visibility = 10000;

        return wr;
    }

    // ── Конвертация Open-Meteo hourly → List<ForecastItem> ───────────────

    private List<ForecastItem> toForecastItems(OpenMeteoForecastResponse r) {
        List<ForecastItem> result = new ArrayList<>();
        if (r.hourly == null || r.hourly.time == null) return result;

        int count = r.hourly.time.size();
        for (int i = 0; i < count; i++) {
            String timeStr = r.hourly.time.get(i);

            ForecastItem item = new ForecastItem();
            item.timestamp = parseIsoToEpochSeconds(timeStr);
            item.dtText    = timeStr.replace("T", " ") + ":00";

            // Main
            item.main           = new ForecastItem.Main();
            item.main.temp      = getDouble(r.hourly.temperature2m, i, 0.0);
            item.main.feelsLike = getDouble(r.hourly.apparentTemperature, i, item.main.temp);
            item.main.tempMin   = item.main.temp;
            item.main.tempMax   = item.main.temp;
            item.main.pressure  = (int) Math.round(getDouble(r.hourly.surfacePressure, i, 1013.0));
            item.main.humidity  = getInt(r.hourly.relativeHumidity2m, i, 0);

            // Wind
            item.wind       = new ForecastItem.Wind();
            item.wind.speed = getDouble(r.hourly.windSpeed10m, i, 0.0);
            item.wind.deg   = getInt(r.hourly.windDirection10m, i, 0);

            // Clouds (заглушка — Open-Meteo не даёт cloud_cover в hourly по умолчанию)
            item.clouds     = new ForecastItem.Clouds();
            item.clouds.all = 0;

            // Вероятность осадков (0–100 → 0.0–1.0)
            item.pop = getInt(r.hourly.precipitationProbability, i, 0) / 100.0;

            // Видимость в метрах
            item.visibility = (int) getDouble(r.hourly.visibility, i, 10000.0);

            // WeatherCondition
            int wmoCode = getInt(r.hourly.weatherCode, i, 0);
            int hour    = extractHour(timeStr);
            boolean isDay = (hour >= 6 && hour < 20);

            item.weather    = new ForecastItem.WeatherCondition[1];
            item.weather[0] = new ForecastItem.WeatherCondition();
            item.weather[0].id          = wmoCode;
            item.weather[0].icon        = WmoWeatherCode.getIconCode(wmoCode, isDay);
            item.weather[0].description = WmoWeatherCode.getDescription(wmoCode);
            item.weather[0].main        = item.weather[0].description;

            result.add(item);
        }
        return result;
    }

    // ── Вспомогательные методы ────────────────────────────────────────────

    /**
     * Разбирает строку "2024-06-25T14:00" как UTC-время и возвращает Unix-секунды.
     * Open-Meteo с timezone=auto возвращает местное время, которое HourlyAdapter
     * форматирует через SimpleDateFormat устройства — поведение аналогично исходному OWM.
     */
    private static long parseIsoToEpochSeconds(String iso) {
        if (iso == null || iso.length() < 16) return 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sdf.parse(iso.substring(0, 16));
            return date != null ? date.getTime() / 1000 : 0;
        } catch (ParseException e) {
            return 0;
        }
    }

    /** Извлекает час из строки "2024-06-25T14:00" → 14. */
    private static int extractHour(String iso) {
        if (iso == null || iso.length() < 13) return 12;
        try {
            return Integer.parseInt(iso.substring(11, 13));
        } catch (NumberFormatException e) {
            return 12;
        }
    }

    private static double getDouble(List<Double> list, int i, double def) {
        if (list == null || i >= list.size() || list.get(i) == null) return def;
        return list.get(i);
    }

    private static int getInt(List<Integer> list, int i, int def) {
        if (list == null || i >= list.size() || list.get(i) == null) return def;
        return list.get(i);
    }

    private void cancelLocked() {
        geocoding.cancel();
        if (weatherCall  != null && !weatherCall.isCanceled())  weatherCall.cancel();
        if (forecastCall != null && !forecastCall.isCanceled()) forecastCall.cancel();
    }

    private static String networkError(Throwable t) {
        String m = t != null ? t.getMessage() : null;
        return "Ошибка сети" + (m != null && !m.isEmpty() ? ": " + m : "");
    }
}
