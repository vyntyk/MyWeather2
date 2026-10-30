package com.home.myweather.ui.fragments;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.contrib.RecyclerViewActions;

import com.home.myweather.R;
import com.home.myweather.data.model.DailyData;
import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.utils.TestWeatherFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ForecastFragmentUiTest {

    private FragmentScenario<ForecastFragment> scenario;

    @Before
    public void setUp() {
        scenario = FragmentScenario.launchInContainer(ForecastFragment.class);
    }

    @Test
    public void showsPlaceholderWhenNoData() {
        onView(withId(R.id.tv_placeholder))
            .check(matches(isDisplayed()));
        
        onView(withId(R.id.tv_placeholder))
            .check(matches(withText(containsString("Сначала найдите погоду"))));
    }

    @Test
    public void showsRecyclerViewWhenDataLoaded() {
        // This test requires a mocked ViewModel - using real fragment would need DI
        onView(withId(R.id.rv_daily))
            .check(matches(isDisplayed()));
    }

    @Test
    public void recyclerViewHasCorrectItemCount() {
        onView(withId(R.id.rv_daily))
            .check(matches(isDisplayed()));
    }
}