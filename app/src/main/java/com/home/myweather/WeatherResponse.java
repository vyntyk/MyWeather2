package com.home.myweather;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Модель ответа Current Weather API 2.5 /data/2.5/weather.
 * Документация: https://openweathermap.org/current
 */
public class WeatherResponse implements Serializable {

    @SerializedName("coord")
    public Coord coord;

    @SerializedName("weather")
    public WeatherCondition[] weather;

    @SerializedName("main")
    public Main main;

    @SerializedName("wind")
    public Wind wind;

    @SerializedName("clouds")
    public Clouds clouds;

    @SerializedName("sys")
    public Sys sys;

    @SerializedName("dt")
    public long dt;

    @SerializedName("name")
    public String name;

    @SerializedName("visibility")
    public Integer visibility;

    @SerializedName("timezone")
    public int timezone;

    // ──────────────────────────────────────────────
    public static class Coord implements Serializable {

        @SerializedName("lat")
        public double lat;

        @SerializedName("lon")
        public double lon;
    }

    // ──────────────────────────────────────────────
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

    // ──────────────────────────────────────────────
    public static class Wind implements Serializable {

        @SerializedName("speed")
        public double speed;

        @SerializedName("deg")
        public int deg;
    }

    // ──────────────────────────────────────────────
    public static class Clouds implements Serializable {

        @SerializedName("all")
        public int all;
    }

    // ──────────────────────────────────────────────
    public static class Sys implements Serializable {

        @SerializedName("sunrise")
        public long sunrise;

        @SerializedName("sunset")
        public long sunset;

        @SerializedName("country")
        public String country;
    }

    // ──────────────────────────────────────────────
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
}
