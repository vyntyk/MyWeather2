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

    public MainPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case PAGE_NOW:      return new NowFragment();
            case PAGE_FORECAST: return new ForecastFragment();
            case PAGE_MAP:      return new MapFragment();
            case PAGE_CITIES:   return new CitiesFragment();
            case PAGE_SETTINGS: return new SettingsFragment();
            default: throw new IllegalArgumentException("Unknown page: " + position);
        }
    }

    @Override
    public int getItemCount() { return PAGE_COUNT; }
}
