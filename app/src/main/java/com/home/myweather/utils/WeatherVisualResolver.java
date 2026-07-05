package com.home.myweather.utils;

import com.home.myweather.R;

/**
 * Emoji и фоны по WMO-коду погоды (Open-Meteo).
 */
public final class WeatherVisualResolver {

    private WeatherVisualResolver() {}

    public static String getEmoji(int wmoCode, boolean isDay) {
        if (isThunderstorm(wmoCode)) return "⛈";
        if (isDrizzleOrRain(wmoCode)) return "🌧";
        if (isSnow(wmoCode)) return "❄️";
        if (isFog(wmoCode)) return "🌫";
        if (wmoCode == 0 || wmoCode == 1) return isDay ? "☀️" : "🌙";
        if (wmoCode == 2 || wmoCode == 3) return "☁️";
        return "🌡";
    }

    public static int getBackground(int wmoCode, boolean isDay) {
        if (isThunderstorm(wmoCode) || isDrizzleOrRain(wmoCode)) return R.drawable.foto3;
        if (isSnow(wmoCode) || isFog(wmoCode)) return R.drawable.foto4;
        if (wmoCode == 0 || wmoCode == 1) return isDay ? R.drawable.foto2 : R.drawable.foto4;
        return R.drawable.foto4;
    }

    private static boolean isThunderstorm(int code) {
        return code == 95 || code == 96 || code == 99;
    }

    private static boolean isDrizzleOrRain(int code) {
        return (code >= 51 && code <= 67) || (code >= 80 && code <= 82);
    }

    private static boolean isSnow(int code) {
        return (code >= 71 && code <= 77) || code == 85 || code == 86;
    }

    private static boolean isFog(int code) {
        return code == 45 || code == 48;
    }
}
