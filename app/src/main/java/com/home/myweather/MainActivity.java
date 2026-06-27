package com.home.myweather;

import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_NOW = "now";
    private static final String TAG_FORECAST = "forecast";
    private static final String TAG_MAP = "map";
    private static final String TAG_CITIES = "cities";
    private static final String TAG_SETTINGS = "settings";
    private static final String STATE_SELECTED_NAV = "selected_nav";
    private static final String STATE_LAST_GEO = "last_geo";

    private NowFragment nowFragment;
    private ForecastFragment forecastFragment;
    private MapFragment mapFragment;
    private CitiesFragment citiesFragment;
    private SettingsFragment settingsFragment;

    private LocationHelper locationHelper;
    private GeoLocation lastGeo;
    private int selectedNavItemId = R.id.nav_now;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationHelper = new LocationHelper(this);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        restoreFragments();

        if (savedInstanceState != null) {
            selectedNavItemId = savedInstanceState.getInt(STATE_SELECTED_NAV, R.id.nav_now);
            lastGeo = (GeoLocation) savedInstanceState.getSerializable(STATE_LAST_GEO);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            selectedNavItemId = item.getItemId();
            getSupportFragmentManager().popBackStack();
            showSelectedFragment(selectedNavItemId);
            return true;
        });

        if (bottomNav.getSelectedItemId() == selectedNavItemId) {
            showSelectedFragment(selectedNavItemId);
        } else {
            bottomNav.setSelectedItemId(selectedNavItemId);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else if (selectedNavItemId != R.id.nav_now) {
                    bottomNav.setSelectedItemId(R.id.nav_now);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        hideSystemUI();
    }

    private void restoreFragments() {
        nowFragment = (NowFragment) getSupportFragmentManager().findFragmentByTag(TAG_NOW);
        forecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentByTag(TAG_FORECAST);
        mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(TAG_MAP);
        citiesFragment = (CitiesFragment) getSupportFragmentManager().findFragmentByTag(TAG_CITIES);
        settingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag(TAG_SETTINGS);

        if (nowFragment == null) nowFragment = new NowFragment();
        if (forecastFragment == null) forecastFragment = new ForecastFragment();
        if (mapFragment == null) mapFragment = new MapFragment();
        if (citiesFragment == null) citiesFragment = new CitiesFragment();
        if (settingsFragment == null) settingsFragment = new SettingsFragment();
    }

    private void showSelectedFragment(int id) {
        if (id == R.id.nav_now) {
            showFragment(nowFragment, TAG_NOW);
        } else if (id == R.id.nav_forecast) {
            showFragment(forecastFragment, TAG_FORECAST);
            if (lastGeo != null) {
                forecastFragment.setGeoLocation(lastGeo);
            } else {
                forecastFragment.showPlaceholder();
            }
        } else if (id == R.id.nav_map) {
            showFragment(mapFragment, TAG_MAP);
        } else if (id == R.id.nav_cities) {
            showFragment(citiesFragment, TAG_CITIES);
        } else if (id == R.id.nav_settings) {
            showFragment(settingsFragment, TAG_SETTINGS);
        }
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
                runOnUiThread(() -> {
                    lastGeo = new GeoLocation();
                    lastGeo.lat = lat;
                    lastGeo.lon = lon;
                    lastGeo.name = "GPS";
                    nowFragment.loadWeatherByCoords(lat, lon);
                    forecastFragment.setGeoLocation(lastGeo);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show());
            }
        });
    }

    public void openDayDetail(DailyData day) {
        selectedNavItemId = R.id.nav_forecast;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, DayDetailFragment.newInstance(day))
                .addToBackStack("day_detail")
                .commit();
    }

    public void loadWeatherFromFavoriteCity(String cityName) {
        if (nowFragment != null) {
            nowFragment.loadWeatherByCity(cityName);
        }
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_now);
    }

    public void onWeatherLocationLoaded(GeoLocation geo) {
        if (geo == null) return;
        lastGeo = geo;
        forecastFragment.setGeoLocation(geo);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_NAV, selectedNavItemId);
        if (lastGeo != null) {
            outState.putSerializable(STATE_LAST_GEO, lastGeo);
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
