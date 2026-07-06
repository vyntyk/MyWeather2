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
import androidx.viewpager2.widget.ViewPager2;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;

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
import com.home.myweather.ui.fragments.CitiesFragment;
import com.home.myweather.ui.fragments.SettingsFragment;
import dagger.hilt.android.AndroidEntryPoint;

import java.util.ArrayList;
import java.util.List;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private static final String STATE_SELECTED_PAGE = "selected_page";
    private static final String STATE_LAST_GEO = "last_geo";
    private static final String STATE_LAST_GEO_SOURCE = "last_geo_source";

    private BottomNavigationView bottomNav;
    private ViewPager2 viewPager;
    private LocationHelper locationHelper;
    private WeatherStorage weatherStorage;
    public GeoLocation lastGeo;
    public String lastGeoSource = "GPS";
    private MainPagerAdapter pagerAdapter;
    private int currentPage = 0;
    private ForecastFragment forecastFragment;
    private MapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        
        applyStoredTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationHelper = new LocationHelper(this);
        locationHelper.init(this);
        weatherStorage = new WeatherStorage(this);

        bottomNav = findViewById(R.id.bottom_nav);
        viewPager = findViewById(R.id.view_pager);

        if (savedInstanceState != null) {
            lastGeo = (GeoLocation) savedInstanceState.getSerializable(STATE_LAST_GEO);
            lastGeoSource = savedInstanceState.getString(STATE_LAST_GEO_SOURCE, "GPS");
            currentPage = savedInstanceState.getInt(STATE_SELECTED_PAGE, 0);
        } else {
            lastGeo = weatherStorage.loadGeo();
        }

        pagerAdapter = new MainPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setPageTransformer(new com.home.myweather.ui.adapters.SmoothPageTransformer());
        viewPager.setOffscreenPageLimit(4);

        bottomNav.setOnItemSelectedListener(item -> {
            int position = getTabPositionFromItemId(item.getItemId());
            if (position >= 0 && position < pagerAdapter.getItemCount()) {
                viewPager.setCurrentItem(position, false);
                syncFragmentsAtPage(position);
                return true;
            }
            return false;
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPage = position;
                int navId = getNavItemIdFromPosition(position);
                if (navId != -1) {
                    bottomNav.setSelectedItemId(navId);
                }
                syncFragmentsAtPage(position);
                preloadAdjacentFragments(position);
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (viewPager.getCurrentItem() != 0) {
                    viewPager.setCurrentItem(0, true);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        viewPager.setCurrentItem(currentPage, false);
        syncFragmentsAtPage(currentPage);
        preloadAdjacentFragments(currentPage);

        hideSystemUI();
    }

    private void syncFragmentsAtPage(int position) {
        if (lastGeo == null) return;

        NowFragment nf = getNowFragment();
        ForecastFragment ff = getForecastFragment();
        MapFragment mf = getMapFragment();

        switch (position) {
            case 0: // Now
                if (nf != null && nf.isAdded()) {
                    nf.loadWeatherByCoords(lastGeo.lat, lastGeo.lon);
                }
                break;
            case 1: // Forecast
                if (ff != null && ff.isAdded()) {
                    ff.setGeoLocation(lastGeo, lastGeoSource);
                }
                break;
            case 2: // Map
                if (mf != null && mf.isAdded()) {
                    mf.moveToLocation(lastGeo.lat, lastGeo.lon);
                    mf.updateWeatherCard(null, lastGeo.name);
                }
                break;
        }
    }

    private void preloadAdjacentFragments(int position) {
        if (position == 0) {
            // From Now, preload Forecast and Map
            ForecastFragment ff = getForecastFragment();
            MapFragment mf = getMapFragment();
            if (ff != null && ff.isAdded() && lastGeo != null) {
                ff.setGeoLocation(lastGeo, lastGeoSource);
            }
            if (mf != null && mf.isAdded() && lastGeo != null) {
                mf.moveToLocation(lastGeo.lat, lastGeo.lon);
            }
        } else if (position == 1) {
            // From Forecast, preload Map
            MapFragment mf = getMapFragment();
            if (mf != null && mf.isAdded() && lastGeo != null) {
                mf.moveToLocation(lastGeo.lat, lastGeo.lon);
            }
        }
    }

    private int getTabPositionFromItemId(int itemId) {
        if (itemId == R.id.nav_now) return 0;
        if (itemId == R.id.nav_forecast) return 1;
        if (itemId == R.id.nav_map) return 2;
        if (itemId == R.id.nav_cities) return 3;
        if (itemId == R.id.nav_settings) return 4;
        return -1;
    }

    private int getNavItemIdFromPosition(int position) {
        if (position == 0) return R.id.nav_now;
        if (position == 1) return R.id.nav_forecast;
        if (position == 2) return R.id.nav_map;
        if (position == 3) return R.id.nav_cities;
        if (position == 4) return R.id.nav_settings;
        return -1;
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
                    lastGeoSource = "GPS";
                    
                    weatherStorage.saveGeo(lastGeo);
                    
                    NowFragment nf = getNowFragment();
                    ForecastFragment ff = getForecastFragment();
                    MapFragment mf = getMapFragment();
                    
                    if (nf != null && nf.isAdded()) {
                        nf.loadWeatherByCoords(lat, lon);
                    }
                    if (ff != null && ff.isAdded()) {
                        ff.setGeoLocation(lastGeo, "GPS");
                    }
                    if (mf != null && mf.isAdded()) {
                        mf.moveToLocation(lat, lon);
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show());
            }
        });
    }

    public void openDayDetail(DailyData day) {
        DayDetailFragment fragment = DayDetailFragment.newInstance(day);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
    }

    public void loadWeatherFromFavoriteCity(String cityName) {
        NowFragment nf = getNowFragment();
        if (nf != null) nf.loadWeatherByCity(cityName);
        viewPager.setCurrentItem(0, true);
    }

    public void onWeatherLocationLoaded(GeoLocation geo, String source) {
        if (geo == null) return;
        lastGeo = geo;
        lastGeoSource = source != null ? source : "GPS";
        weatherStorage.saveGeo(geo);
        
        ForecastFragment ff = getForecastFragment();
        MapFragment mf = getMapFragment();
        
        if (ff != null && ff.isAdded()) {
            ff.setGeoLocation(geo, source);
        }
        if (mf != null && mf.isAdded()) {
            mf.moveToLocation(geo.lat, geo.lon);
        }
    }

    public void refreshWeatherDisplay() {
        NowFragment nf = getNowFragment();
        ForecastFragment ff = getForecastFragment();
        MapFragment mf = getMapFragment();
        
        if (nf != null) nf.refresh();
        if (ff != null) ff.refresh();
        if (mf != null && lastGeo != null) {
            mf.moveToLocation(lastGeo.lat, lastGeo.lon);
        }
    }

    public GeoLocation getGeoLocation() {
        return lastGeo;
    }

    private NowFragment getNowFragment() {
        Fragment f = pagerAdapter.getFragmentAt(0);
        return f instanceof NowFragment ? (NowFragment) f : null;
    }

    private ForecastFragment getForecastFragment() {
        Fragment f = pagerAdapter.getFragmentAt(1);
        return f instanceof ForecastFragment ? (ForecastFragment) f : null;
    }

    private MapFragment getMapFragment() {
        Fragment f = pagerAdapter.getFragmentAt(2);
        return f instanceof MapFragment ? (MapFragment) f : null;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (lastGeo != null) outState.putSerializable(STATE_LAST_GEO, lastGeo);
        outState.putString(STATE_LAST_GEO_SOURCE, lastGeoSource);
        outState.putInt(STATE_SELECTED_PAGE, currentPage);
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
