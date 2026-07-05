package com.home.myweather;

import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.splashscreen.SplashScreen;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.home.myweather.data.model.DailyData;
import com.home.myweather.data.model.GeoLocation;
import com.home.myweather.data.repository.WeatherStorage;
import com.home.myweather.helpers.LocationHelper;
import com.home.myweather.ui.adapters.MainPagerAdapter;
import com.home.myweather.ui.fragments.DayDetailFragment;
import com.home.myweather.ui.fragments.ForecastFragment;
import com.home.myweather.ui.fragments.MapFragment;
import com.home.myweather.ui.fragments.NowFragment;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private static final String STATE_SELECTED_PAGE = "selected_page";
    private static final String STATE_LAST_GEO = "last_geo";

    private BottomNavigationView bottomNav;
    private LocationHelper     locationHelper;
    private WeatherStorage     weatherStorage;
    public GeoLocation         lastGeo;  // Made public for ForecastFragment and MapFragment
    private NavController      navController;

    // Соответствие позиций страниц и id пунктов меню
    private static final int[] NAV_IDS = {
            R.id.nav_now, R.id.nav_forecast, R.id.nav_map,
            R.id.nav_cities, R.id.nav_settings
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply splash screen
        SplashScreen.installSplashScreen(this);
        
        applyStoredTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationHelper = new LocationHelper(this);
        weatherStorage = new WeatherStorage(this);

        bottomNav = findViewById(R.id.bottom_nav);

        // Restore lastGeo from savedInstanceState
        if (savedInstanceState != null) {
            lastGeo = (GeoLocation) savedInstanceState.getSerializable(STATE_LAST_GEO);
        } else {
            // Try to load from WeatherStorage
            lastGeo = weatherStorage.loadGeo();
        }

        // Setup navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        // Handle navigation selection
        bottomNav.setOnItemSelectedListener(item -> {
            int navId = item.getItemId();
            if (navId == R.id.nav_now) {
                navController.navigate(R.id.nowDestination);
                return true;
            } else if (navId == R.id.nav_forecast) {
                navController.navigate(R.id.forecastDestination);
                return true;
            } else if (navId == R.id.nav_map) {
                navController.navigate(R.id.mapDestination);
                return true;
            } else if (navId == R.id.nav_cities) {
                navController.navigate(R.id.citiesDestination);
                return true;
            } else if (navId == R.id.nav_settings) {
                navController.navigate(R.id.settingsDestination);
                return true;
            }
            return false;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else if (navController.getCurrentDestination() != null && 
                           navController.getCurrentDestination().getId() != R.id.nowDestination) {
                    navController.navigate(R.id.nowDestination);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        hideSystemUI();

        // Слушатель для скрытия overlay-контейнера, когда стек фрагментов пуст
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                View c = findViewById(R.id.fragment_container);
                if (c != null) c.setVisibility(View.GONE);
            }
        });
    }

    private void applyStoredTheme() {
        SharedPreferences prefs = getSharedPreferences("myweather_prefs", 0);
        boolean isDark = prefs.getBoolean("dark_theme", false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
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
                    
                    // Update all visible fragments
                    NowFragment nf = getNowFragment();
                    ForecastFragment ff = getForecastFragment();
                    MapFragment mf = getMapFragment();
                    
                    if (nf != null) nf.loadWeatherByCoords(lat, lon);
                    if (ff != null) ff.setGeoLocation(lastGeo);
                    if (mf != null && mf.isAdded()) mf.moveToLocation(lat, lon);
                    
                    // Update bottom nav state
                    updateBottomNavFromNavController();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void updateBottomNavFromNavController() {
        if (navController != null) {
            int currentDestId = navController.getCurrentDestination().getId();
            if (currentDestId == R.id.nowDestination) {
                bottomNav.setSelectedItemId(R.id.nav_now);
            } else if (currentDestId == R.id.forecastDestination) {
                bottomNav.setSelectedItemId(R.id.nav_forecast);
            } else if (currentDestId == R.id.mapDestination) {
                bottomNav.setSelectedItemId(R.id.nav_map);
            } else if (currentDestId == R.id.citiesDestination) {
                bottomNav.setSelectedItemId(R.id.nav_cities);
            } else if (currentDestId == R.id.settingsDestination) {
                bottomNav.setSelectedItemId(R.id.nav_settings);
            }
        }
    }

    public void openDayDetail(DailyData day) {
        Bundle args = DayDetailFragment.newInstance(day).getArguments();
        navController.navigate(R.id.action_now_to_dayDetail, args);
    }

    public void loadWeatherFromFavoriteCity(String cityName) {
        NowFragment nf = getNowFragment();
        if (nf != null) nf.loadWeatherByCity(cityName);
        navController.navigate(R.id.nowDestination);
    }

    public void onWeatherLocationLoaded(GeoLocation geo) {
        if (geo == null) return;
        lastGeo = geo;
        ForecastFragment ff = getForecastFragment();
        if (ff != null) ff.setGeoLocation(geo);
    }

    public void refreshWeatherDisplay() {
        NowFragment nf = getNowFragment();
        ForecastFragment ff = getForecastFragment();
        if (nf != null) nf.refresh();
        if (ff != null) ff.refresh();
    }

    public GeoLocation getGeoLocation() {
        return lastGeo;
    }

    private NowFragment getNowFragment() {
        NowFragment f = (NowFragment) getSupportFragmentManager().findFragmentByTag("androidx.navigation_fragment:" + R.id.nowDestination);
        if (f == null) {
            // Fallback: try all fragments and check their arguments
            androidx.fragment.app.FragmentManager fragmentManager = getSupportFragmentManager();
            for (androidx.fragment.app.Fragment frag : fragmentManager.getFragments()) {
                if (frag instanceof NowFragment) {
                    f = (NowFragment) frag;
                    break;
                }
            }
        }
        return f;
    }

    private ForecastFragment getForecastFragment() {
        ForecastFragment f = (ForecastFragment) getSupportFragmentManager().findFragmentByTag("androidx.navigation_fragment:" + R.id.forecastDestination);
        if (f == null) {
            androidx.fragment.app.FragmentManager fragmentManager = getSupportFragmentManager();
            for (androidx.fragment.app.Fragment frag : fragmentManager.getFragments()) {
                if (frag instanceof ForecastFragment) {
                    f = (ForecastFragment) frag;
                    break;
                }
            }
        }
        return f;
    }

    private MapFragment getMapFragment() {
        MapFragment f = (MapFragment) getSupportFragmentManager().findFragmentByTag("androidx.navigation_fragment:" + R.id.mapDestination);
        if (f == null) {
            androidx.fragment.app.FragmentManager fragmentManager = getSupportFragmentManager();
            for (androidx.fragment.app.Fragment frag : fragmentManager.getFragments()) {
                if (frag instanceof MapFragment) {
                    f = (MapFragment) frag;
                    break;
                }
            }
        }
        return f;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (lastGeo != null) outState.putSerializable(STATE_LAST_GEO, lastGeo);
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
