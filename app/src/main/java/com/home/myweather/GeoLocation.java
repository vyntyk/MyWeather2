package com.home.myweather;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Модель ответа Geocoding API (один элемент массива).
 * Пример: [{name:"London", lat:51.509865, lon:-0.118092, country:"GB", ...}]
 */
public class GeoLocation implements Serializable {

    @SerializedName("name")
    public String name;

    @SerializedName("lat")
    public double lat;

    @SerializedName("lon")
    public double lon;

    @SerializedName("country")
    public String country;

    @SerializedName("state")
    public String state;
}
