package com.home.myweather.ui.viewmodel;

import static org.junit.Assert.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.model.WeatherResponse;
import com.home.myweather.testing.FakeWeatherRepository;
import com.home.myweather.utils.TestWeatherFactory;
import com.home.myweather.utils.WeatherTileLayer;

import kotlin.Pair;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class MapViewModelTest {

    private static final double DEFAULT_LAT = 55.751244;
    private static final double DEFAULT_LON = 37.618423;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private FakeWeatherRepository repository;
    private MapViewModel viewModel;

    @Before
    public void setUp() {
        repository = new FakeWeatherRepository();
        viewModel = new MapViewModel(repository);
    }

    @Test
    public void initialState_centersOnMoscow_withNoCard() {
        MapUiState state = viewModel.getUiState().getValue();

        assertNotNull(state);
        assertEquals(DEFAULT_LAT, state.getCenterLat(), 1e-6);
        assertEquals(DEFAULT_LON, state.getCenterLon(), 1e-6);
        assertFalse(state.getShowWeatherCard());
        assertNull(state.getWeather());
        assertNull(state.getActiveLayer());
    }

    @Test
    public void moveToLocation_updatesCenter() {
        viewModel.moveToLocation(59.93, 30.33);

        MapUiState state = viewModel.getUiState().getValue();
        assertEquals(59.93, state.getCenterLat(), 1e-6);
        assertEquals(30.33, state.getCenterLon(), 1e-6);
    }

    @Test
    public void fetchWeatherForPoint_onSuccess_showsWeatherCardWithCityName() {
        WeatherResponse weather = new WeatherResponse();
        GeoLocation geo = new GeoLocation();
        geo.name = "Kazan";
        repository.onCoords = FakeWeatherRepository.successWith(weather, geo);

        viewModel.fetchWeatherForPoint(55.79, 49.12);
        idle();

        MapUiState state = viewModel.getUiState().getValue();
        assertTrue(state.getShowWeatherCard());
        assertSame(weather, state.getWeather());
        assertEquals("Kazan", state.getCityName());
    }

    @Test
    public void fetchWeatherForPoint_onError_doesNotShowCard() {
        repository.onCoords = FakeWeatherRepository.failWith("Network error");

        viewModel.fetchWeatherForPoint(55.79, 49.12);
        idle();

        assertEquals(1, repository.coordsCalls.size());
        assertFalse(viewModel.getUiState().getValue().getShowWeatherCard());
    }

    @Test
    public void showWeatherCard_thenHide_togglesCard() {
        WeatherResponse weather = new WeatherResponse();

        viewModel.showWeatherCard(weather, "Berlin");
        MapUiState shown = viewModel.getUiState().getValue();
        assertTrue(shown.getShowWeatherCard());
        assertSame(weather, shown.getWeather());
        assertEquals("Berlin", shown.getCityName());

        viewModel.hideWeatherCard();
        assertFalse(viewModel.getUiState().getValue().getShowWeatherCard());
    }

    @Test
    public void switchLayer_setsActiveLayer_andClearsWithNull() {
        viewModel.switchLayer(WeatherTileLayer.Layer.WIND);
        assertEquals(WeatherTileLayer.Layer.WIND, viewModel.getUiState().getValue().getActiveLayer());

        viewModel.switchLayer(null);
        assertNull(viewModel.getUiState().getValue().getActiveLayer());
    }

    @Test
    public void clearTemperatureMarkers_emptiesMarkers() {
        viewModel.clearTemperatureMarkers();

        assertTrue(viewModel.getUiState().getValue().getTemperatureMarkers().isEmpty());
    }

    @Test
    public void loadTemperatureMarkers_emptyList_stopsLoadingImmediately() {
        viewModel.loadTemperatureMarkers(Collections.emptyList());

        MapUiState state = viewModel.getUiState().getValue();
        assertFalse(state.isLoadingMarkers());
        assertTrue(state.getTemperatureMarkers().isEmpty());
        assertTrue(repository.markerCalls.isEmpty());
    }

    @Test
    public void loadTemperatureMarkers_usesMarkerRequests_notCoordinateFetch() {
        repository.onMarker = FakeWeatherRepository.failWith("Network error");

        viewModel.loadTemperatureMarkers(twoCities());
        idle();

        assertEquals(2, repository.markerCalls.size());
        assertTrue(repository.coordsCalls.isEmpty());
        assertEquals(0, repository.cancelCalls);
    }

    @Test
    public void loadTemperatureMarkers_collectsMarkersFromSuccessfulResponses() throws Exception {
        WeatherResponse warm = TestWeatherFactory.createWeather(10.0);
        WeatherResponse cold = TestWeatherFactory.createWeather(-3.5);
        repository.onMarker = (lat, lon, cb) ->
                cb.onSuccess(lat > 56.0 ? cold : warm, new GeoLocation());

        viewModel.loadTemperatureMarkers(twoCities());
        idle();

        MapUiState state = viewModel.getUiState().getValue();
        assertFalse(state.isLoadingMarkers());
        List<TempMarker> markers = state.getTemperatureMarkers();
        assertEquals(2, markers.size());
        assertTrue(markers.stream().anyMatch(m -> m.getLat() == 55.75 && m.getTemp() == 10.0));
        assertTrue(markers.stream().anyMatch(m -> m.getLat() == 59.93 && m.getTemp() == -3.5));
    }

    @Test
    public void loadTemperatureMarkers_failedPoint_isSkippedWithoutBlockingOthers() throws Exception {
        WeatherResponse weather = TestWeatherFactory.createWeather(7.0);
        repository.onMarker = (lat, lon, cb) -> {
            if (lat > 56.0) {
                cb.onError("Network error");
            } else {
                cb.onSuccess(weather, new GeoLocation());
            }
        };

        viewModel.loadTemperatureMarkers(twoCities());
        idle();

        MapUiState state = viewModel.getUiState().getValue();
        assertFalse(state.isLoadingMarkers());
        assertEquals(1, state.getTemperatureMarkers().size());
        assertEquals(55.75, state.getTemperatureMarkers().get(0).getLat(), 1e-6);
    }

    @Test
    public void loadTemperatureMarkers_keepsMapCenterAndWeatherCard() throws Exception {
        viewModel.moveToLocation(59.93, 30.33);
        viewModel.showWeatherCard(new WeatherResponse(), "Saint Petersburg");
        WeatherResponse weather = TestWeatherFactory.createWeather(5.0);
        repository.onMarker = (lat, lon, cb) ->
                cb.onSuccess(weather, new GeoLocation());

        viewModel.loadTemperatureMarkers(Collections.singletonList(new Pair<>(55.75, 37.62)));
        idle();

        MapUiState state = viewModel.getUiState().getValue();
        assertEquals(59.93, state.getCenterLat(), 1e-6);
        assertEquals(30.33, state.getCenterLon(), 1e-6);
        assertTrue(state.getShowWeatherCard());
        assertEquals("Saint Petersburg", state.getCityName());
    }

    @Test
    public void moveToLocation_keepsLayerAndWeatherCard() {
        viewModel.switchLayer(WeatherTileLayer.Layer.CLOUDS);
        viewModel.showWeatherCard(new WeatherResponse(), "Berlin");

        viewModel.moveToLocation(59.93, 30.33);

        MapUiState state = viewModel.getUiState().getValue();
        assertEquals(59.93, state.getCenterLat(), 1e-6);
        assertEquals(WeatherTileLayer.Layer.CLOUDS, state.getActiveLayer());
        assertTrue(state.getShowWeatherCard());
        assertEquals("Berlin", state.getCityName());
    }

    @Test
    public void switchLayer_keepsMapCenterAndWeatherCard() {
        viewModel.moveToLocation(59.93, 30.33);
        viewModel.showWeatherCard(new WeatherResponse(), "Berlin");

        viewModel.switchLayer(WeatherTileLayer.Layer.PRECIPITATION);

        MapUiState state = viewModel.getUiState().getValue();
        assertEquals(59.93, state.getCenterLat(), 1e-6);
        assertEquals(30.33, state.getCenterLon(), 1e-6);
        assertEquals(WeatherTileLayer.Layer.PRECIPITATION, state.getActiveLayer());
        assertTrue(state.getShowWeatherCard());
    }

    @Test
    public void hideWeatherCard_keepsCenterAndLayer() {
        viewModel.moveToLocation(59.93, 30.33);
        viewModel.switchLayer(WeatherTileLayer.Layer.WIND);
        viewModel.showWeatherCard(new WeatherResponse(), "Berlin");

        viewModel.hideWeatherCard();

        MapUiState state = viewModel.getUiState().getValue();
        assertFalse(state.getShowWeatherCard());
        assertEquals(59.93, state.getCenterLat(), 1e-6);
        assertEquals(WeatherTileLayer.Layer.WIND, state.getActiveLayer());
    }

    @Test
    public void consecutiveOperations_doNotLoseEarlierChanges() {
        viewModel.moveToLocation(59.93, 30.33);
        viewModel.switchLayer(WeatherTileLayer.Layer.TEMPERATURE);
        viewModel.clearTemperatureMarkers();

        MapUiState state = viewModel.getUiState().getValue();
        assertEquals(59.93, state.getCenterLat(), 1e-6);
        assertEquals(WeatherTileLayer.Layer.TEMPERATURE, state.getActiveLayer());
    }

    @Test
    public void fetchWeatherForPoint_keepsMapCenterAndLayer() {
        GeoLocation geo = new GeoLocation();
        geo.name = "Kazan";
        repository.onCoords = FakeWeatherRepository.successWith(new WeatherResponse(), geo);

        viewModel.moveToLocation(59.93, 30.33);
        viewModel.switchLayer(WeatherTileLayer.Layer.WIND);
        viewModel.fetchWeatherForPoint(55.79, 49.12);
        idle();

        MapUiState state = viewModel.getUiState().getValue();
        assertEquals(59.93, state.getCenterLat(), 1e-6);
        assertEquals(WeatherTileLayer.Layer.WIND, state.getActiveLayer());
        assertEquals("Kazan", state.getCityName());
    }

    private static List<Pair<Double, Double>> twoCities() {
        return Arrays.asList(
                new Pair<>(55.75, 37.62),
                new Pair<>(59.93, 30.33)
        );
    }

    /** Дожидается выполнения отложенной работы ViewModel на главном потоке. */
    private static void idle() {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }
}
