package com.home.myweather.data.repository;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.home.myweather.data.model.DailyData;
import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.model.OpenMeteoForecastResponse;
import com.home.myweather.data.network.WeatherApiService;
import com.home.myweather.helpers.NetworkMonitor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RunWith(AndroidJUnit4.class)
public class WeatherRepositoryAndroidTest {

    private Context context;

    @Mock WeatherApiService apiService;
    @Mock GeocodingRepository geocodingRepository;
    @Mock WeatherStorage weatherStorage;
    @Mock NetworkMonitor networkMonitor;

    private WeatherRepository repository;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        MockitoAnnotations.openMocks(this);
        repository = new WeatherRepository(apiService, geocodingRepository, weatherStorage, networkMonitor);
    }

    @Test
    public void fetchWeather_online_callsGeocodingAndApi() throws InterruptedException {
        when(networkMonitor.isOnline()).thenReturn(true);
        
        CountDownLatch latch = new CountDownLatch(1);
        WeatherRepository.WeatherCallback callback = new WeatherRepository.WeatherCallback() {
            @Override public void onSuccess(com.home.myweather.data.model.WeatherResponse weather, GeoLocation geo) {
                latch.countDown();
            }
            @Override public void onError(String message) {
                fail("Should not error: " + message);
                latch.countDown();
            }
        };

        // Mock geocoding success
        doAnswer(invocation -> {
            GeocodingRepository.Callback2 cb = invocation.getArgument(2);
            GeoLocation geo = new GeoLocation();
            geo.lat = 55.75;
            geo.lon = 37.62;
            geo.name = "Moscow";
            cb.onSuccess(geo);
            return null;
        }).when(geocodingRepository).fetch(anyString(), anyLong(), any());

        // Mock API call
        Call<OpenMeteoForecastResponse> apiCall = mock(Call.class);
        when(apiService.getForecast(anyDouble(), anyDouble(), anyString(), anyString(), anyString(), anyString(), anyString(), anyInt()))
            .thenReturn(apiCall);
        doAnswer(invocation -> {
            Callback<OpenMeteoForecastResponse> cb = invocation.getArgument(0);
            OpenMeteoForecastResponse response = createMockForecastResponse();
            cb.onResponse(apiCall, Response.success(response));
            return null;
        }).when(apiCall).enqueue(any());

        repository.fetchWeather("Moscow", null, callback);
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        
        verify(geocodingRepository).fetch(eq("Moscow"), anyLong(), any());
        verify(apiService).getForecast(anyDouble(), anyDouble(), anyString(), anyString(), anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    public void fetchWeather_offline_returnsCachedData() throws InterruptedException {
        when(networkMonitor.isOnline()).thenReturn(false);
        
        com.home.myweather.data.model.WeatherResponse cachedWeather = new com.home.myweather.data.model.WeatherResponse();
        cachedWeather.name = "Cached City";
        GeoLocation cachedGeo = new GeoLocation();
        cachedGeo.name = "Cached City";
        cachedGeo.lat = 1.0;
        cachedGeo.lon = 2.0;
        
        when(weatherStorage.loadWeather()).thenReturn(cachedWeather);
        when(weatherStorage.loadGeo()).thenReturn(cachedGeo);

        CountDownLatch latch = new CountDownLatch(1);
        WeatherRepository.WeatherCallback callback = new WeatherRepository.WeatherCallback() {
            @Override public void onSuccess(com.home.myweather.data.model.WeatherResponse weather, GeoLocation geo) {
                assertEquals("Cached City", weather.name);
                assertEquals("Cached City", geo.name);
                latch.countDown();
            }
            @Override public void onError(String message) {
                fail("Should not error: " + message);
                latch.countDown();
            }
        };

        repository.fetchWeather("Moscow", null, callback);
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        
        verify(geocodingRepository, never()).fetch(anyString(), anyLong(), any());
        verify(apiService, never()).getForecast(anyDouble(), anyDouble(), anyString(), anyString(), anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    public void fetchWeather_offline_noCache_returnsError() throws InterruptedException {
        when(networkMonitor.isOnline()).thenReturn(false);
        when(weatherStorage.loadWeather()).thenReturn(null);
        when(weatherStorage.loadGeo()).thenReturn(null);

        CountDownLatch latch = new CountDownLatch(1);
        WeatherRepository.WeatherCallback callback = new WeatherRepository.WeatherCallback() {
            @Override public void onSuccess(com.home.myweather.data.model.WeatherResponse weather, GeoLocation geo) {
                fail("Should not succeed");
                latch.countDown();
            }
            @Override public void onError(String message) {
                assertTrue(message.contains("Нет интернета"));
                latch.countDown();
            }
        };

        repository.fetchWeather("Moscow", null, callback);
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void fetchForecast_online_callsApiAndCaches() throws InterruptedException {
        when(networkMonitor.isOnline()).thenReturn(true);
        
        Call<OpenMeteoForecastResponse> apiCall = mock(Call.class);
        when(apiService.getForecast(anyDouble(), anyDouble(), anyString(), anyString(), anyString(), anyString(), anyString(), anyInt()))
            .thenReturn(apiCall);
        doAnswer(invocation -> {
            Callback<OpenMeteoForecastResponse> cb = invocation.getArgument(0);
            OpenMeteoForecastResponse response = createMockForecastResponse();
            cb.onResponse(apiCall, Response.success(response));
            return null;
        }).when(apiCall).enqueue(any());

        CountDownLatch latch = new CountDownLatch(1);
        WeatherRepository.ForecastCallback callback = new WeatherRepository.ForecastCallback() {
            @Override public void onSuccess(com.home.myweather.data.model.ForecastResponse forecast) {
                assertNotNull(forecast.list);
                assertFalse(forecast.list.isEmpty());
                latch.countDown();
            }
            @Override public void onError(String message) {
                fail("Should not error: " + message);
                latch.countDown();
            }
        };

        repository.fetchForecast(55.75, 37.62, callback);
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        
        verify(apiService).getForecast(anyDouble(), anyDouble(), anyString(), anyString(), anyString(), anyString(), anyString(), anyInt());
        verify(weatherStorage).saveForecast(anyList());
    }

    @Test
    public void cancelPendingRequests_cancelsCalls() {
        repository.cancelPendingRequests();
        verify(geocodingRepository).cancel();
    }

    private OpenMeteoForecastResponse createMockForecastResponse() {
        OpenMeteoForecastResponse resp = new OpenMeteoForecastResponse();
        resp.latitude = 55.75;
        resp.longitude = 37.62;
        resp.utcOffsetSeconds = 10800;
        
        OpenMeteoForecastResponse.Current current = new OpenMeteoForecastResponse.Current();
        current.temperature2m = 15.0;
        current.apparentTemperature = 14.0;
        current.relativeHumidity2m = 60;
        current.surfacePressure = 1013.0;
        current.windSpeed10m = 3.0;
        current.windDirection10m = 180;
        current.cloudCover = 20;
        current.isDay = 1;
        current.weatherCode = 0;
        current.time = "2024-01-15T12:00";
        resp.current = current;
        
        OpenMeteoForecastResponse.Hourly hourly = new OpenMeteoForecastResponse.Hourly();
        hourly.time = new ArrayList<>();
        hourly.temperature2m = new ArrayList<>();
        hourly.relativeHumidity2m = new ArrayList<>();
        hourly.apparentTemperature = new ArrayList<>();
        hourly.precipitationProbability = new ArrayList<>();
        hourly.weatherCode = new ArrayList<>();
        hourly.windSpeed10m = new ArrayList<>();
        hourly.windDirection10m = new ArrayList<>();
        hourly.surfacePressure = new ArrayList<>();
        hourly.visibility = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hourly.time.add(String.format("2024-01-15T%02d:00", i));
            hourly.temperature2m.add(10.0 + i * 0.5);
            hourly.relativeHumidity2m.add(50 + i);
            hourly.apparentTemperature.add(9.0 + i * 0.5);
            hourly.precipitationProbability.add(i % 3);
            hourly.weatherCode.add(0);
            hourly.windSpeed10m.add(2.0 + i * 0.1);
            hourly.windDirection10m.add(180);
            hourly.surfacePressure.add(1013.0);
            hourly.visibility.add(10000.0);
        }
        resp.hourly = hourly;
        
        OpenMeteoForecastResponse.Daily daily = new OpenMeteoForecastResponse.Daily();
        daily.time = new ArrayList<>();
        daily.temperature2mMax = new ArrayList<>();
        daily.temperature2mMin = new ArrayList<>();
        daily.weatherCode = new ArrayList<>();
        daily.precipitationProbabilityMax = new ArrayList<>();
        daily.windSpeed10mMax = new ArrayList<>();
        daily.sunrise = new ArrayList<>();
        daily.sunset = new ArrayList<>();
        daily.time.add("2024-01-15");
        daily.temperature2mMax.add(18.0);
        daily.temperature2mMin.add(8.0);
        daily.weatherCode.add(0);
        daily.precipitationProbabilityMax.add(10);
        daily.windSpeed10mMax.add(5.0);
        daily.sunrise.add("2024-01-15T08:00");
        daily.sunset.add("2024-01-15T16:00");
        resp.daily = daily;
        
        return resp;
    }
}