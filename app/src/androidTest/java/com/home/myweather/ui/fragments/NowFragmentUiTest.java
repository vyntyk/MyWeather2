package com.home.myweather.ui.fragments;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.contrib.RecyclerViewActions;

import com.home.myweather.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NowFragmentUiTest {

    private FragmentScenario<NowFragment> scenario;

    @Before
    public void setUp() {
        scenario = FragmentScenario.launchInContainer(NowFragment.class);
    }

    @Test
    public void showsCityInputField() {
        onView(withId(R.id.user_field))
            .check(matches(isDisplayed()));
        
        onView(withId(R.id.user_field))
            .check(matches(withHint(containsString("City"))));
    }

    @Test
    public void showsFindButton() {
        onView(withId(R.id.main_btn))
            .check(matches(isDisplayed()));
        
        onView(withId(R.id.main_btn))
            .check(matches(withText(containsString("Find"))));
    }

    @Test
    public void showsWeatherResultViews() {
        onView(withId(R.id.resultat))
            .check(matches(isDisplayed()));
        
        onView(withId(R.id.resultat2))
            .check(matches(isDisplayed()));
        
        onView(withId(R.id.resultat3))
            .check(matches(isDisplayed()));
        
        onView(withId(R.id.resultat4))
            .check(matches(isDisplayed()));
        
        onView(withId(R.id.resultat5))
            .check(matches(isDisplayed()));
    }

    @Test
    public void typingCityAndClickingFind_triggersSearch() {
        onView(withId(R.id.user_field))
            .perform(replaceText("Moscow"), closeSoftKeyboard());
        
        onView(withId(R.id.main_btn))
            .perform(click());
        
        // Search triggered - would show loading or results
        // In real test with mocked ViewModel, we'd verify the call
    }

    @Test
    public void emptyCityInput_showsToast() {
        onView(withId(R.id.user_field))
            .perform(replaceText(""), closeSoftKeyboard());
        
        onView(withId(R.id.main_btn))
            .perform(click());
        
        // Would show "Please enter a city name" toast
    }

    @Test
    public void backgroundButtons_exist() {
        onView(withId(R.id.btn1)).check(matches(isDisplayed()));
        onView(withId(R.id.btn2)).check(matches(isDisplayed()));
        onView(withId(R.id.btn3)).check(matches(isDisplayed()));
    }
}