package com.home.myweather.data.repository;

import static com.google.common.truth.Truth.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.model.OpenMeteoGeoResponse;
import com.home.myweather.data.network.GeocodingApiService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import org.robolectric.annotation.Config;

import org.junit.jupiter.api.Assertions;

import java.util.Optional;

@Config(sdk = 30)
class GeocodingRepositoryTest {

    @Mock GeocodingApiService geoService;
    @Mock Call<OpenMeteoGeoResponse> call;
    @Mock Response<OpenMeteoGeoResponse> response;

    private OpenMeteoGeoResponse responseBody;
    private OpenMeteoGeoResponse.Result result;

    @Captor ArgumentCaptor<Callback<OpenMeteoGeoResponse>> callbackCaptor;

    private GeocodingRepository repository;
    private GeocodingRepository.Callback2 testCallback;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        responseBody = new OpenMeteoGeoResponse();
        result = new OpenMeteoGeoResponse.Result();
        repository = new GeocodingRepository(geoService);
        
        testCallback = new GeocodingRepository.Callback2() {
            @Override public void onSuccess(GeoLocation geo) {}
            @Override public void onError(String message) {}
        };
    }

    @Test
    void fetch_success_returnsGeoLocation() {
        when(geoService.search("Moscow", 1, "ru", "json")).thenReturn(call);
        doNothing().when(call).enqueue(callbackCaptor.capture());
        
        when(response.isSuccessful()).thenReturn(true);
        when(response.body()).thenReturn(responseBody);
        responseBody.results = java.util.Collections.singletonList(result);
        result.latitude = 55.75;
        result.longitude = 37.62;
        result.name = "Moscow";
        result.countryCode = "RU";
        result.admin1 = "Moscow";

        repository.fetch("Moscow", 1L, testCallback);

        Callback<OpenMeteoGeoResponse> callback = callbackCaptor.getValue();
        callback.onResponse(call, response);
    }

    @Test
    void fetch_cityWithCountryCode_stripsCountry() {
        when(geoService.search("Moscow", 1, "ru", "json")).thenReturn(call);
        doNothing().when(call).enqueue(callbackCaptor.capture());

        repository.fetch("Moscow,RU", 1L, testCallback);

        verify(geoService).search(eq("Moscow"), eq(1), eq("ru"), eq("json"));
    }

    @Test
    void fetch_cityWithSpaces_trims() {
        when(geoService.search("Moscow", 1, "ru", "json")).thenReturn(call);
        doNothing().when(call).enqueue(callbackCaptor.capture());

        repository.fetch(" Moscow ", 1L, testCallback);

        verify(geoService).search(eq("Moscow"), eq(1), eq("ru"), eq("json"));
    }

    @Test
    void fetch_emptyResponse_onError() {
        when(geoService.search("UnknownCity", 1, "ru", "json")).thenReturn(call);
        doNothing().when(call).enqueue(callbackCaptor.capture());

        when(response.isSuccessful()).thenReturn(true);
        when(response.body()).thenReturn(responseBody);
        responseBody.results = null;

        GeocodingRepository.Callback2 spyCallback = spy(testCallback);
        repository.fetch("UnknownCity", 1L, spyCallback);

        Callback<OpenMeteoGeoResponse> callback = callbackCaptor.getValue();
        callback.onResponse(call, response);

        verify(spyCallback).onError(contains("Город не найден"));
    }

    @Test
    void fetch_unsuccessfulResponse_onError() {
        when(geoService.search("City", 1, "ru", "json")).thenReturn(call);
        doNothing().when(call).enqueue(callbackCaptor.capture());

        when(response.isSuccessful()).thenReturn(false);
        when(response.code()).thenReturn(404);

        GeocodingRepository.Callback2 spyCallback = spy(testCallback);
        repository.fetch("City", 1L, spyCallback);

        Callback<OpenMeteoGeoResponse> callback = callbackCaptor.getValue();
        callback.onResponse(call, response);

        verify(spyCallback).onError(contains("Город не найден"));
    }

    @Test
    void fetch_networkFailure_onError() {
        when(geoService.search("City", 1, "ru", "json")).thenReturn(call);
        doNothing().when(call).enqueue(callbackCaptor.capture());

        GeocodingRepository.Callback2 spyCallback = spy(testCallback);
        repository.fetch("City", 1L, spyCallback);

        Callback<OpenMeteoGeoResponse> callback = callbackCaptor.getValue();
        callback.onFailure(call, new java.io.IOException("Network error"));

        verify(spyCallback).onError(contains("Ошибка сети"));
    }

    @Test
    void fetch_canceledCall_doesNotCallback() {
        when(geoService.search("City", 1, "ru", "json")).thenReturn(call);
        doNothing().when(call).enqueue(callbackCaptor.capture());
        when(call.isCanceled()).thenReturn(true);

        GeocodingRepository.Callback2 spyCallback = spy(testCallback);
        repository.fetch("City", 1L, spyCallback);

        Callback<OpenMeteoGeoResponse> callback = callbackCaptor.getValue();
        callback.onResponse(call, response);

        verify(spyCallback, never()).onSuccess(any());
        verify(spyCallback, never()).onError(anyString());
    }

    @Test
    void fetch_onFailure_canceledCall_doesNotCallback() {
        when(geoService.search("City", 1, "ru", "json")).thenReturn(call);
        doNothing().when(call).enqueue(callbackCaptor.capture());
        when(call.isCanceled()).thenReturn(true);

        GeocodingRepository.Callback2 spyCallback = spy(testCallback);
        repository.fetch("City", 1L, spyCallback);

        Callback<OpenMeteoGeoResponse> callback = callbackCaptor.getValue();
        callback.onFailure(call, new java.io.IOException("Network error"));

        verify(spyCallback, never()).onSuccess(any());
        verify(spyCallback, never()).onError(anyString());
    }

    @Test
    void cancel_pendingCall_cancelsIt() {
        when(geoService.search("City", 1, "ru", "json")).thenReturn(call);
        doNothing().when(call).enqueue(callbackCaptor.capture());

        repository.fetch("City", 1L, testCallback);
        repository.cancel();

        verify(call).cancel();
    }

    @Test
    void cancel_noPendingCall_doesNothing() {
        repository.cancel();
    }

    @Test
    void cancel_alreadyCanceled_doesNotCallCancelAgain() {
        when(geoService.search("City", 1, "ru", "json")).thenReturn(call);
        doNothing().when(call).enqueue(callbackCaptor.capture());
        when(call.isCanceled()).thenReturn(true);

        repository.fetch("City", 1L, testCallback);
        repository.cancel();

        verify(call, never()).cancel();
    }

    @Test
    void fetch_populatesGeoLocationCorrectly() {
        when(geoService.search("Saint Petersburg", 1, "ru", "json")).thenReturn(call);
        doNothing().when(call).enqueue(callbackCaptor.capture());

        when(response.isSuccessful()).thenReturn(true);
        when(response.body()).thenReturn(responseBody);
        responseBody.results = java.util.Collections.singletonList(result);
        result.latitude = 59.93;
        result.longitude = 30.33;
        result.name = "Saint Petersburg";
        result.countryCode = "RU";
        result.admin1 = "Saint Petersburg";

        final GeoLocation[] captured = new GeoLocation[1];
        testCallback = new GeocodingRepository.Callback2() {
            @Override public void onSuccess(GeoLocation geo) { captured[0] = geo; }
            @Override public void onError(String message) { Assertions.fail("Should not error: " + message); }
        };

        repository.fetch("Saint Petersburg", 1L, testCallback);

        Callback<OpenMeteoGeoResponse> callback = callbackCaptor.getValue();
        callback.onResponse(call, response);

        assertThat(Optional.ofNullable(captured[0])).isNotNull();
        Assertions.assertNotNull(captured[0]);
        assertThat(captured[0].lat).isEqualTo(59.93);
        assertThat(captured[0].lon).isEqualTo(30.33);
        assertThat((CharSequence) captured[0].name).isEqualTo("Saint Petersburg");
        assertThat((CharSequence) captured[0].country).isEqualTo("RU");
        assertThat((CharSequence) captured[0].state).isEqualTo("Saint Petersburg");
    }
}