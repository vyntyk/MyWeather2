package com.home.myweather;

import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Главная Activity с BottomNavigationView и 5 фрагментами.
 * Сохраняет lastGeo при пересоздании и передаёт между фрагментами.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG_NOW = "now";
    private static final String TAG_FORECAST = "forecast";
    private static final String TAG_MAP = "map";
    private static final String TAG_CITIES = "cities";
    private static final String TAG_SETTINGS = "settings";

    private NowFragment nowFragment;
    private ForecastFragment forecastFragment;
    private MapFragment mapFragment;
    private CitiesFragment citiesFragment;
    private SettingsFragment settingsFragment;

    private LocationHelper locationHelper;
    private GeoLocation lastGeo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationHelper = new LocationHelper(this);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        if (savedInstanceState == null) {
            nowFragment = new NowFragment();
            forecastFragment = new ForecastFragment();
            mapFragment = new MapFragment();
            citiesFragment = new CitiesFragment();
            settingsFragment = new SettingsFragment();

            showFragment(nowFragment, TAG_NOW);
            bottomNav.setSelectedItemId(R.id.nav_now);
        } else {
            // Восстанавливаем фрагменты из FragmentManager
            nowFragment = (NowFragment) getSupportFragmentManager().findFragmentByTag(TAG_NOW);
            forecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentByTag(TAG_FORECAST);
            mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(TAG_MAP);
            citiesFragment = (CitiesFragment) getSupportFragmentManager().findFragmentByTag(TAG_CITIES);
            settingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag(TAG_SETTINGS);

            // Восстанавливаем координаты
            double lat = savedInstanceState.getDouble("last_lat", Double.NaN);
            double lon = savedInstanceState.getDouble("last_lon", Double.NaN);
            String name = savedInstanceState.getString("last_name", null);
            if (!Double.isNaN(lat) && !Double.isNaN(lon)) {
                lastGeo = new GeoLocation();
                lastGeo.lat = lat;
                lastGeo.lon = lon;
                lastGeo.name = name;
            }
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_now) {
                showFragment(nowFragment, TAG_NOW);
                return true;
            } else if (id == R.id.nav_forecast) {
                showFragment(forecastFragment, TAG_FORECAST);
                if (lastGeo != null) {
                    forecastFragment.setGeoLocation(lastGeo);
                } else {
                    forecastFragment.showPlaceholder();
                }
                return true;
            } else if (id == R.id.nav_map) {
                showFragment(mapFragment, TAG_MAP);
                return true;
            } else if (id == R.id.nav_cities) {
                showFragment(citiesFragment, TAG_CITIES);
                return true;
            } else if (id == R.id.nav_settings) {
                showFragment(settingsFragment, TAG_SETTINGS);
                return true;
            }
            return false;
        });

        hideSystemUI();
    }

    private void showFragment(@NonNull Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, tag)
                .commit();
    }

    public void requestGeoLocation() {
        locationHelper.requestLocation(new LocationHelper.Callback() {
            @Override
            public void onLocationReady(double lat, double lon) {
                lastGeo = new GeoLocation();
                lastGeo.lat = lat;
                lastGeo.lon = lon;
                lastGeo.name = "GPS";
                nowFragment.loadWeatherByCoords(lat, lon);
            }
            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show());
            }
        });
    }

    public void openDayDetail(DailyData day) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, DayDetailFragment.newInstance(day))
                .addToBackStack("day_detail")
                .commit();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (lastGeo != null) {
            outState.putDouble("last_lat", lastGeo.lat);
            outState.putDouble("last_lon", lastGeo.lon);
            outState.putString("last_name", lastGeo.name);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUI();
    }

    private void hideSystemUI() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            WindowInsetsController c = getWindow().getInsetsController();
            if (c != null) {
                c.hide(WindowInsets.Type.systemBars());
                c.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }
}
