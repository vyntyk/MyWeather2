# My Weather 🌤️

Android-приложение для отображения текущей погоды по названию города.

[Google Play](https://play.google.com/store/apps/details?id=com.home.myweather)

---

## Архитектура API (двухшаговый процесс)

### Шаг 1 — Геокодирование: название города → координаты

```
GET http://api.openweathermap.org/geo/1.0/direct
    ?q={city},{country}
    &limit=1
    &appid={API_KEY}
```

**Пример:** `?q=Kyiv,UA&limit=1&appid=...`

**Ответ:**
```json
[
  {
    "name": "Москва",
    "lat": 50.4500336,
    "lon": 30.5241361,
    "country": "Ru",
    "state": "Moscow City"
  }
]
```

---

### Шаг 2 — Текущая погода: координаты → данные

```
GET https://api.openweathermap.org/data/2.5/weather
    ?lat={lat}
    &lon={lon}
    &appid={API_KEY}
    &units=metric
    &lang=ru
```

**Ответ (сокращённо):**
```json
{
  "coord": { "lat": 50.45, "lon": 30.52 },
  "name": "Москва",
  "main": {
    "temp": 12.5,
    "feels_like": 10.3,
    "humidity": 72,
    "pressure": 1012
  },
  "wind": { "speed": 4.2, "deg": 230 },
  "weather": [{ "description": "переменная облачность", "icon": "03d" }]
}
```

---

## Используемые классы

| Файл | Назначение |
|------|-----------|
| `WeatherApiService.java` | Retrofit-интерфейс с двумя endpoint'ами |
| `GeoLocation.java` | Модель ответа Geocoding API |
| `WeatherResponse.java` | Модель ответа Current Weather 2.5 |
| `RetrofitClient.java` | Singleton Retrofit-клиент |
| `WeatherRepository.java` | Двухшаговая бизнес-логика запроса |

---

## Пример использования WeatherRepository

```java
WeatherRepository repo = new WeatherRepository();

repo.fetchWeather("Moscow", "RU", new WeatherRepository.WeatherCallback() {
    @Override
    public void onSuccess(WeatherResponse weather, GeoLocation geo) {
        double temp     = weather.main.temp;
        String desc     = weather.weather[0].description;
        int    humidity = weather.main.humidity;
        double wind     = weather.wind.speed;
        // обновить UI...
    }

    @Override
    public void onError(String message) {
        // показать ошибку...
    }
});
```

---

## Зависимости (build.gradle)

```groovy
dependencies {
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
}
```

---

## Разрешения (AndroidManifest.xml)

```xml
<uses-permission android:name="android.permission.INTERNET" />
```
