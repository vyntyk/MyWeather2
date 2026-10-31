package com.home.myweather.data.repository;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.data.model.ForecastResponse;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.model.MetNoForecastResponse;
import com.home.myweather.data.model.WeatherResponse;
import com.home.myweather.data.network.WeatherApiService;
import com.home.myweather.helpers.NetworkMonitor;
import com.home.myweather.testing.FakeCall;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;

@RunWith(AndroidJUnit4.class)
public class WeatherRepositoryAndroidTest {

    private Context context;
    private WeatherStorage storage;
    private WeatherRepository repository;

    private boolean online;
    private boolean apiFails;
    private boolean geocodingSucceeds;
    private int apiCalls;
    private int geocodingCalls;
    private int geocodingCancelCalls;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        storage = new WeatherStorage(context);
        storage.clear();
        ForecastCache.clear();

        online = true;
        apiFails = false;
        geocodingSucceeds = true;

        NetworkMonitor network = new NetworkMonitor(context) {
            @Override
            public boolean isOnline() {
                return online;
            }
        };

        WeatherApiService api = (lat, lon) -> {
            apiCalls++;
            return apiFails
                    ? FakeCall.failing(new IOException("API down"))
                    : FakeCall.ok(createMetNoResponse());
        };

        GeocodingRepository geocoding = new GeocodingRepository(null) {
            @Override
            public void fetch(String query, long requestId, Callback2 callback) {
                geocodingCalls++;
                if (geocodingSucceeds) {
                    GeoLocation geo = new GeoLocation();
                    geo.lat = 55.75;
                    geo.lon = 37.62;
                    geo.name = query;
                    callback.onSuccess(geo);
                } else {
                    callback.onError("Город не найден");
                }
            }

            @Override
            public void cancel() {
                geocodingCancelCalls++;
            }
        };

        repository = new WeatherRepository(api, geocoding, storage, network);
    }

    @After
    public void tearDown() {
        storage.clear();
        ForecastCache.clear();
    }

    @Test
    public void fetchWeather_online_callsGeocodingAndApi_andSavesWeather() {
        final WeatherResponse[] result = new WeatherResponse[1];
        final GeoLocation[] resultGeo = new GeoLocation[1];

        repository.fetchWeather("Moscow", null, new WeatherRepository.WeatherCallback() {
            @Override
            public void onSuccess(WeatherResponse weather, GeoLocation geo) {
                result[0] = weather;
                resultGeo[0] = geo;
            }

            @Override
            public void onError(String message) {
                fail("Should not error: " + message);
            }
        });

        assertNotNull(result[0]);
        assertEquals("Moscow", resultGeo[0].name);
        assertEquals(1, geocodingCalls);
        assertEquals(1, apiCalls);
        assertNotNull(storage.loadWeather());
    }

    @Test
    public void fetchWeather_offline_returnsCachedData_withoutNetwork() {
        WeatherResponse cachedWeather = new WeatherResponse();
        cachedWeather.name = "Cached City";
        GeoLocation cachedGeo = new GeoLocation();
        cachedGeo.name = "Cached City";
        storage.saveWeather(cachedWeather, cachedGeo);
        online = false;

        final String[] name = new String[1];
        repository.fetchWeather("Moscow", null, new WeatherRepository.WeatherCallback() {
            @Override
            public void onSuccess(WeatherResponse weather, GeoLocation geo) {
                name[0] = weather.name;
            }

            @Override
            public void onError(String message) {
                fail("Should not error: " + message);
            }
        });

        assertEquals("Cached City", name[0]);
        assertEquals(0, geocodingCalls);
        assertEquals(0, apiCalls);
    }

    @Test
    public void fetchWeather_offline_noCache_returnsNoInternetError() {
        online = false;

        final String[] error = new String[1];
        repository.fetchWeather("Moscow", null, new WeatherRepository.WeatherCallback() {
            @Override
            public void onSuccess(WeatherResponse weather, GeoLocation geo) {
                fail("Should not succeed");
            }

            @Override
            public void onError(String message) {
                error[0] = message;
            }
        });

        assertNotNull(error[0]);
        assertTrue(error[0].contains("Нет интернета"));
    }

    @Test
    public void fetchForecast_online_callsApiAndCachesForecast() {
        final List<ForecastItem>[] result = new List[1];

        repository.fetchForecast(55.75, 37.62, new WeatherRepository.ForecastCallback() {
            @Override
            public void onSuccess(ForecastResponse forecast) {
                result[0] = forecast.list;
            }

            @Override
            public void onError(String message) {
                fail("Should not error: " + message);
            }
        });

        assertNotNull(result[0]);
        assertFalse(result[0].isEmpty());
        assertEquals(1, apiCalls);
        assertFalse(storage.loadHourly().isEmpty());
        assertNotNull(ForecastCache.get(55.75, 37.62));
    }

    @Test
    public void fetchForecast_apiFailure_servesSavedForecast() {
        List<ForecastItem> saved = new ArrayList<>();
        saved.add(new ForecastItem());
        GeoLocation savedGeo = new GeoLocation();
        savedGeo.lat = 55.75;
        savedGeo.lon = 37.62;
        storage.saveForecast(saved, savedGeo);
        apiFails = true;

        final List<ForecastItem>[] result = new List[1];
        repository.fetchForecast(55.75, 37.62, new WeatherRepository.ForecastCallback() {
            @Override
            public void onSuccess(ForecastResponse forecast) {
                result[0] = forecast.list;
            }

            @Override
            public void onError(String message) {
                fail("Should serve saved forecast, got error: " + message);
            }
        });

        assertNotNull(result[0]);
        assertEquals(1, result[0].size());
    }

    @Test
    public void cancelPendingRequests_cancelsGeocoding() {
        repository.cancelPendingRequests();
        assertEquals(1, geocodingCancelCalls);
    }

    private static MetNoForecastResponse createMetNoResponse() {
        MetNoForecastResponse response = new MetNoForecastResponse();
        response.properties = new MetNoForecastResponse.Properties();
        response.properties.timeSeries = new ArrayList<>();

        for (int i = 0; i < 24; i++) {
            MetNoForecastResponse.TimeSeries ts = new MetNoForecastResponse.TimeSeries();
            ts.time = String.format(Locale.US, "2024-01-15T%02d:00:00Z", i);

            ts.data = new MetNoForecastResponse.TimeData();
            ts.data.instant = new MetNoForecastResponse.Instant();
            ts.data.instant.details = new MetNoForecastResponse.Details();
            ts.data.instant.details.airTemperature = 10.0 + i * 0.5;
            ts.data.instant.details.airPressure = 1013.0;
            ts.data.instant.details.humidity = 60.0;
            ts.data.instant.details.windSpeed = 3.0;
            ts.data.instant.details.windDirection = 180.0;
            ts.data.instant.details.cloudCover = 20.0;

            ts.data.next1Hours = new MetNoForecastResponse.Period();
            ts.data.next1Hours.summary = new MetNoForecastResponse.Summary();
            ts.data.next1Hours.summary.symbolCode = "clearsky_day";
            ts.data.next1Hours.details = new MetNoForecastResponse.Details();
            ts.data.next1Hours.details.precipitationProbability = 10.0;

            response.properties.timeSeries.add(ts);
        }
        return response;
    }
}
