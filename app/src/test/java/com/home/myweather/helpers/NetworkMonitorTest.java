package com.home.myweather.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class NetworkMonitorTest {

    @Mock Context context;
    @Mock Context applicationContext;
    @Mock ConnectivityManager connectivityManager;
    @Mock Network activeNetwork;
    @Mock NetworkCapabilities caps;

    private NetworkMonitor networkMonitor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(context.getApplicationContext()).thenReturn(applicationContext);
        when(applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE))
                .thenReturn(connectivityManager);
        networkMonitor = new NetworkMonitor(context);
    }

    @Test
    void isOnline_networkCapabilitiesNull_returnsFalse() {
        when(connectivityManager.getActiveNetwork()).thenReturn(activeNetwork);
        when(connectivityManager.getNetworkCapabilities(activeNetwork)).thenReturn(null);

        assertThat(networkMonitor.isOnline()).isFalse();
    }

    @Test
    void isOnline_hasInternetCapability_returnsTrue() {
        when(connectivityManager.getActiveNetwork()).thenReturn(activeNetwork);
        when(connectivityManager.getNetworkCapabilities(activeNetwork)).thenReturn(caps);
        when(caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(true);

        assertThat(networkMonitor.isOnline()).isTrue();
    }

    @Test
    void isOnline_noInternetCapabilityButCapsExist_returnsTrue_fallback() {
        when(connectivityManager.getActiveNetwork()).thenReturn(activeNetwork);
        when(connectivityManager.getNetworkCapabilities(activeNetwork)).thenReturn(caps);
        when(caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(false);

        assertThat(networkMonitor.isOnline()).isTrue();
    }

    @Test
    void isOnline_noInternetCapabilityAndNoCaps_returnsFalse() {
        when(connectivityManager.getActiveNetwork()).thenReturn(activeNetwork);
        when(connectivityManager.getNetworkCapabilities(activeNetwork)).thenReturn(null);

        assertThat(networkMonitor.isOnline()).isFalse();
    }
}