package com.home.myweather.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.home.myweather.data.model.DailyData;
import com.home.myweather.data.model.ForecastItem;

/**
 * Утилита для группировки почасовых прогнозов в дневные блоки.
 * Входные данные: список ForecastItem (почасовые данные)
 * Выходные данные: список DailyData (сгруппированные по дням)
 */
public final class ForecastGrouper {

    private ForecastGrouper() {}

    /**
     * Группирует почасовой прогноз по дням.
     * 
     * Для каждого дня вычисляет:
     * - tempMin и tempMax из всех элементов дня
     * - Максимальную вероятность осадков (pop)
     * - Описание из первого элемента дня
     * - Все часовые элементы (для деталей в адаптерах)
     *
     * @param items список почасовых прогнозов (обычно от API)
     * @return список дневных прогнозов, отсортированный по датам
     */
    public static List<DailyData> groupByDay(List<ForecastItem> items) {
        List<DailyData> result = new ArrayList<>();
        if (items == null || items.isEmpty()) return result;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDay = "";
        DailyData current = null;

        for (ForecastItem item : items) {
            String day = sdf.format(new Date(item.timestamp * 1000L));
            
            // Если это новый день — создаём новый DailyData
            if (!day.equals(currentDay)) {
                current = new DailyData();
                current.dateMillis = item.timestamp * 1000L;
                current.tempMin = item.main != null ? item.main.temp : 0;
                current.tempMax = item.main != null ? item.main.temp : 0;
                current.pop = item.pop;
                current.items = new ArrayList<>();
                current.items.add(item);
                current.description = ""; // Инициализируем описание
                
                // Описание из первого элемента дня
                if (item.weather != null && item.weather.length > 0 && item.weather[0] != null) {
                    current.description = item.weather[0].description;
                }
                
                result.add(current);
                currentDay = day;
            } else if (current != null) {
                // Продолжаем добавлять в текущий день
                current.items.add(item);
                
                // Обновляем min/max температур
                if (item.main != null) {
                    current.tempMin = Math.min(current.tempMin, item.main.temp);
                    current.tempMax = Math.max(current.tempMax, item.main.temp);
                }
                
                // Берём максимальную вероятность осадков
                current.pop = Math.max(current.pop, item.pop);
            }
        }

        return result;
    }
}
