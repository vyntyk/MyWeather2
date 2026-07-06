package com.home.myweather.ui.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
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

    private final Map<Integer, Fragment> fragmentCache = new HashMap<>();
    private final FragmentManager fragmentManager;

    public MainPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
        this.fragmentManager = activity.getSupportFragmentManager();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position) {
            case PAGE_NOW:
                fragment = new NowFragment();
                break;
            case PAGE_FORECAST:
                fragment = new ForecastFragment();
                break;
            case PAGE_MAP:
                fragment = new MapFragment();
                break;
            case PAGE_CITIES:
                fragment = new CitiesFragment();
                break;
            case PAGE_SETTINGS:
                fragment = new SettingsFragment();
                break;
            default:
                throw new IllegalArgumentException("Unknown page: " + position);
        }
        
        // Cache fragment reference
        fragmentCache.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return PAGE_COUNT;
    }

    /**
     * Get fragment at position with null-safety check
     */
    @Nullable
    public Fragment getFragmentAt(int position) {
        Fragment cached = fragmentCache.get(position);
        if (cached != null && cached.isAdded()) {
            return cached;
        }
        
        // Try to find fragment by tag if it exists in FragmentManager
        String tag = "f" + position;
        Fragment fm = fragmentManager.findFragmentByTag(tag);
        if (fm != null) {
            fragmentCache.put(position, fm);
            return fm;
        }
        
        return cached;
    }

    /**
     * Clear fragment cache and remove from FragmentManager
     */
    public void removeFragment(int position) {
        fragmentCache.remove(position);
    }

    /**
     * Clear all cached fragments
     */
    public void clearCache() {
        fragmentCache.clear();
    }
}
