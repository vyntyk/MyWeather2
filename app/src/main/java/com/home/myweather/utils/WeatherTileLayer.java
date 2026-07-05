package com.home.myweather.utils;

/**
 * URL тайлов OpenWeatherMap для наложения на карту.
 * Каждый слой — отдельный tile URL с токеном API.
 * Документация: https://openweathermap.org/api/weathermaps
 */
public class WeatherTileLayer {

    public enum Layer {
        TEMPERATURE  ("temp_new",          "🌡 Температура"),
        PRECIPITATION("precipitation_new", "🌧 Осадки"),
        CLOUDS       ("clouds_new",        "☁ Облака"),
        WIND         ("wind_new",          "💨 Ветер");

        public final String owmCode;
        public final String label;

        Layer(String owmCode, String label) {
            this.owmCode = owmCode;
            this.label   = label;
        }
    }

    /**
     * Возвращает URL тайла OWM для заданного слоя.
     * {z}/{x}/{y} — стандартные плейсхолдеры MapLibre.
     */
    public static String tileUrl(Layer layer, String apiKey) {
        return "https://tile.openweathermap.org/map/"
                + layer.owmCode
                + "/{z}/{x}/{y}.png?appid="
                + apiKey;
    }

    /** ID источника в стиле MapLibre (уникален для каждого слоя). */
    public static String sourceId(Layer layer) {
        return "owm_source_" + layer.owmCode;
    }

    /** ID слоя в стиле MapLibre. */
    public static String layerId(Layer layer) {
        return "owm_layer_" + layer.owmCode;
    }
}
