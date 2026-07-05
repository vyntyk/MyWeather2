package com.home.myweather.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.home.myweather.ui.fragments.CitiesFragment;
import com.home.myweather.ui.fragments.ForecastFragment;
import com.home.myweather.ui.fragments.MapFragment;
import com.home.myweather.ui.fragments.NowFragment;
import com.home.myweather.ui.fragments.SettingsFragment;

public class MainPagerAdapter extends FragmentStateAdapter {

    public static final int PAGE_NOW      = 0;
    public static final int PAGE_FORECAST = 1;
    public static final int PAGE_MAP      = 2;
    public static final int PAGE_CITIES   = 3;
    public static final int PAGE_SETTINGS = 4;
    public static final int PAGE_COUNT    = 5;

    // Держим ссылки, чтобы MainActivity могла обращаться к фрагментам
    private final NowFragment      nowFragment      = new NowFragment();
    private final ForecastFragment forecastFragment = new ForecastFragment();
    private final MapFragment      mapFragment      = new MapFragment();
    private final CitiesFragment   citiesFragment   = new CitiesFragment();
    private final SettingsFragment settingsFragment = new SettingsFragment();

    public MainPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case PAGE_NOW:      return nowFragment;
            case PAGE_FORECAST: return forecastFragment;
            case PAGE_MAP:      return mapFragment;
            case PAGE_CITIES:   return citiesFragment;
            case PAGE_SETTINGS: return settingsFragment;
            default: throw new IllegalArgumentException("Unknown page: " + position);
        }
    }

    @Override
    public int getItemCount() { return PAGE_COUNT; }

    public NowFragment      getNowFragment()      { return nowFragment; }
    public ForecastFragment getForecastFragment() { return forecastFragment; }
    public MapFragment      getMapFragment()      { return mapFragment; }
}
