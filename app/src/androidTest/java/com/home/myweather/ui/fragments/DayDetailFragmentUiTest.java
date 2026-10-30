package com.home.myweather.ui.fragments;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.home.myweather.R;
import com.home.myweather.data.model.DailyData;
import com.home.myweather.utils.TestWeatherFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
public class DayDetailFragmentUiTest {

    private FragmentScenario<DayDetailFragment> scenario;

    @Before
    public void setUp() {
        DailyData day = createTestDailyData();
        scenario = FragmentScenario.launchInContainer(DayDetailFragment.class, 
            DayDetailFragment.newInstance(day).getArguments());
    }

    @Test
    public void showsBackButton() {
        onView(withId(R.id.btn_back))
            .check(matches(isDisplayed()));
    }

    @Test
    public void showsDayTitle() {
        onView(withId(R.id.tv_day_title))
            .check(matches(isDisplayed()));
    }

    @Test
    public void showsTemperatureSection() {
        onView(withText("Температура"))
            .check(matches(isDisplayed()));
        
        onView(withText("Ощущается как"))
            .check(matches(isDisplayed()));
        
        onView(withId(R.id.tv_temp_morning)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_temp_day)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_temp_evening)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_temp_night)).check(matches(isDisplayed()));
        
        onView(withId(R.id.tv_feels_morning)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_feels_day)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_feels_evening)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_feels_night)).check(matches(isDisplayed()));
    }

    @Test
    public void showsWindSection() {
        onView(withText("Ветер"))
            .check(matches(isDisplayed()));
        
        onView(withId(R.id.tv_wind_morning)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_wind_day)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_wind_evening)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_wind_night)).check(matches(isDisplayed()));
        
        onView(withId(R.id.tv_dir_morning)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_dir_day)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_dir_evening)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_dir_night)).check(matches(isDisplayed()));
    }

    @Test
    public void showsHumiditySection() {
        onView(withText("Влажность"))
            .check(matches(isDisplayed()));
        
        onView(withId(R.id.tv_hum_morning)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_hum_day)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_hum_evening)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_hum_night)).check(matches(isDisplayed()));
    }

    @Test
    public void showsPressureSection() {
        onView(withText("Давление"))
            .check(matches(isDisplayed()));
        
        onView(withId(R.id.tv_press_morning)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_press_day)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_press_evening)).check(matches(isDisplayed()));
        onView(withId(R.id.tv_press_night)).check(matches(isDisplayed()));
        
        onView(withText("мм рт. ст."))
            .check(matches(isDisplayed()));
    }

    @Test
    public void showsTimeLabels() {
        onView(withText("Утро")).check(matches(isDisplayed()));
        onView(withText("День")).check(matches(isDisplayed()));
        onView(withText("Вечер")).check(matches(isDisplayed()));
        onView(withText("Ночь")).check(matches(isDisplayed()));
    }

    @Test
    public void showsWeatherIcons() {
        onView(withId(R.id.iv_weather_morning)).check(matches(isDisplayed()));
        onView(withId(R.id.iv_weather_day)).check(matches(isDisplayed()));
        onView(withId(R.id.iv_weather_evening)).check(matches(isDisplayed()));
        onView(withId(R.id.iv_weather_night)).check(matches(isDisplayed()));
    }

    private DailyData createTestDailyData() {
        DailyData day = new DailyData();
        day.dateMillis = System.currentTimeMillis();
        day.tempMin = 5.0;
        day.tempMax = 15.0;
        day.pop = 0.3;
        day.description = "Light rain";
        day.items = new ArrayList<>();
        
        for (int i = 0; i < 8; i++) {
            ForecastItem item = TestWeatherFactory.createForecastItem(
                System.currentTimeMillis() / 1000L + i * 3600,
                10.0 + i, 12.0 + i, 60 + i, 1013 + i, 3.0 + i * 0.5, 180 + i * 10, 0.1 * i
            );
            day.items.add(item);
        }
        
        return day;
    }
}