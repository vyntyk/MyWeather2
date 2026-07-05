package com.home.myweather.utils;

/**
 * Утилита преобразования WMO-кодов погоды (Open-Meteo) в иконки и описания.
 *
 * WMO-коды: https://open-meteo.com/en/docs#weathervariables
 * Иконки отображаются на существующие OWM drawable-коды (ow_01d.xml и т.д.),
 * поэтому WeatherIcon.getResId() работает без изменений.
 */
public final class WmoWeatherCode {

    private WmoWeatherCode() {}

    /**
     * Возвращает строковый код иконки в формате OWM ("01d", "10n" и т.д.)
     * на основе WMO-кода и признака "день / ночь".
     */
    public static String getIconCode(int wmoCode, boolean isDay) {
        String s = isDay ? "d" : "n";
        switch (wmoCode) {
            case 0:
            case 1:  return "01" + s;  // ясно
            case 2:  return "02" + s;  // переменная облачность
            case 3:  return "04" + s;  // пасмурно
            case 45:
            case 48: return "50" + s;  // туман
            case 51:
            case 53:
            case 55:
            case 56:
            case 57: return "09" + s;  // морось
            case 61:
            case 63:
            case 65:
            case 66:
            case 67: return "10" + s;  // дождь
            case 71:
            case 73:
            case 75:
            case 77:
            case 85:
            case 86: return "13" + s;  // снег
            case 80:
            case 81:
            case 82: return "09" + s;  // ливни
            case 95:
            case 96:
            case 99: return "11" + s;  // гроза
            default: return "01" + s;
        }
    }

    /** Возвращает русское описание по WMO-коду. */
    public static String getDescription(int wmoCode) {
        switch (wmoCode) {
            case 0:  return "Ясно";
            case 1:  return "Преимущественно ясно";
            case 2:  return "Переменная облачность";
            case 3:  return "Пасмурно";
            case 45: return "Туман";
            case 48: return "Туман с инеем";
            case 51: return "Слабая морось";
            case 53: return "Морось";
            case 55: return "Сильная морось";
            case 56: return "Переохлажденная морось";
            case 57: return "Интенсивная переохлажденная морось";
            case 61: return "Слабый дождь";
            case 63: return "Дождь";
            case 65: return "Сильный дождь";
            case 66: return "Слабый ледяной дождь";
            case 67: return "Ледяной дождь";
            case 71: return "Слабый снег";
            case 73: return "Снег";
            case 75: return "Сильный снег";
            case 77: return "Снежные зёрна";
            case 80: return "Слабые ливни";
            case 81: return "Ливни";
            case 82: return "Сильные ливни";
            case 85: return "Слабые снежные ливни";
            case 86: return "Сильные снежные ливни";
            case 95: return "Гроза";
            case 96: return "Гроза с небольшим градом";
            case 99: return "Гроза с крупным градом";
            default: return "—";
        }
    }
}
