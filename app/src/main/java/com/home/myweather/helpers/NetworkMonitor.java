package com.home.myweather.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import javax.inject.Inject;

/**
 * Проверка наличия активного интернет-соединения.
 *
 * Используется репозиторием, чтобы не отправлять запросы к API (и не жечь
 * трафик и время пользователя), когда сети нет: в этом случае отдаются
 * данные, сохранённые в {@link com.home.myweather.data.repository.WeatherStorage}.
 */
public final class NetworkMonitor {

    private final ConnectivityManager connectivityManager;

    @Inject
    public NetworkMonitor(Context context) {
        this.connectivityManager = (ConnectivityManager)
                context.getApplicationContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    /**
     * @return true, если есть активная сеть с подтверждённым доступом в интернет.
     */
    public boolean isOnline() {
        if (connectivityManager == null) return false;
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) return false;
        NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(activeNetwork);
        return caps != null
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }
}