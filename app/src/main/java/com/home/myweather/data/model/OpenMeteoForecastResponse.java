package com.home.myweather.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** Ответ Open-Meteo Forecast API — current + hourly + daily в одном запросе. */
public class OpenMeteoForecastResponse {

    @SerializedName("latitude")           public double  latitude;
    @SerializedName("longitude")          public double  longitude;
    @SerializedName("timezone")           public String  timezone;
    @SerializedName("utc_offset_seconds") public int     utcOffsetSeconds;

    @SerializedName("current") public Current current;
    @SerializedName("hourly")  public Hourly  hourly;
    @SerializedName("daily")   public Daily   daily;

    public static class Current {
        @SerializedName("time")                    public String time;
        @SerializedName("temperature_2m")          public double  temperature2m;
        @SerializedName("relative_humidity_2m")    public int     relativeHumidity2m;
        @SerializedName("apparent_temperature")    public double  apparentTemperature;
        @SerializedName("precipitation_probability") public int   precipitationProbability;
        @SerializedName("precipitation")           public double  precipitation;
        @SerializedName("weather_code")            public int     weatherCode;
        @SerializedName("cloud_cover")             public int     cloudCover;
        @SerializedName("surface_pressure")        public double  surfacePressure;
        @SerializedName("wind_speed_10m")          public double  windSpeed10m;
        @SerializedName("wind_direction_10m")      public int     windDirection10m;
        @SerializedName("is_day")                  public int     isDay;
    }

    public static class Hourly {
        @SerializedName("time")                      public List<String>  time;
        @SerializedName("temperature_2m")            public List<Double>  temperature2m;
        @SerializedName("relative_humidity_2m")      public List<Integer> relativeHumidity2m;
        @SerializedName("apparent_temperature")      public List<Double>  apparentTemperature;
        @SerializedName("precipitation_probability") public List<Integer> precipitationProbability;
        @SerializedName("weather_code")              public List<Integer> weatherCode;
        @SerializedName("wind_speed_10m")            public List<Double>  windSpeed10m;
        @SerializedName("wind_direction_10m")        public List<Integer> windDirection10m;
        @SerializedName("surface_pressure")          public List<Double>  surfacePressure;
        @SerializedName("visibility")                public List<Double>  visibility;
    }

    public static class Daily {
        @SerializedName("time")                          public List<String>  time;
        @SerializedName("weather_code")                  public List<Integer> weatherCode;
        @SerializedName("temperature_2m_max")            public List<Double>  temperature2mMax;
        @SerializedName("temperature_2m_min")            public List<Double>  temperature2mMin;
        @SerializedName("precipitation_probability_max") public List<Integer> precipitationProbabilityMax;
        @SerializedName("wind_speed_10m_max")            public List<Double>  windSpeed10mMax;
        @SerializedName("sunrise")                       public List<String>  sunrise;
        @SerializedName("sunset")                        public List<String>  sunset;
    }
}
