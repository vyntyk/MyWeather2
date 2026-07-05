package com.home.myweather.helpers;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

/**
 * Отвечает за запрос разрешений геолокации и получение текущих координат.
 * Результат передаёт через интерфейс Callback.
 */
public class LocationHelper {

    public interface Callback {
        void onLocationReady(double lat, double lon);
        void onError(String message);
    }

    private final AppCompatActivity activity;
    private final FusedLocationProviderClient fusedClient;
    private Callback pendingCallback;

    private final ActivityResultLauncher<String[]> permissionLauncher;

    public LocationHelper(AppCompatActivity activity) {
        this.activity    = activity;
        this.fusedClient = LocationServices.getFusedLocationProviderClient(activity);

        permissionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean granted =
                            Boolean.TRUE.equals(permissions.get(android.Manifest.permission.ACCESS_FINE_LOCATION))
                            || Boolean.TRUE.equals(permissions.get(android.Manifest.permission.ACCESS_COARSE_LOCATION));
                    if (granted && pendingCallback != null) {
                        getLocation(pendingCallback);
                    } else if (pendingCallback != null) {
                        pendingCallback.onError("Разрешение на геолокацию отклонено");
                    }
                });
    }

    public void requestLocation(Callback callback) {
        boolean hasFine   = hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
        boolean hasCoarse = hasPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasFine || hasCoarse) {
            getLocation(callback);
        } else {
            pendingCallback = callback;
            permissionLauncher.launch(new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocation(Callback callback) {
        // Сначала пробуем получить текущее местоположение
        fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener(activity, location -> {
                    if (location != null) {
                        callback.onLocationReady(location.getLatitude(), location.getLongitude());
                    } else {
                        // Если getCurrentLocation вернул null, пробуем getLastLocation()
                        fusedClient.getLastLocation().addOnSuccessListener(activity, lastLocation -> {
                            if (lastLocation != null) {
                                callback.onLocationReady(lastLocation.getLatitude(), lastLocation.getLongitude());
                            } else {
                                callback.onError("Не удалось определить позицию. Включите GPS.");
                            }
                        }).addOnFailureListener(activity, e ->
                                callback.onError("Ошибка геолокации: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(activity, e ->
                        callback.onError("Ошибка геолокации: " + e.getMessage()));
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_GRANTED;
    }
}
