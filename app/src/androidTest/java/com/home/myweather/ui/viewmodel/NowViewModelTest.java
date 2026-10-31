package com.home.myweather.ui.viewmodel;

import static org.junit.Assert.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.data.model.ForecastResponse;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.model.WeatherResponse;
import com.home.myweather.data.repository.WeatherStorage;
import com.home.myweather.testing.FakeWeatherRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class NowViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private FakeWeatherRepository repository;
    private WeatherStorage storage;
    private NowViewModel viewModel;

    @Before
    public void setUp() {
        storage = new WeatherStorage(ApplicationProvider.getApplicationContext());
        storage.clear();
        repository = new FakeWeatherRepository();
        viewModel = new NowViewModel(repository, storage);
    }

    @Test
    public void fetchWeatherByCoords_setsLoadingThenSuccess() {
        GeoLocation geo = new GeoLocation();
        geo.lat = 55.75;
        geo.lon = 37.62;
        geo.name = "Moscow";
        WeatherResponse weather = new WeatherResponse();
        weather.name = "Moscow";
        repository.onCoords = FakeWeatherRepository.successWith(weather, geo);

        viewModel.fetchWeatherByCoords(55.75, 37.62);
        idle();

        WeatherUiState state = viewModel.uiState.getValue();
        assertNotNull(state);
        assertFalse(state.isLoading);
        assertNotNull(state.weather);
        assertEquals("Moscow", state.weather.name);
        assertEquals(1, repository.coordsCalls.size());
    }

    @Test
    public void fetchWeatherByCity_setsWeather() {
        GeoLocation geo = new GeoLocation();
        geo.name = "Saint Petersburg";
        WeatherResponse weather = new WeatherResponse();
        weather.name = "Saint Petersburg";
        repository.onCity = (city, cb) -> cb.onSuccess(weather, geo);

        viewModel.fetchWeatherByCity("Saint Petersburg");
        idle();

        assertEquals("Saint Petersburg", viewModel.uiState.getValue().weather.name);
    }

    @Test
    public void fetchWeatherByCoords_onError_setsError() {
        repository.onCoords = FakeWeatherRepository.failWith("Network error");

        viewModel.fetchWeatherByCoords(55.75, 37.62);
        idle();

        WeatherUiState state = viewModel.uiState.getValue();
        assertFalse(state.isLoading);
        assertEquals("Network error", state.error);
    }

    @Test
    public void loadForecast_populatesHourlyForecast() {
        List<ForecastItem> items = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ForecastItem item = new ForecastItem();
            item.timestamp = System.currentTimeMillis() / 1000L + i * 3600;
            items.add(item);
        }
        ForecastResponse response = new ForecastResponse();
        response.list = items;
        repository.onForecast = FakeWeatherRepository.forecastWith(response);

        viewModel.loadForecast(55.75, 37.62);
        idle();

        List<ForecastItem> hourly = viewModel.hourlyForecast.getValue();
        assertNotNull(hourly);
        assertEquals(5, hourly.size());
    }

    @Test
    public void refresh_refetchesLastKnownLocation() {
        GeoLocation geo = new GeoLocation();
        geo.lat = 55.75;
        geo.lon = 37.62;
        WeatherResponse weather = new WeatherResponse();
        weather.name = "Refreshed";
        repository.onCoords = FakeWeatherRepository.successWith(weather, geo);
        viewModel.lastGeo = geo;

        viewModel.refresh();
        idle();

        assertEquals("Refreshed", viewModel.uiState.getValue().weather.name);
        assertEquals(1, repository.coordsCalls.size());
        assertEquals(55.75, repository.coordsCalls.get(0)[0], 1e-6);
    }

    @Test
    public void clearRequests_cancelsRepository() {
        viewModel.clearRequests();
        assertEquals(1, repository.cancelCalls);
    }

    @Test
    public void initialState_loadsFromStorage() {
        WeatherResponse weather = new WeatherResponse();
        weather.name = "Cached City";
        GeoLocation geo = new GeoLocation();
        geo.name = "Cached City";
        storage.saveWeather(weather, geo);

        NowViewModel restored = new NowViewModel(repository, storage);

        WeatherUiState state = restored.uiState.getValue();
        assertNotNull(state);
        assertNotNull(state.weather);
        assertEquals("Cached City", state.weather.name);
    }

    /** Дожидается выполнения отложенной работы ViewModel на главном потоке. */
    private static void idle() {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }
}
