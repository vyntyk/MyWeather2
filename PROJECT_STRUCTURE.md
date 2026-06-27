# 📱 Структура проекта MyWeather2 - Clean Architecture

## 🏗️ Архитектурная организация

Проект переорганизирован в соответствии с **Clean Architecture** и **SOLID** принципами. Код разделен на логические слои по функциональности.

### 📂 Структура папок

```
src/main/java/com/home/myweather/
│
├── 📁 ui/                          # PRESENTATION LAYER (Слой представления)
│   ├── fragments/                  # UI Фрагменты
│   │   ├── CitiesFragment.java      # Экран выбора города
│   │   ├── NowFragment.java         # Текущая погода
│   │   ├── ForecastFragment.java    # Прогноз погоды
│   │   ├── FiveDaysFragment.java    # Прогноз на 5 дней
│   │   ├── DayDetailFragment.java   # Детали дня
│   │   ├── DetailDayFragment.java   # Альтернативный вид деталей
│   │   ├── MapFragment.java         # Карта
│   │   └── SettingsFragment.java    # Параметры
│   │
│   └── adapters/                   # RecyclerView Адаптеры
│       ├── DailyAdapter.java        # Адаптер для ежедневных данных
│       ├── DaysAdapter.java         # Адаптер для дней
│       ├── HourlyAdapter.java       # Адаптер для часовых данных
│       └── FavoriteCitiesAdapter.java # Адаптер для избранных городов
│
├── 📁 data/                         # DATA LAYER (Слой данных)
│   ├── network/                    # Работа с сетью/API
│   │   ├── RetrofitClient.java      # Конфигурация Retrofit
│   │   └── WeatherApiService.java   # API сервис
│   │
│   ├── repository/                 # Репозитории (CRUD операции)
│   │   ├── WeatherRepository.java   # Репозиторий погоды
│   │   └── GeocodingRepository.java # Репозиторий геокодирования
│   │
│   └── model/                      # Модели данных
│       ├── WeatherResponse.java     # Ответ API погода
│       ├── ForecastResponse.java    # Ответ API прогноз
│       ├── ForecastItem.java        # Элемент прогноза
│       ├── DailyData.java           # Данные за день
│       └── GeoLocation.java         # Геолокация
│
├── 📁 utils/                        # UTILITY LAYER (Утилиты)
│   ├── ComfortIndex.java            # Расчет индекса комфорта
│   └── WeatherFormatter.java        # Форматирование данных
│
├── 📁 helpers/                      # HELPER LAYER (Помощники)
│   ├── LocationHelper.java          # Работа с геолокацией
│   ├── FavoriteCitiesManager.java   # Управление избранными городами
│   └── UiController.java            # Контроль UI
│
└── MainActivity.java                # Главная активность приложения

```

---

## 🎯 Назначение каждого слоя

### 1. **UI Layer** (ui/)
- **Назначение**: Отвечает за взаимодействие с пользователем
- **Компоненты**:
  - **fragments/**: Фрагменты, которые отображают UI
  - **adapters/**: Адаптеры для связывания данных со списками (RecyclerView)
- **Зависимости**: Может зависеть от Data и Utils слоев

### 2. **Data Layer** (data/)
- **Назначение**: Управление источниками данных (API, БД, кэш)
- **Подслои**:
  - **network/**: Работа с REST API через Retrofit
  - **repository/**: Паттерн Repository для абстракции источников данных
  - **model/**: DTO (Data Transfer Objects) - модели данных с API
- **Зависимости**: Не зависит от UI

### 3. **Utils Layer** (utils/)
- **Назначение**: Вспомогательные функции для обработки данных
- **Компоненты**:
  - Расчеты (индекс комфорта)
  - Форматирование данных (преобразование формата времени, температуры и т.д.)
- **Зависимости**: Может зависеть от Data Model

### 4. **Helpers Layer** (helpers/)
- **Назначение**: Помощники для специфичных задач
- **Компоненты**:
  - `LocationHelper`: Работа с геолокацией
  - `FavoriteCitiesManager`: Управление избранными городами
  - `UiController`: Управление UI элементами
- **Зависимости**: Может зависеть от Data и Utils

---

## 📋 Принципы организации

### ✅ Применены принципы:

1. **Separation of Concerns** - каждый слой отвечает за одно
2. **Dependency Inversion** - зависимости указывают на абстракции
3. **Single Responsibility** - каждый класс имеет одну ответственность
4. **Layers Independence** - слои слабо связаны между собой

### 📦 Пакеты организованы по:

- **Функциональности** (fragments, adapters, network)
- **Слоям архитектуры** (ui, data, utils, helpers)
- **Типам компонентов** (в каждом слое свои подпапки)

---

## 🔄 Миграция импортов

Все import ссылки были автоматически обновлены:

| Было | Стало |
|------|-------|
| `import com.home.myweather.CitiesFragment;` | `import com.home.myweather.ui.fragments.CitiesFragment;` |
| `import com.home.myweather.RetrofitClient;` | `import com.home.myweather.data.network.RetrofitClient;` |
| `import com.home.myweather.WeatherRepository;` | `import com.home.myweather.data.repository.WeatherRepository;` |
| `import com.home.myweather.ComfortIndex;` | `import com.home.myweather.utils.ComfortIndex;` |

---

## 💡 Преимущества новой структуры

✨ **Улучшения:**
- 🎯 Четкое разделение ответственности
- 📚 Легче найти нужный класс
- 🔧 Проще проводить тестирование
- 🚀 Масштабируемость приложения
- 🔒 Лучшая инкапсуляция
- 🔗 Слабая связанность компонентов

---

## 🛠️ Что дальше?

При необходимости расширения:

```
├── 📁 domain/              # (Опционально) Бизнес-логика
│   ├── models/
│   └── usecase/
├── 📁 di/                  # Dependency Injection (Dagger/Hilt)
├── 📁 database/            # Room Database
└── 📁 notifications/       # Push уведомления
```

---

## 📄 Файлы модели

**Дата модели** находятся в `data/model/`:
- Содержат JSON структуры для десериализации
- Используются Gson/Retrofit для преобразования
- Экспортируют данные в Фрагменты

---

## 🔍 Пример использования

### До (неорганизованная структура)
```java
import com.home.myweather.CitiesFragment;
import com.home.myweather.WeatherRepository;
import com.home.myweather.ComfortIndex;
```

### После (организованная структура)
```java
import com.home.myweather.ui.fragments.CitiesFragment;
import com.home.myweather.data.repository.WeatherRepository;
import com.home.myweather.utils.ComfortIndex;
```

---

**Дата организации**: 2026-06-27  
**Архитектура**: Clean Architecture + Layered Architecture  
**Язык**: Java  
**Платформа**: Android
