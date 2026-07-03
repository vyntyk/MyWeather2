package com.home.myweather;

import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewpager2.widget.ViewPager2;

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

public class MainActivity extends AppCompatActivity {

    private static final String STATE_SELECTED_PAGE = "selected_page";
    private static final String STATE_LAST_GEO = "last_geo";

    private ViewPager2         viewPager;
    private BottomNavigationView bottomNav;
    private MainPagerAdapter   pagerAdapter;
    private LocationHelper     locationHelper;
    private WeatherStorage     weatherStorage;
    private GeoLocation        lastGeo;

    // Соответствие позиций страниц и id пунктов меню
    private static final int[] NAV_IDS = {
            R.id.nav_now, R.id.nav_forecast, R.id.nav_map,
            R.id.nav_cities, R.id.nav_settings
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyStoredTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationHelper = new LocationHelper(this);
        weatherStorage = new WeatherStorage(this);

        viewPager = findViewById(R.id.view_pager);
        bottomNav = findViewById(R.id.bottom_nav);

        pagerAdapter = new MainPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        // Предзагружаем соседние страницы, чтобы свайп был плавным
        viewPager.setOffscreenPageLimit(2);

        if (savedInstanceState != null) {
            lastGeo = (GeoLocation) savedInstanceState.getSerializable(STATE_LAST_GEO);
            int page = savedInstanceState.getInt(STATE_SELECTED_PAGE, 0);
            viewPager.setCurrentItem(page, false);
            bottomNav.setSelectedItemId(NAV_IDS[page]);
        } else {
            lastGeo = weatherStorage.loadGeo();
        }

        // ViewPager → BottomNav
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                bottomNav.setSelectedItemId(NAV_IDS[position]);
                onPageActivated(position);
            }
        });

        // BottomNav → ViewPager
        bottomNav.setOnItemSelectedListener(item -> {
            int page = navIdToPage(item.getItemId());
            if (page >= 0) {
                viewPager.setCurrentItem(page, true);
                return true;
            }
            return false;
        });

        // Карта не поддерживает свайп внутри ViewPager — отключаем свайп только на странице карты
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                viewPager.setUserInputEnabled(position != MainPagerAdapter.PAGE_MAP);
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else if (viewPager.getCurrentItem() != 0) {
                    viewPager.setCurrentItem(0, true);
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

    /** Вызывается когда страница стала активной (через свайп или nav). */
    private void onPageActivated(int position) {
        if (position == MainPagerAdapter.PAGE_FORECAST) {
            ForecastFragment ff = pagerAdapter.getForecastFragment();
            if (lastGeo != null) {
                ff.setGeoLocation(lastGeo);
            } else {
                ff.showPlaceholder();
            }
        }
    }

    private int navIdToPage(int navId) {
        for (int i = 0; i < NAV_IDS.length; i++) {
            if (NAV_IDS[i] == navId) return i;
        }
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
                    pagerAdapter.getNowFragment().loadWeatherByCoords(lat, lon);
                    pagerAdapter.getForecastFragment().setGeoLocation(lastGeo);
                    MapFragment mf = pagerAdapter.getMapFragment();
                    if (mf.isAdded()) mf.moveToLocation(lat, lon);
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
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, DayDetailFragment.newInstance(day))
                .addToBackStack("day_detail")
                .commit();
            View container = findViewById(R.id.fragment_container);
            if (container != null) container.setVisibility(View.VISIBLE);
    }

    public void loadWeatherFromFavoriteCity(String cityName) {
        pagerAdapter.getNowFragment().loadWeatherByCity(cityName);
        viewPager.setCurrentItem(MainPagerAdapter.PAGE_NOW, true);
    }

    public void onWeatherLocationLoaded(GeoLocation geo) {
        if (geo == null) return;
        lastGeo = geo;
        pagerAdapter.getForecastFragment().setGeoLocation(geo);
    }

    public void refreshWeatherDisplay() {
        pagerAdapter.getNowFragment().refresh();
        pagerAdapter.getForecastFragment().refresh();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_PAGE, viewPager.getCurrentItem());
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
