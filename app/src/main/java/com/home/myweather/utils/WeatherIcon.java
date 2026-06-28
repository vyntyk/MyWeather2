package com.home.myweather.utils;

import com.home.myweather.R;

/**
 * Утилита для получения SVG-иконки погоды по коду OpenWeatherMap.
 * Все иконки хранятся в res/drawable (ow_01d.xml, ow_01n.xml и т.д.)
 * и компилируются в APK — интернет не нужен.
 *
 * Коды берутся из поля weather[0].icon в ответе API.
 */
public class WeatherIcon {

    /**
     * Возвращает R.drawable.ow_* по коду иконки OWM (например "01d", "10n").
     * Если код не распознан — возвращает ow_01d как заглушку.
     */
    public static int getResId(String iconCode) {
        if (iconCode == null) return R.drawable.ow_01d;
        switch (iconCode) {
            case "01d": return R.drawable.ow_01d;  // ясно, день
            case "01n": return R.drawable.ow_01n;  // ясно, ночь
            case "02d": return R.drawable.ow_02d;  // небольшая облачность, день
            case "02n": return R.drawable.ow_02n;  // небольшая облачность, ночь
            case "03d": return R.drawable.ow_03d;  // рассеянные облака
            case "03n": return R.drawable.ow_03n;
            case "04d": return R.drawable.ow_04d;  // сплошная облачность
            case "04n": return R.drawable.ow_04n;
            case "09d": return R.drawable.ow_09d;  // ливень
            case "09n": return R.drawable.ow_09n;
            case "10d": return R.drawable.ow_10d;  // дождь
            case "10n": return R.drawable.ow_10n;
            case "11d": return R.drawable.ow_11d;  // гроза
            case "11n": return R.drawable.ow_11n;
            case "13d": return R.drawable.ow_13d;  // снег
            case "13n": return R.drawable.ow_13n;
            case "50d": return R.drawable.ow_50d;  // туман
            case "50n": return R.drawable.ow_50n;
            default:    return R.drawable.ow_01d;
        }
    }
}
