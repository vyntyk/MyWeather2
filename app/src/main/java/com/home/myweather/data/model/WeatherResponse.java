package com.home.myweather.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Модель текущей погоды.
 *
 * Поля сделаны public, чтобы WeatherRepository мог заполнять их
 * данными Open-Meteo без рефлексии. Геттеры сохранены для совместимости
 * с WeatherFormatter, UiController и другими потребителями.
 */
public class WeatherResponse implements Serializable {

    @SerializedName("coord")       public Coord              coord;
    @SerializedName("weather")     public WeatherCondition[] weather;
    @SerializedName("main")        public Main               main;
    @SerializedName("wind")        public Wind               wind;
    @SerializedName("clouds")      public Clouds             clouds;
    @SerializedName("sys")         public Sys                sys;
    @SerializedName("dt")          public long               dt;
    @SerializedName("name")        public String             name;
    @SerializedName("visibility")  public Integer            visibility;
    @SerializedName("timezone")    public int                timezone;

    public Coord              getCoord()      { return coord; }
    public WeatherCondition[] getWeather()    { return weather; }
    public Main               getMain()       { return main; }
    public Wind               getWind()       { return wind; }
    public Clouds             getClouds()     { return clouds; }
    public Sys                getSys()        { return sys; }
    public long               getDt()         { return dt; }
    public String             getName()       { return name; }
    public Integer            getVisibility() { return visibility; }
    public int                getTimezone()   { return timezone; }

    // ── Coord ─────────────────────────────────────────────────────────────
    public static class Coord implements Serializable {
        @SerializedName("lat") public double lat;
        @SerializedName("lon") public double lon;

        public double getLat() { return lat; }
        public double getLon() { return lon; }
    }

    // ── Main ──────────────────────────────────────────────────────────────
    public static class Main implements Serializable {
        @SerializedName("temp")       public double temp;
        @SerializedName("feels_like") public double feelsLike;
        @SerializedName("temp_min")   public double tempMin;
        @SerializedName("temp_max")   public double tempMax;
        @SerializedName("pressure")   public int    pressure;
        @SerializedName("humidity")   public int    humidity;

        public double getTemp()      { return temp; }
        public double getFeelsLike() { return feelsLike; }
        public double getTempMin()   { return tempMin; }
        public double getTempMax()   { return tempMax; }
        public int    getPressure()  { return pressure; }
        public int    getHumidity()  { return humidity; }
    }

    // ── Wind ──────────────────────────────────────────────────────────────
    public static class Wind implements Serializable {
        @SerializedName("speed") public double speed;
        @SerializedName("deg")   public int    deg;

        public double getSpeed() { return speed; }
        public int    getDeg()   { return deg; }
    }

    // ── Clouds ────────────────────────────────────────────────────────────
    public static class Clouds implements Serializable {
        @SerializedName("all") public int all;

        public int getAll() { return all; }
    }

    // ── Sys ───────────────────────────────────────────────────────────────
    public static class Sys implements Serializable {
        @SerializedName("sunrise") public long   sunrise;
        @SerializedName("sunset")  public long   sunset;
        @SerializedName("country") public String country;

        public long   getSunrise() { return sunrise; }
        public long   getSunset()  { return sunset; }
        public String getCountry() { return country; }
    }

    // ── WeatherCondition ──────────────────────────────────────────────────
    public static class WeatherCondition implements Serializable {
        @SerializedName("id")          public int    id;
        @SerializedName("main")        public String main;
        @SerializedName("description") public String description;
        @SerializedName("icon")        public String icon;

        public int    getId()             { return id; }
        public String getMain()           { return main; }
        public String getDescription()    { return description; }
        public String getIcon()           { return icon; }
    }
}
