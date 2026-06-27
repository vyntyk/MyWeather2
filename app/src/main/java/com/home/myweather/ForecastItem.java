package com.home.myweather;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Один блок прогноза (каждые 3 часа) из /data/2.5/forecast.
 */
public class ForecastItem implements Serializable {

    @SerializedName("dt")
    public long timestamp; // Unix timestamp

    @SerializedName("main")
    public Main main;

    @SerializedName("weather")
    public WeatherCondition[] weather;

    @SerializedName("wind")
    public Wind wind;

    @SerializedName("clouds")
    public Clouds clouds;

    @SerializedName("pop")
    public double pop; // Probability of precipitation (0-1)

    @SerializedName("visibility")
    public int visibility;

    @SerializedName("dt_txt")
    public String dtText; // e.g. "2024-06-25 12:00:00"

    public static class Main implements Serializable {
        @SerializedName("temp")
        public double temp;

        @SerializedName("feels_like")
        public double feelsLike;

        @SerializedName("temp_min")
        public double tempMin;

        @SerializedName("temp_max")
        public double tempMax;

        @SerializedName("pressure")
        public int pressure;

        @SerializedName("humidity")
        public int humidity;
    }

    public static class WeatherCondition implements Serializable {
        @SerializedName("id")
        public int id;

        @SerializedName("main")
        public String main;

        @SerializedName("description")
        public String description;

        @SerializedName("icon")
        public String icon;
    }

    public static class Wind implements Serializable {
        @SerializedName("speed")
        public double speed;

        @SerializedName("deg")
        public int deg;
    }

    public static class Clouds implements Serializable {
        @SerializedName("all")
        public int all;
    }
}
