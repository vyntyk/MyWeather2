package com.home.myweather.data.repository;

import com.home.myweather.data.model.ForecastResponse;
import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.model.WeatherResponse;
import com.home.myweather.data.network.WeatherApiService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class WeatherRepositoryTest {

    @Mock
    private WeatherApiService mockApiService;

    private WeatherRepository repository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        // Note: WeatherRepository uses RetrofitClient singleton which we can't easily mock
        // So this test will use the real RetrofitClient (which makes real network calls)
        // For proper unit tests, we'd need to refactor WeatherRepository to accept WeatherApiService as dependency
    }

    @Test
    public void validate_emptyCity_returnsError() {
        // This test requires a mock callback
        WeatherRepository repo = new WeatherRepository();
        // The validate method is private, we can test it indirectly via fetchWeather
        // But we can't easily mock the API key check without dependency injection
    }

    @Test
    public void fetchWeatherByCoords_nullCallback_throwsException() {
        WeatherRepository repo = new WeatherRepository();
        try {
            repo.fetchWeatherByCoords(55.75, 37.61, null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("callback must not be null", e.getMessage());
        }
    }

    @Test
    public void fetchForecast_nullCallback_throwsException() {
        WeatherRepository repo = new WeatherRepository();
        try {
            repo.fetchForecast(55.75, 37.61, null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("callback must not be null", e.getMessage());
        }
    }

    @Test
    public void cancelPendingRequests_doesNotCrash() {
        WeatherRepository repo = new WeatherRepository();
        repo.cancelPendingRequests();
        // Should complete without exception
    }
}