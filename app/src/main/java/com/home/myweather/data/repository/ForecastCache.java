package com.home.myweather.data.repository;

import com.home.myweather.data.model.ForecastItem;

import java.util.List;

/**
 * Простой in-memory кэш ответа 5-day/3-hour Forecast API.
 *
 * Нужен, чтобы NowFragment и ForecastFragment не делали два отдельных
 * сетевых запроса прогноза для одних и тех же координат при переключении
 * вкладок: первый фрагмент, который получил прогноз, кладёт его сюда,
 * второй сначала проверяет кэш и использует его, если координаты совпадают
 * и кэш ещё не устарел.
 */
public final class ForecastCache {

    private static final long TTL_MILLIS = 10 * 60 * 1000L; // 10 минут
    private static final double COORD_EPS = 0.01; // ~1 км на широте средних широт

    private static List<ForecastItem> items;
    private static double lat = Double.NaN;
    private static double lon = Double.NaN;
    private static long savedAt = 0L;

    private ForecastCache() {
    }

    /**
     * @return закэшированный список блоков прогноза для координат (lat, lon),
     * либо null, если кэша нет, он устарел или координаты не совпадают.
     */
    public static synchronized List<ForecastItem> get(double queryLat, double queryLon) {
        if (items == null) return null;
        if (System.currentTimeMillis() - savedAt > TTL_MILLIS) return null;
        if (Math.abs(queryLat - lat) > COORD_EPS || Math.abs(queryLon - lon) > COORD_EPS) return null;
        return items;
    }

    public static synchronized void put(double queryLat, double queryLon, List<ForecastItem> newItems) {
        lat = queryLat;
        lon = queryLon;
        items = newItems;
        savedAt = System.currentTimeMillis();
    }

    public static synchronized void clear() {
        items = null;
        lat = Double.NaN;
        lon = Double.NaN;
        savedAt = 0L;
    }
}
