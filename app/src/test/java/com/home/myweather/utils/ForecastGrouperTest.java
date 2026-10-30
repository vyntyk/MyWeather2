package com.home.myweather.utils;

import static com.google.common.truth.Truth.assertThat;

import com.home.myweather.data.model.DailyData;
import com.home.myweather.data.model.ForecastItem;
import com.home.myweather.utils.ForecastGrouper;
import com.home.myweather.utils.TestWeatherFactory;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ForecastGrouperTest {

    @Test
    public void groupByDay_emptyList_returnsEmpty() {
        List<DailyData> result = ForecastGrouper.groupByDay(new ArrayList<>());
        assertThat(result).isEmpty();
    }

    @Test
    public void groupByDay_singleDay_aggregatesCorrectly() {
        List<ForecastItem> items = new ArrayList<>();
        // 3 items for same day (2023-11-15)
        long baseTime = 1700000000L; // 2023-11-15
        items.add(TestWeatherFactory.createForecastItem(baseTime, 10.0, 20.0, 60, 1013, 5.0, 180, 0.1));
        items.add(TestWeatherFactory.createForecastItem(baseTime + 3600, 12.0, 22.0, 65, 1012, 4.0, 190, 0.2));
        items.add(TestWeatherFactory.createForecastItem(baseTime + 7200, 8.0, 18.0, 55, 1014, 3.0, 200, 0.0));

        List<DailyData> result = ForecastGrouper.groupByDay(items);

        assertThat(result).hasSize(1);
        DailyData day = result.get(0);
        assertThat(day.tempMin).isEqualTo(8.0);
        assertThat(day.tempMax).isEqualTo(22.0);
        assertThat(day.pop).isEqualTo(0.2);
        assertThat(day.items).hasSize(3);
    }

    @Test
    public void groupByDay_multipleDays_splitsCorrectly() {
        List<ForecastItem> items = new ArrayList<>();
        // Day 1 (2023-11-15)
        items.add(TestWeatherFactory.createForecastItem(1700000000L, 10.0, 20.0, 60, 1013, 5.0, 180, 0.1));
        // Day 2 (2023-11-16)
        items.add(TestWeatherFactory.createForecastItem(1700086400L, 5.0, 15.0, 70, 1015, 6.0, 200, 0.3));

        List<DailyData> result = ForecastGrouper.groupByDay(items);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).tempMin).isEqualTo(10.0);
        assertThat(result.get(1).tempMin).isEqualTo(5.0);
    }

    @Test
    public void groupByDay_descriptionFromFirstItemWithWeather() {
        List<ForecastItem> items = new ArrayList<>();
        items.add(TestWeatherFactory.createForecastItem(1700000000L, 10.0, 20.0, 60, 1013, 5.0, 180, 0.1));
        // Second item has no weather
        ForecastItem noWeather = TestWeatherFactory.createForecastItem(1700010000L, 12.0, 22.0, 65, 1012, 4.0, 190, 0.2);
        noWeather.weather = null;
        items.add(noWeather);

        List<DailyData> result = ForecastGrouper.groupByDay(items);

        assertThat(result.get(0).description).isEqualTo("Clear sky");
    }

    @Test
    public void groupByDay_itemsNotSorted_sortsByDate() {
        List<ForecastItem> items = new ArrayList<>();
        // Day 2 first
        items.add(TestWeatherFactory.createForecastItem(1700086400L, 5.0, 15.0, 70, 1015, 6.0, 200, 0.3));
        // Day 1 second
        items.add(TestWeatherFactory.createForecastItem(1700000000L, 10.0, 20.0, 60, 1013, 5.0, 180, 0.1));

        List<DailyData> result = ForecastGrouper.groupByDay(items);

        assertThat(result).hasSize(2);
        // First should be earlier date
        assertThat(result.get(0).dateMillis).isLessThan(result.get(1).dateMillis);
    }

    @Test
    public void groupByDay_popUsesMax() {
        List<ForecastItem> items = new ArrayList<>();
        items.add(TestWeatherFactory.createForecastItem(1700000000L, 10.0, 20.0, 60, 1013, 5.0, 180, 0.1));
        items.add(TestWeatherFactory.createForecastItem(1700010000L, 12.0, 22.0, 65, 1012, 4.0, 190, 0.5));
        items.add(TestWeatherFactory.createForecastItem(1700020000L, 8.0, 18.0, 55, 1014, 3.0, 200, 0.2));

        List<DailyData> result = ForecastGrouper.groupByDay(items);

        assertThat(result.get(0).pop).isEqualTo(0.5); // max
    }

    @Test
    public void groupByDay_manyItemsPerDay_performance() {
        List<ForecastItem> items = new ArrayList<>();
        // 7 days * 8 items per day = 56 items
        long baseTime = 1700000000L;
        for (int day = 0; day < 7; day++) {
            for (int hour = 0; hour < 8; hour++) {
                items.add(TestWeatherFactory.createForecastItem(
                    baseTime + day * 86400L + hour * 3600L,
                    10.0 + day, 20.0 + day, 60, 1013, 5.0, 180, 0.1
                ));
            }
        }

        List<DailyData> result = ForecastGrouper.groupByDay(items);

        assertThat(result).hasSize(7);
    }
}