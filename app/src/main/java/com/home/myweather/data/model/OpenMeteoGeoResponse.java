package com.home.myweather.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** Ответ Open-Meteo Geocoding API. */
public class OpenMeteoGeoResponse {

    @SerializedName("results")
    public List<Result> results;

    public static class Result {
        @SerializedName("id")           public long   id;
        @SerializedName("name")         public String name;
        @SerializedName("latitude")     public double latitude;
        @SerializedName("longitude")    public double longitude;
        @SerializedName("country_code") public String countryCode;
        @SerializedName("country")      public String country;
        @SerializedName("admin1")       public String admin1;
        @SerializedName("timezone")     public String timezone;
    }
}
