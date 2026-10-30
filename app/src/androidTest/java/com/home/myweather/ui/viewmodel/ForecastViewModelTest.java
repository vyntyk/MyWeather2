package com.home.myweather.ui.viewmodel;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.home.myweather.data.model.DailyData;
import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.data.model.ForecastResponse;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.repository.WeatherRepository;
import com.home.myweather.data.repository.WeatherStorage;
import com.home.myweather.data.repository.ForecastCache;
import com.home.myweather.utils.ForecastGrouper;

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
public class ForecastViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock WeatherRepository weatherRepository;
    @Mock WeatherStorage weatherStorage;
    @Mock ForecastGrouper forecastGrouper;

    private ForecastViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ForecastCache.clear();
        
        viewModel = new ForecastViewModel(weatherRepository, weatherStorage);
    }

    @Test
    public void setGeoLocation_callsLoadForecast() {
        GeoLocation geo = new GeoLocation();
        geo.lat = 55.75;
        geo.lon = 37.62;
        
        viewModel.setGeoLocation(geo);
        
        // Verify initial loading state
        ForecastUiState state = viewModel.uiState.getValue();
        assertNotNull(state);
    }

    @Test
    public void loadForecast_withCachedItems_usesCache() {
        // Setup cached items
        List<ForecastItem> cachedItems = new ArrayList<>();
        ForecastItem item = new ForecastItem();
        item.timestamp = 1000;
        cachedItems.add(item);
        ForecastCache.put(55.75, 37.62, cachedItems);
        
        // Mock grouper
        List<DailyData> grouped = new ArrayList<>();
        DailyData day = new DailyData();
        day.tempMin = 10.0;
        day.tempMax = 20.0;
        grouped.add(day);
        when(forecastGrouper.groupByDay(cachedItems)).thenReturn(grouped);
        
        viewModel.loadForecast(55.75, 37.62);
        
        ForecastUiState state = viewModel.uiState.getValue();
        assertNotNull(state);
        assertFalse(state.isLoading);
        assertEquals(1, state.days.size());
    }

    @Test
    public void loadForecast_withoutCache_showsLoading() {
        ForecastCache.clear();
        when(weatherStorage.loadHourly()).thenReturn(new ArrayList<>());
        when(weatherStorage.loadForecastGeo()).thenReturn(null);
        
        viewModel.loadForecast(55.75, 37.62);
        
        ForecastUiState state = viewModel.uiState.getValue();
        assertNotNull(state);
        assertTrue(state.isLoading);
    }

    @Test
    public void refresh_callsLoadForecast() throws InterruptedException {
        GeoLocation geo = new GeoLocation();
        geo.lat = 55.75;
        geo.lon = 37.62;
        viewModel.setGeoLocation(geo);
        
        // Mock API call
        Call<ForecastResponse> apiCall = mock(Call.class);
        when(weatherRepository.fetchForecast(anyDouble(), anyDouble(), any()))
            .thenAnswer(invocation -> {
                WeatherRepository.ForecastCallback cb = invocation.getArgument(2);
                ForecastResponse response = new ForecastResponse();
                response.list = new ArrayList<>();
                cb.onSuccess(response);
                return null;
            });
        
        CountDownLatch latch = new CountDownLatch(1);
        LiveData<ForecastUiState> liveData = viewModel.uiState;
        Observer<ForecastUiState> observer = state -> {
            if (state != null && !state.isLoading) {
                latch.countDown();
            }
        };
        liveData.observeForever(observer);
        
        viewModel.refresh();
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        liveData.removeObserver(observer);
    }

    @Test
    public void getCachedDays_returnsEmptyWhenNoGeo() {
        List<DailyData> cached = viewModel.getCachedDays();
        assertNotNull(cached);
        assertTrue(cached.isEmpty());
    }

    @Test
    public void onCleared_clearsCache() {
        GeoLocation geo = new GeoLocation();
        geo.lat = 55.75;
        geo.lon = 37.62;
        viewModel.setGeoLocation(geo);
        
        viewModel.onCleared();
        
        List<DailyData> cached = viewModel.getCachedDays();
        assertTrue(cached.isEmpty());
    }
}