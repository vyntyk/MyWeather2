package com.home.myweather.ui.fragments;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.home.myweather.R;
import com.home.myweather.helpers.FavoriteCitiesManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

@RunWith(AndroidJUnit4.class)
public class CitiesFragmentUiTest {

    private static final String PREFS_NAME = "favorite_cities";

    private Context context;
    private SharedPreferences prefs;
    private FavoriteCitiesManager manager;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        manager = new FavoriteCitiesManager(context);
    }

    @After
    public void tearDown() {
        prefs.edit().clear().commit();
    }

    @Test
    public void emptyState_isShownWhenNoCities() {
        FragmentScenario.launchInContainer(CitiesFragment.class, null, R.style.Theme_MyWeather);

        onView(withId(R.id.tv_empty_cities)).check(matches(isDisplayed()));
        onView(withId(R.id.rv_favorite_cities)).check(matches(not(isDisplayed())));
    }

    @Test
    public void existingCities_areListedOnLaunch() {
        manager.addCity("Berlin");
        manager.addCity("Paris");

        FragmentScenario.launchInContainer(CitiesFragment.class, null, R.style.Theme_MyWeather);

        onView(withText("Berlin")).check(matches(isDisplayed()));
        onView(withText("Paris")).check(matches(isDisplayed()));
        onView(withId(R.id.tv_empty_cities)).check(matches(not(isDisplayed())));
    }

    @Test
    public void addCity_showsItInList_andPersistsIt() {
        FragmentScenario.launchInContainer(CitiesFragment.class, null, R.style.Theme_MyWeather);

        onView(withId(R.id.et_city_input))
                .perform(typeText("Kazan"), closeSoftKeyboard());
        onView(withId(R.id.btn_add_city)).perform(click());

        onView(withText("Kazan")).check(matches(isDisplayed()));
        onView(withId(R.id.tv_empty_cities)).check(matches(not(isDisplayed())));
        assertEquals(Collections.singletonList("Kazan"), manager.getCities());
    }

    @Test
    public void addCity_withBlankInput_addsNothing() {
        FragmentScenario.launchInContainer(CitiesFragment.class, null, R.style.Theme_MyWeather);

        onView(withId(R.id.et_city_input))
                .perform(typeText("   "), closeSoftKeyboard());
        onView(withId(R.id.btn_add_city)).perform(click());

        onView(withId(R.id.tv_empty_cities)).check(matches(isDisplayed()));
        assertTrue(manager.getCities().isEmpty());
    }

    @Test
    public void removeCity_removesItFromListAndStorage() {
        manager.addCity("Berlin");
        FragmentScenario.launchInContainer(CitiesFragment.class, null, R.style.Theme_MyWeather);

        onView(allOf(withId(R.id.btn_remove_city), hasSibling(withText("Berlin"))))
                .perform(click());

        onView(withId(R.id.tv_empty_cities)).check(matches(isDisplayed()));
        assertTrue(manager.getCities().isEmpty());
    }
}
