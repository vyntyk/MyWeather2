package com.home.myweather.testing;

import com.home.myweather.data.model.ForecastResponse;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.model.WeatherResponse;
import com.home.myweather.data.repository.WeatherRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Фейк WeatherRepository для ViewModel-тестов.
 * Ответы задаются через обработчики, все вызовы записываются для проверок.
 */
public final class FakeWeatherRepository extends WeatherRepository {

    public interface CoordsHandler {
        void handle(double lat, double lon, WeatherCallback callback);
    }

    public interface CityHandler {
        void handle(String city, WeatherCallback callback);
    }

    public interface ForecastHandler {
        void handle(double lat, double lon, ForecastCallback callback);
    }

    public CoordsHandler onCoords = (lat, lon, cb) -> { };
    public CityHandler onCity = (city, cb) -> { };
    public CoordsHandler onMarker = (lat, lon, cb) -> { };
    public ForecastHandler onForecast = (lat, lon, cb) -> { };

    public final List<double[]> coordsCalls = new ArrayList<>();
    public final List<double[]> markerCalls = new ArrayList<>();
    public final List<double[]> forecastCalls = new ArrayList<>();
    public int cancelCalls;

    public FakeWeatherRepository() {
        super(null, null, null, null);
    }

    /** Удобный обработчик: сразу отдаёт успешный ответ для любых координат. */
    public static CoordsHandler successWith(WeatherResponse weather, GeoLocation geo) {
        return (lat, lon, cb) -> cb.onSuccess(weather, geo);
    }

    /** Удобный обработчик: сразу отдаёт ошибку для любых координат. */
    public static CoordsHandler failWith(String message) {
        return (lat, lon, cb) -> cb.onError(message);
    }

    /** Удобный обработчик прогноза: успешный ответ с переданным списком. */
    public static ForecastHandler forecastWith(ForecastResponse response) {
        return (lat, lon, cb) -> cb.onSuccess(response);
    }

    @Override
    public void fetchWeather(String city, String country, WeatherCallback callback) {
        onCity.handle(city, callback);
    }

    @Override
    public void fetchWeatherByCoords(double lat, double lon, WeatherCallback callback) {
        coordsCalls.add(new double[]{lat, lon});
        onCoords.handle(lat, lon, callback);
    }

    @Override
    public void fetchCurrentWeatherMarker(double lat, double lon, WeatherCallback callback) {
        markerCalls.add(new double[]{lat, lon});
        onMarker.handle(lat, lon, callback);
    }

    @Override
    public void fetchForecast(double lat, double lon, ForecastCallback callback) {
        forecastCalls.add(new double[]{lat, lon});
        onForecast.handle(lat, lon, callback);
    }

    @Override
    public void cancelPendingRequests() {
        cancelCalls++;
    }
}
