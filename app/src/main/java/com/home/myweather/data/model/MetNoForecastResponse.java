package com.home.myweather.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MetNoForecastResponse {
    @SerializedName("properties")
    public Properties properties;

    public static class Properties {
        @SerializedName("timeseries")
        public List<TimeSeries> timeSeries;
    }

    public static class TimeSeries {
        @SerializedName("time")
        public String time;

        @SerializedName("data")
        public TimeData data;
    }

    public static class TimeData {
        @SerializedName("instant")
        public Instant instant;

        @SerializedName("next_1_hours")
        public Period next1Hours;

        @SerializedName("next_6_hours")
        public Period next6Hours;

        @SerializedName("next_12_hours")
        public Period next12Hours;
    }

    public static class Instant {
        @SerializedName("details")
        public Details details;
    }

    public static class Period {
        @SerializedName("summary")
        public Summary summary;

        @SerializedName("details")
        public Details details;
    }

    public static class Summary {
        @SerializedName("symbol_code")
        public String symbolCode;
    }

    public static class Details {
        @SerializedName("air_pressure_at_sea_level")
        public Double airPressure;

        @SerializedName("air_temperature")
        public Double airTemperature;

        @SerializedName("cloud_area_fraction")
        public Double cloudCover;

        @SerializedName("relative_humidity")
        public Double humidity;

        @SerializedName("wind_from_direction")
        public Double windDirection;

        @SerializedName("wind_speed")
        public Double windSpeed;

        @SerializedName("probability_of_precipitation")
        public Double precipitationProbability;
    }
}
