package com.home.myweather.ui.fragments;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.home.myweather.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SettingsFragmentUiTest {

    private static final String PREFS_NAME = "myweather_prefs";
    private static final String KEY_TEMP_UNIT = "temp_unit";
    private static final String KEY_DARK_THEME = "dark_theme";

    private SharedPreferences prefs;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
    }

    @After
    public void tearDown() {
        prefs.edit().clear().commit();
    }

    @Test
    public void defaultTempUnit_isCelsius() {
        FragmentScenario.launchInContainer(SettingsFragment.class, null, R.style.Theme_MyWeather);

        onView(withId(R.id.rb_celsius)).check(matches(isChecked()));
        onView(withId(R.id.rb_fahrenheit)).check(matches(not(isChecked())));
    }

    @Test
    public void storedFahrenheit_isRestoredOnLaunch() {
        prefs.edit().putString(KEY_TEMP_UNIT, "F").commit();

        FragmentScenario.launchInContainer(SettingsFragment.class, null, R.style.Theme_MyWeather);

        onView(withId(R.id.rb_fahrenheit)).check(matches(isChecked()));
        onView(withId(R.id.rb_celsius)).check(matches(not(isChecked())));
    }

    @Test
    public void selectingFahrenheit_persistsPreference() {
        FragmentScenario.launchInContainer(SettingsFragment.class, null, R.style.Theme_MyWeather);

        onView(withId(R.id.rb_fahrenheit)).perform(click());

        assertEquals("F", prefs.getString(KEY_TEMP_UNIT, "C"));
        onView(withId(R.id.rb_fahrenheit)).check(matches(isChecked()));
        onView(withId(R.id.rb_celsius)).check(matches(not(isChecked())));
    }

    @Test
    public void selectingCelsiusAfterFahrenheit_persistsPreference() {
        prefs.edit().putString(KEY_TEMP_UNIT, "F").commit();
        FragmentScenario.launchInContainer(SettingsFragment.class, null, R.style.Theme_MyWeather);

        onView(withId(R.id.rb_celsius)).perform(click());

        assertEquals("C", prefs.getString(KEY_TEMP_UNIT, "F"));
        onView(withId(R.id.rb_celsius)).check(matches(isChecked()));
        onView(withId(R.id.rb_fahrenheit)).check(matches(not(isChecked())));
    }

    @Test
    public void darkThemeSwitch_reflectsStoredValue() {
        prefs.edit().putBoolean(KEY_DARK_THEME, true).commit();

        FragmentScenario.launchInContainer(SettingsFragment.class, null, R.style.Theme_MyWeather);

        onView(withId(R.id.sw_dark_theme)).check(matches(isChecked()));
    }

    @Test
    public void darkThemeSwitch_defaultsToOff() {
        FragmentScenario.launchInContainer(SettingsFragment.class, null, R.style.Theme_MyWeather);

        onView(withId(R.id.sw_dark_theme)).check(matches(not(isChecked())));
    }

    @Test
    public void attribution_isDisplayedWithMetNorwayCredit() {
        FragmentScenario.launchInContainer(SettingsFragment.class, null, R.style.Theme_MyWeather);

        onView(withId(R.id.weather_attribution)).check(matches(isDisplayed()));
        onView(withId(R.id.weather_attribution))
                .check(matches(withText(containsString("MET Norway"))));
    }

    @Test
    public void unitSelection_doesNotLeaveBothRadioButtonsChecked() {
        FragmentScenario.launchInContainer(SettingsFragment.class, null, R.style.Theme_MyWeather);

        onView(withId(R.id.rb_fahrenheit)).perform(click());
        onView(withId(R.id.rb_celsius)).perform(click());
        onView(withId(R.id.rb_fahrenheit)).perform(click());

        assertTrue("Единица должна быть одна", "F".equals(prefs.getString(KEY_TEMP_UNIT, "C")));
        onView(withId(R.id.rb_fahrenheit)).check(matches(isChecked()));
        onView(withId(R.id.rb_celsius)).check(matches(not(isChecked())));
    }
}
