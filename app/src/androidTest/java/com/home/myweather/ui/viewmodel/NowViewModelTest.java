package com.home.myweather.ui.viewmodel;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.data.model.ForecastResponse;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.model.WeatherResponse;
import com.home.myweather.data.repository.WeatherRepository;
import com.home.myweather.data.repository.WeatherStorage;

import org.junit.Before;
import org.junit.Rule;
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
public class NowViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock WeatherRepository weatherRepository;
    @Mock WeatherStorage weatherStorage;

    private NowViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new NowViewModel(weatherRepository, weatherStorage);
    }

    @Test
    public void fetchWeatherByCoords_setsLoadingThenSuccess() throws InterruptedException {
        GeoLocation geo = new GeoLocation();
        geo.lat = 55.75;
        geo.lon = 37.62;
        geo.name = "Moscow";
        
        WeatherResponse weather = new WeatherResponse();
        weather.name = "Moscow";
        
        when(weatherRepository.fetchWeatherByCoords(anyDouble(), anyDouble(), any()))
            .thenAnswer(invocation -> {
                WeatherRepository.WeatherCallback cb = invocation.getArgument(2);
                cb.onSuccess(weather, geo);
                return null;
            });
        
        CountDownLatch latch = new CountDownLatch(2);
        LiveData<WeatherUiState> liveData = viewModel.uiState;
        Observer<WeatherUiState> observer = state -> {
            if (state != null) latch.countDown();
        };
        liveData.observeForever(observer);
        
        viewModel.fetchWeatherByCoords(55.75, 37.62);
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        liveData.removeObserver(observer);
        
        WeatherUiState finalState = viewModel.uiState.getValue();
        assertNotNull(finalState);
        assertFalse(finalState.isLoading);
        assertNotNull(finalState.weather);
        assertEquals("Moscow", finalState.weather.name);
    }

    @Test
    public void fetchWeatherByCity_setsLoadingThenSuccess() throws InterruptedException {
        GeoLocation geo = new GeoLocation();
        geo.name = "Saint Petersburg";
        
        WeatherResponse weather = new WeatherResponse();
        weather.name = "Saint Petersburg";
        
        when(weatherRepository.fetchWeather(anyString(), any(), any()))
            .thenAnswer(invocation -> {
                WeatherRepository.WeatherCallback cb = invocation.getArgument(2);
                cb.onSuccess(weather, geo);
                return null;
            });
        
        CountDownLatch latch = new CountDownLatch(2);
        LiveData<WeatherUiState> liveData = viewModel.uiState;
        Observer<WeatherUiState> observer = state -> {
            if (state != null) latch.countDown();
        };
        liveData.observeForever(observer);
        
        viewModel.fetchWeatherByCity("Saint Petersburg");
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        liveData.removeObserver(observer);
        
        WeatherUiState finalState = viewModel.uiState.getValue();
        assertNotNull(finalState);
        assertFalse(finalState.isLoading);
        assertEquals("Saint Petersburg", finalState.weather.name);
    }

    @Test
    public void fetchWeatherByCoords_onError_setsError() throws InterruptedException {
        when(weatherRepository.fetchWeatherByCoords(anyDouble(), anyDouble(), any()))
            .thenAnswer(invocation -> {
                WeatherRepository.WeatherCallback cb = invocation.getArgument(2);
                cb.onError("Network error");
                return null;
            });
        
        CountDownLatch latch = new CountDownLatch(2);
        LiveData<WeatherUiState> liveData = viewModel.uiState;
        Observer<WeatherUiState> observer = state -> {
            if (state != null) latch.countDown();
        };
        liveData.observeForever(observer);
        
        viewModel.fetchWeatherByCoords(55.75, 37.62);
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        liveData.removeObserver(observer);
        
        WeatherUiState finalState = viewModel.uiState.getValue();
        assertNotNull(finalState);
        assertFalse(finalState.isLoading);
        assertNotNull(finalState.error);
        assertEquals("Network error", finalState.error);
    }

    @Test
    public void loadForecast_populatesHourlyForecast() throws InterruptedException {
        List<ForecastItem> items = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ForecastItem item = new ForecastItem();
            item.timestamp = System.currentTimeMillis() / 1000L + i * 3600;
            items.add(item);
        }
        
        ForecastResponse response = new ForecastResponse();
        response.list = items;
        
        when(weatherRepository.fetchForecast(anyDouble(), anyDouble(), any()))
            .thenAnswer(invocation -> {
                WeatherRepository.ForecastCallback cb = invocation.getArgument(2);
                cb.onSuccess(response);
                return null;
            });
        
        CountDownLatch latch = new CountDownLatch(1);
        LiveData<List<ForecastItem>> liveData = viewModel.hourlyForecast;
        Observer<List<ForecastItem>> observer = list -> {
            if (list != null && !list.isEmpty()) latch.countDown();
        };
        liveData.observeForever(observer);
        
        viewModel.loadForecast(55.75, 37.62);
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        liveData.removeObserver(observer);
        
        List<ForecastItem> hourly = viewModel.hourlyForecast.getValue();
        assertNotNull(hourly);
        assertEquals(5, hourly.size());
    }

    @Test
    public void refresh_callsFetchWeatherByCoords() throws InterruptedException {
        GeoLocation geo = new GeoLocation();
        geo.lat = 55.75;
        geo.lon = 37.62;
        viewModel.lastGeo = geo;
        
        WeatherResponse weather = new WeatherResponse();
        weather.name = "Refreshed";
        
        when(weatherRepository.fetchWeatherByCoords(anyDouble(), anyDouble(), any()))
            .thenAnswer(invocation -> {
                WeatherRepository.WeatherCallback cb = invocation.getArgument(2);
                cb.onSuccess(weather, geo);
                return null;
            });
        
        CountDownLatch latch = new CountDownLatch(1);
        LiveData<WeatherUiState> liveData = viewModel.uiState;
        Observer<WeatherUiState> observer = state -> {
            if (state != null && !state.isLoading && state.weather != null 
                    && "Refreshed".equals(state.weather.name)) {
                latch.countDown();
            }
        };
        liveData.observeForever(observer);
        
        viewModel.refresh();
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        liveData.removeObserver(observer);
    }

    @Test
    public void clearRequests_cancelsRepository() {
        viewModel.clearRequests();
        verify(weatherRepository).cancelPendingRequests();
    }

    @Test
    public void initialState_loadsFromStorage() {
        WeatherResponse weather = new WeatherResponse();
        weather.name = "Cached City";
        
        GeoLocation geo = new GeoLocation();
        geo.name = "Cached City";
        
        when(weatherStorage.loadWeather()).thenReturn(weather);
        when(weatherStorage.loadGeo()).thenReturn(geo);
        when(weatherStorage.loadHourly()).thenReturn(new ArrayList<>());
        
        NowViewModel newViewModel = new NowViewModel(weatherRepository, weatherStorage);
        
        WeatherUiState state = newViewModel.uiState.getValue();
        assertNotNull(state);
        assertNotNull(state.weather);
        assertEquals("Cached City", state.weather.name);
    }
}