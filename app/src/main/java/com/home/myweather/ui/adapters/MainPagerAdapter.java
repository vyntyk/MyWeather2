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

import java.util.HashMap;
import java.util.Map;

public class MainPagerAdapter extends FragmentStateAdapter {

    public static final int PAGE_NOW      = 0;
    public static final int PAGE_FORECAST = 1;
    public static final int PAGE_MAP      = 2;
    public static final int PAGE_CITIES   = 3;
    public static final int PAGE_SETTINGS = 4;
    public static final int PAGE_COUNT    = 5;

    private final Map<Integer, Fragment> fragments = new HashMap<>();

    public MainPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position) {
            case PAGE_NOW:      fragment = new NowFragment(); break;
            case PAGE_FORECAST: fragment = new ForecastFragment(); break;
            case PAGE_MAP:      fragment = new MapFragment(); break;
            case PAGE_CITIES:   fragment = new CitiesFragment(); break;
            case PAGE_SETTINGS: fragment = new SettingsFragment(); break;
            default: throw new IllegalArgumentException("Unknown page: " + position);
        }
        fragments.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() { return PAGE_COUNT; }

    public Fragment getFragmentAt(int position) {
        return fragments.get(position);
    }

    public void removeFragment(int position) {
        fragments.remove(position);
    }
}
