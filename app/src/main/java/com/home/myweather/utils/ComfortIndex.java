package com.home.myweather.utils;

import com.home.myweather.R;

import java.util.Calendar;

/**
 * Утилита для вычисления индекса комфорта на основе погодных данных.
 * Возвращает человекочитаемую рекомендацию.
 */
public class ComfortIndex {

    public static String getComfortMessage(double temp, double windSpeed, int humidity, int pressure, double pop) {
        StringBuilder msg = new StringBuilder();

        // Проверка на осадки
        if (pop >= 0.7) {
            msg.append("☂ Возьмите зонт. ");
        } else if (pop >= 0.3) {
            msg.append("🌂 Может пойти дождь. ");
        }

        // Температурные рекомендации
        if (temp >= 30) {
            msg.append("🕶 Жарко — пейте больше воды. ");
        } else if (temp >= 25) {
            msg.append("🕶 Высокий UV — пригодятся солнцезащитные очки. ");
        } else if (temp >= 15 && temp < 25) {
            msg.append("😊 Отличный день для прогулки. ");
        } else if (temp >= 5 && temp < 15) {
            if (windSpeed > 5) {
                msg.append("🧥 Лучше надеть куртку — ветер прохладный. ");
            } else {
                msg.append("🧥 Прохладно — возьмите куртку. ");
            }
        } else if (temp >= -5 && temp < 5) {
            msg.append("🧣 Холодно — шапка и шарф не помешают. ");
        } else if (temp < -5) {
            msg.append("❄️ Мороз — одевайтесь теплее! ");
        }

        // Ветер
        if (windSpeed > 15) {
            msg.append("💨 Сильный ветер — осторожнее. ");
        } else if (windSpeed > 10 && temp > 15) {
            msg.append("🏃 Хорошая погода для пробежки. ");
        } else if (windSpeed > 5 && windSpeed <= 10 && temp > 15) {
            msg.append("🚴 Идеально для велосипеда. ");
        }

        // Давление
        if (pressure < 1000) {
            msg.append("📉 Низкое давление — берегите себя. ");
        }

        String result = msg.toString().trim();
        return result.isEmpty() ? "Погода нормальная" : result;
    }

    public static String getComfortEmoji(double temp, int weatherId) {
        if (weatherId >= 200 && weatherId < 300) return "⛈";
        if (weatherId >= 300 && weatherId < 400) return "🌧";
        if (weatherId >= 500 && weatherId < 600) return "🌧";
        if (weatherId >= 600 && weatherId < 700) return "❄️";
        if (weatherId >= 700 && weatherId < 800) return "🌫";
        if (weatherId == 800) {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            return (hour >= 6 && hour < 20) ? "☀️" : "🌙";
        }
        if (weatherId > 800) return "☁️";
        return "🌡";
    }

    public static int getBackgroundResource(int weatherId, boolean isDay) {
        if (weatherId >= 200 && weatherId < 300) return R.drawable.foto3; // storm → синий
        if (weatherId >= 300 && weatherId < 600) return R.drawable.foto3; // rain → синий
        if (weatherId >= 600 && weatherId < 700) return R.drawable.foto4; // snow → голубой
        if (weatherId >= 700 && weatherId < 800) return R.drawable.foto4; // fog → голубой
        if (weatherId == 800) return isDay ? R.drawable.foto2 : R.drawable.foto4; // clear
        return R.drawable.foto4; // cloudy
    }
}
