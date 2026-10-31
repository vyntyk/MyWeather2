package com.home.myweather.ui.viewmodel;

import static org.junit.Assert.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.home.myweather.data.model.DailyData;
import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.data.model.ForecastResponse;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.repository.ForecastCache;
import com.home.myweather.data.repository.WeatherStorage;
import com.home.myweather.testing.FakeWeatherRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ForecastViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private FakeWeatherRepository repository;
    private WeatherStorage storage;
    private ForecastViewModel viewModel;

    @Before
    public void setUp() {
        ForecastCache.clear();
        storage = new WeatherStorage(ApplicationProvider.getApplicationContext());
        storage.clear();
        repository = new FakeWeatherRepository();
        viewModel = new ForecastViewModel(repository, storage);
    }

    @Test
    public void setGeoLocation_startsLoading() {
        GeoLocation geo = new GeoLocation();
        geo.lat = 55.75;
        geo.lon = 37.62;

        viewModel.setGeoLocation(geo);

        ForecastUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertTrue(state.isLoading());
    }

    @Test
    public void loadForecast_withCachedItems_usesCacheWithoutNetwork() {
        List<ForecastItem> cachedItems = new ArrayList<>();
        ForecastItem item = new ForecastItem();
        item.timestamp = System.currentTimeMillis() / 1000L;
        cachedItems.add(item);
        ForecastCache.put(55.75, 37.62, cachedItems);

        viewModel.loadForecast(55.75, 37.62);

        ForecastUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertFalse(state.isLoading());
        assertEquals(1, state.getDays().size());
        assertTrue(repository.forecastCalls.isEmpty());
    }

    @Test
    public void loadForecast_withoutCache_showsLoading() {
        viewModel.loadForecast(55.75, 37.62);

        ForecastUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertTrue(state.isLoading());
    }

    @Test
    public void refresh_refetchesForecastForCurrentLocation() {
        repository.onForecast = FakeWeatherRepository.forecastWith(emptyForecast());

        GeoLocation geo = new GeoLocation();
        geo.lat = 55.75;
        geo.lon = 37.62;
        viewModel.setGeoLocation(geo);
        idle();
        viewModel.refresh();
        idle();

        assertEquals(2, repository.forecastCalls.size());
    }

    @Test
    public void getCachedDays_returnsEmptyWhenNoGeo() {
        List<DailyData> cached = viewModel.getCachedDays();
        assertNotNull(cached);
        assertTrue(cached.isEmpty());
    }

    @Test
    public void onCleared_clearsLocalCache() {
        repository.onForecast = FakeWeatherRepository.forecastWith(emptyForecast());
        GeoLocation geo = new GeoLocation();
        geo.lat = 55.75;
        geo.lon = 37.62;
        viewModel.setGeoLocation(geo);
        idle();

        viewModel.onCleared();

        assertTrue(viewModel.getCachedDays().isEmpty());
    }

    private static ForecastResponse emptyForecast() {
        ForecastResponse response = new ForecastResponse();
        response.list = new ArrayList<>();
        return response;
    }

    /** Дожидается выполнения отложенной работы ViewModel на главном потоке. */
    private static void idle() {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }
}
