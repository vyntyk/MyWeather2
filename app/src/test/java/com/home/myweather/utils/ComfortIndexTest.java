package com.home.myweather.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class ComfortIndexTest {

    // Tests for getComfortMessage

    @Test
    public void getComfortMessage_highPop_returnsUmbrella() {
        String msg = ComfortIndex.getComfortMessage(20, 5, 50, 1013, 0.8);
        assertTrue("Should contain umbrella emoji for pop >= 0.7", msg.contains("☂"));
    }

    @Test
    public void getComfortMessage_mediumPop_returnsPossibleRain() {
        String msg = ComfortIndex.getComfortMessage(20, 5, 50, 1013, 0.5);
        assertTrue("Should contain possible rain emoji for pop >= 0.3", msg.contains("🌂"));
    }

    @Test
    public void getComfortMessage_lowPop_noRainEmoji() {
        String msg = ComfortIndex.getComfortMessage(20, 5, 50, 1013, 0.1);
        assertFalse("Should not contain rain emoji for pop < 0.3", msg.contains("🌂") || msg.contains("☂"));
    }

    @Test
    public void getComfortMessage_hotTemp_returnsHotWarning() {
        String msg = ComfortIndex.getComfortMessage(35, 5, 50, 1013, 0);
        assertTrue("Should contain hot warning for temp >= 30", msg.contains("🕶") && msg.contains("воды"));
    }

    @Test
    public void getComfortMessage_warmTemp_returnsUvWarning() {
        String msg = ComfortIndex.getComfortMessage(27, 5, 50, 1013, 0);
        assertTrue("Should contain UV warning for temp >= 25", msg.contains("🕶") && msg.contains("солнцезащитные"));
    }

    @Test
    public void getComfortMessage_pleasantTemp_returnsWalkMessage() {
        String msg = ComfortIndex.getComfortMessage(20, 5, 50, 1013, 0);
        assertTrue("Should contain walk message for temp 15-25", msg.contains("😊") && msg.contains("прогулки"));
    }

    @Test
    public void getComfortMessage_coolTempWithWind_returnsJacketWithWind() {
        String msg = ComfortIndex.getComfortMessage(10, 8, 50, 1013, 0);
        assertTrue("Should mention wind for cool temp with wind > 5", msg.contains("куртку") && msg.contains("ветер"));
    }

    @Test
    public void getComfortMessage_coolTempNoWind_returnsJacket() {
        String msg = ComfortIndex.getComfortMessage(10, 3, 50, 1013, 0);
        assertTrue("Should mention jacket for cool temp", msg.contains("куртку") && !msg.contains("ветер прохладный"));
    }

    @Test
    public void getComfortMessage_coldTemp_returnsHatScarf() {
        String msg = ComfortIndex.getComfortMessage(0, 5, 50, 1013, 0);
        assertTrue("Should mention hat and scarf for cold temp", msg.contains("🧣") && msg.contains("шапка") && msg.contains("шарф"));
    }

    @Test
    public void getComfortMessage_freezingTemp_returnsDressWarmer() {
        String msg = ComfortIndex.getComfortMessage(-10, 5, 50, 1013, 0);
        assertTrue("Should mention frost for temp < -5", msg.contains("❄️") && msg.contains("Мороз"));
    }

    @Test
    public void getComfortMessage_strongWind_returnsWindWarning() {
        String msg = ComfortIndex.getComfortMessage(20, 20, 50, 1013, 0);
        assertTrue("Should contain wind warning for wind > 15", msg.contains("💨") && msg.contains("Сильный ветер"));
    }

    @Test
    public void getComfortMessage_windGoodForRunning_returnsRunning() {
        String msg = ComfortIndex.getComfortMessage(20, 12, 50, 1013, 0);
        assertTrue("Should contain running for wind > 10 and temp > 15", msg.contains("🏃") && msg.contains("пробежки"));
    }

    @Test
    public void getComfortMessage_windGoodForCycling_returnsCycling() {
        String msg = ComfortIndex.getComfortMessage(20, 8, 50, 1013, 0);
        assertTrue("Should contain cycling for wind 5-10 and temp > 15", msg.contains("🚴") && msg.contains("велосипеда"));
    }

    @Test
    public void getComfortMessage_lowPressure_returnsPressureWarning() {
        String msg = ComfortIndex.getComfortMessage(20, 5, 50, 990, 0);
        assertTrue("Should contain low pressure warning for pressure < 1000", msg.contains("📉") && msg.contains("Низкое давление"));
    }

    @Test
    public void getComfortMessage_normalConditions_returnsNormal() {
        String msg = ComfortIndex.getComfortMessage(20, 3, 50, 1013, 0);
        assertEquals("Should return walk message for normal conditions", "😊 Отличный день для прогулки.", msg);
    }

    // Tests for getComfortEmoji

    @Test
    public void getComfortEmoji_thunderstorm_returnsThunder() {
        String emoji = ComfortIndex.getComfortEmoji(20, 200); // 200-299 thunderstorm
        assertEquals("⛈", emoji);
    }

    @Test
    public void getComfortEmoji_drizzle_returnsRain() {
        String emoji = ComfortIndex.getComfortEmoji(20, 300); // 300-399 drizzle
        assertEquals("🌧", emoji);
    }

    @Test
    public void getComfortEmoji_rain_returnsRain() {
        String emoji = ComfortIndex.getComfortEmoji(20, 500); // 500-599 rain
        assertEquals("🌧", emoji);
    }

    @Test
    public void getComfortEmoji_snow_returnsSnow() {
        String emoji = ComfortIndex.getComfortEmoji(20, 600); // 600-699 snow
        assertEquals("❄️", emoji);
    }

    @Test
    public void getComfortEmoji_atmosphere_returnsFog() {
        String emoji = ComfortIndex.getComfortEmoji(20, 700); // 700-799 atmosphere
        assertEquals("🌫", emoji);
    }

    @Test
    public void getComfortEmoji_clearDay_returnsSun() {
        // Day time check is based on current hour, so we just test it returns one of the two
        String emoji = ComfortIndex.getComfortEmoji(20, 800);
        assertTrue("Should return sun or moon for clear sky", emoji.equals("☀️") || emoji.equals("🌙"));
    }

    @Test
    public void getComfortEmoji_clouds_returnsCloud() {
        String emoji = ComfortIndex.getComfortEmoji(20, 801); // > 800 clouds
        assertEquals("☁️", emoji);
    }

    @Test
    public void getComfortEmoji_unknown_returnsDefault() {
        String emoji = ComfortIndex.getComfortEmoji(20, 999); // > 800 clouds
        assertEquals("☁️", emoji);
    }

    // Tests for getBackgroundResource - can't test R.drawable without Robolectric
    // These would need instrumented tests or Robolectric
}