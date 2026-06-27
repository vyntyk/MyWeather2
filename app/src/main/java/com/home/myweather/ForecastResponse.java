package com.home.myweather;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * Модель ответа 5-day / 3-hour Forecast API.
 * GET /data/2.5/forecast
 */
public class ForecastResponse implements Serializable {

    @SerializedName("city")
    public CityInfo city;

    @SerializedName("list")
    public List<ForecastItem> list;

    @SerializedName("cnt")
    public int count;

    public static class CityInfo implements Serializable {
        @SerializedName("id")
        public int id;

        @SerializedName("name")
        public String name;

        @SerializedName("coord")
        public GeoLocation coord;

        @SerializedName("country")
        public String country;

        @SerializedName("sunrise")
        public long sunrise;

        @SerializedName("sunset")
        public long sunset;
    }
}
