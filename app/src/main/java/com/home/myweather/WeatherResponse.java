package com.home.myweather;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Модель ответа Current Weather API 2.5 /data/2.5/weather.
 * Документация: https://openweathermap.org/current
 *
 * Поля намеренно приватные — Gson заполняет их через рефлексию,
 * доступ снаружи осуществляется исключительно через геттеры.
 */
public class WeatherResponse implements Serializable {

    @SerializedName("coord")
    private Coord coord;

    @SerializedName("weather")
    private WeatherCondition[] weather;

    @SerializedName("main")
    private Main main;

    @SerializedName("wind")
    private Wind wind;

    @SerializedName("clouds")
    private Clouds clouds;

    @SerializedName("sys")
    private Sys sys;

    @SerializedName("dt")
    private long dt;

    @SerializedName("name")
    private String name;

    @SerializedName("visibility")
    private Integer visibility;

    @SerializedName("timezone")
    private int timezone;

    public Coord getCoord()               { return coord; }
    public WeatherCondition[] getWeather(){ return weather; }
    public Main getMain()                 { return main; }
    public Wind getWind()                 { return wind; }
    public Clouds getClouds()             { return clouds; }
    public Sys getSys()                   { return sys; }
    public long getDt()                   { return dt; }
    public String getName()               { return name; }
    public Integer getVisibility()        { return visibility; }
    public int getTimezone()              { return timezone; }

    // ──────────────────────────────────────────────
    public static class Coord implements Serializable {

        @SerializedName("lat")
        private double lat;

        @SerializedName("lon")
        private double lon;

        public double getLat() { return lat; }
        public double getLon() { return lon; }
    }

    // ──────────────────────────────────────────────
    public static class Main implements Serializable {

        @SerializedName("temp")
        private double temp;

        @SerializedName("feels_like")
        private double feelsLike;

        @SerializedName("temp_min")
        private double tempMin;

        @SerializedName("temp_max")
        private double tempMax;

        @SerializedName("pressure")
        private int pressure;

        @SerializedName("humidity")
        private int humidity;

        public double getTemp()      { return temp; }
        public double getFeelsLike() { return feelsLike; }
        public double getTempMin()   { return tempMin; }
        public double getTempMax()   { return tempMax; }
        public int getPressure()     { return pressure; }
        public int getHumidity()     { return humidity; }
    }

    // ──────────────────────────────────────────────
    public static class Wind implements Serializable {

        @SerializedName("speed")
        private double speed;

        @SerializedName("deg")
        private int deg;

        public double getSpeed() { return speed; }
        public int getDeg()      { return deg; }
    }

    // ──────────────────────────────────────────────
    public static class Clouds implements Serializable {

        @SerializedName("all")
        private int all;

        public int getAll() { return all; }
    }

    // ──────────────────────────────────────────────
    public static class Sys implements Serializable {

        @SerializedName("sunrise")
        private long sunrise;

        @SerializedName("sunset")
        private long sunset;

        @SerializedName("country")
        private String country;

        public long getSunrise()   { return sunrise; }
        public long getSunset()    { return sunset; }
        public String getCountry() { return country; }
    }

    // ──────────────────────────────────────────────
    public static class WeatherCondition implements Serializable {

        @SerializedName("id")
        private int id;

        @SerializedName("main")
        private String main;

        @SerializedName("description")
        private String description;

        @SerializedName("icon")
        private String icon;

        public int getId()             { return id; }
        public String getMain()        { return main; }
        public String getDescription() { return description; }
        public String getIcon()        { return icon; }
    }
}
