package com.home.myweather.ui.fragments;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static org.hamcrest.Matchers.allOf;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.home.myweather.R;
import com.home.myweather.testing.FragmentTestActivity;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NowFragmentUiTest {

    private ActivityScenario<FragmentTestActivity> scenario;

    @Before
    public void setUp() {
        scenario = ActivityScenario.launch(FragmentTestActivity.class);
        scenario.onActivity(activity -> activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(FragmentTestActivity.CONTAINER_ID, new NowFragment())
                .commitNow());
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    @Test
    public void showsCityInputField() {
        onView(withId(R.id.user_field))
                .check(matches(isDisplayed()));

        onView(withId(R.id.user_field))
                .check(matches(withHint("Город")));
    }

    @Test
    public void showsFindButton() {
        onView(withId(R.id.main_btn))
                .check(matches(isEnabled()));
    }

    @Test
    public void showsWeatherResultViews() {
        onView(isRoot()).check(matches(hasDescendant(allOf(
                withId(R.id.tv_temp), isDisplayed()))));
        onView(isRoot()).check(matches(hasDescendant(allOf(
                withId(R.id.tv_desc), isDisplayed()))));
        onView(isRoot()).check(matches(hasDescendant(allOf(
                withId(R.id.tv_wind_value), isDisplayed()))));
        onView(isRoot()).check(matches(hasDescendant(allOf(
                withId(R.id.tv_pressure_value), isDisplayed()))));
        onView(isRoot()).check(matches(hasDescendant(allOf(
                withId(R.id.tv_humidity_value), isDisplayed()))));
    }

    @Test
    public void typingCityAndClickingFind_doesNotCrash() {
        onView(withId(R.id.user_field))
                .perform(replaceText("Moscow"), closeSoftKeyboard());

        onView(withId(R.id.main_btn))
                .perform(click());

        onView(withId(R.id.user_field)).check(matches(isDisplayed()));
    }

    @Test
    public void emptyCityInput_doesNotCrash() {
        onView(withId(R.id.user_field))
                .perform(replaceText(""), closeSoftKeyboard());

        onView(withId(R.id.main_btn))
                .perform(click());

        onView(withId(R.id.main_btn)).check(matches(isDisplayed()));
    }

    @Test
    public void geoButton_isDisplayed() {
        onView(withId(R.id.geo_btn)).check(matches(isDisplayed()));
    }
}
