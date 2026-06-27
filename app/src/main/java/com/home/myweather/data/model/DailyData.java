package com.home.myweather.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.home.myweather.data.model.ForecastItem;

/**
 * Агрегированные данные по одному дню для прогноза.
 * Отдельный класс для безопасной передачи между фрагментами через Bundle.
 */
public class DailyData implements Serializable {

    public long dateMillis;
    public double tempMin;
    public double tempMax;
    public String description;
    public double pop; // max probability of precipitation for the day
    public List<ForecastItem> items; // all 3-hour blocks for this day

    public DailyData() {
        this.items = new ArrayList<>();
    }
}
