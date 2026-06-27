# WeatherFlow — Архитектура приложения

## Структура пакетов

```
com.home.myweather/
├── ui/
│   ├── MainActivity.java             (точка входа, навигация)
│   ├── fragments/
│   │   ├── NowFragment.java          (экран "Сейчас")
│   │   ├── FiveDaysFragment.java     (экран "5 дней")
│   │   └── DetailDayFragment.java    (экран детали дня)
│   ├── adapters/
│   │   ├── HourlyAdapter.java        (почасовой прогноз)
│   │   └── DaysAdapter.java          (5 дней)
│   └── controllers/
│       ├── UiController.java         (старый — сохраняем)
│       └── WeatherFormatter.java     (старый — сохраняем)
├── network/
│   ├── RetrofitClient.java           (синглтон)
│   ├── WeatherApiService.java        (интерфейс)
│   └── WeatherRepository.java        (логика запросов)
│   └── GeocodingRepository.java      (геокодирование)
├── location/
│   └── LocationHelper.java           (старый — сохраняем)
├── model/
│   ├── WeatherResponse.java          (JSON-модель)
│   ├── GeoLocation.java              (координаты)
│   └── ComfortIndex.java             (новый — индекс комфорта)
└── BuildConfig (сгенерирован Gradle)
```

## Поток данных

```
Activity (точка входа)
├─→ BottomNavigationView (навигация между фрагментами)
└─→ Фрагменты (три экрана)
    ├─ NowFragment
    │  └─ WeatherRepository.fetchWeather()
    │     └─ onSuccess() → UiController.showWeather()
    ├─ FiveDaysFragment
    │  └─ DaysAdapter (RecyclerView)
    │     └─ item click → DetailDayFragment
    └─ DetailDayFragment
       └─ HourlyAdapter (RecyclerView горизонтальный)
```

## Ответственность каждого класса

| Класс | Строк | Задача |
|-------|-------|--------|
| NowFragment | ~80 | Экран "Сейчас": большая температура, карточка "Сейчас", почасовой прогноз горизонтально |
| FiveDaysFragment | ~60 | Экран "5 дней": список дней, клик открывает детали |
| DetailDayFragment | ~70 | Детали дня: график, статистика |
| DaysAdapter | ~50 | RecyclerView адаптер для дней |
| HourlyAdapter | ~40 | RecyclerView адаптер для часов (горизонтально) |
| ComfortIndex | ~40 | Вычисление рекомендаций по погоде |
| WeatherFormatter | ~40 | Уже готов |
| WeatherRepository | ~110 | Уже готов |
| LocationHelper | ~82 | Уже готов |

**Итого:** каждый новый файл < 100 строк, максимум одна ответственность.
