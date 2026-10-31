package com.home.myweather.ui.fragments;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.home.myweather.R;
import com.home.myweather.testing.FragmentTestActivity;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ForecastFragmentUiTest {

    private ActivityScenario<FragmentTestActivity> scenario;

    @Before
    public void setUp() {
        scenario = ActivityScenario.launch(FragmentTestActivity.class);
        scenario.onActivity(activity -> activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(FragmentTestActivity.CONTAINER_ID, new ForecastFragment())
                .commitNow());
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    @Test
    public void showsPlaceholderWhenNoData() {
        onView(withId(R.id.tv_placeholder))
            .check(matches(isDisplayed()));
        
        onView(withId(R.id.tv_placeholder))
            .check(matches(withText(containsString("Сначала найдите погоду"))));
    }

    @Test
    public void showsForecastTitle() {
        onView(withId(R.id.tv_forecast_title))
            .check(matches(isDisplayed()));
    }

    @Test
    public void recyclerViewIsHiddenWhenNoForecastData() {
        onView(withId(R.id.rv_daily))
            .check(matches(not(isDisplayed())));
    }
}