## Раздел 2: Дизайн и UI/UX

**Дата**: 2026-06-30  
**Статус**: ✅ ЧАСТИЧНО РЕАЛИЗОВАНО

---

### 2.1 ✅ Единая палитра

- `values/colors.xml` — светлая тема (27 цветов с осмысленными именами)
- `values-night/colors.xml` — тёмная тема (автоматический перевод при включении ночного режима)
- `values/themes.xml` — обновлены на новую палитру, статус-бар адаптируется
- `values-night/themes.xml` — создана для тёмной темы

**Цвета:**
- Текст: `text_primary`, `text_secondary`, `text_tertiary`
- Фоны: `bg_primary`, `bg_secondary`, `bg_gradient_start/end`
- Карточки: `card_bg`, `card_border`
- Акценты: `accent_blue`, `success_green`, `warning_orange`, `error_red`
- Погода: `temp_cold/cool/mild/warm/hot` (синий → красный)

---

### 2.2 ✅ Иконки вместо эмодзи

Созданы 4 SVG-иконки:
- `ic_comfort.xml` — галочка (зелёная, для совета)
- `ic_wind.xml` — ветер (синяя)
- `ic_pressure.xml` — давление (оранжевая)
- `ic_humidity.xml` — влажность (голубая)

**Обновлён** `fragment_now.xml`:
- Заменены `😊` → `ImageView ic_comfort`
- Заменены `💨` → `ImageView ic_wind`
- Заменены `🌡` → `ImageView ic_pressure`
- Заменены `💧` → `ImageView ic_humidity`
- Все цвета из `colors.xml` (поддержка тёмной темы автоматическая)

---

### 2.3 ✅ SettingsFragment

**Было:** текст-заглушка (⚙ Настройки / • Тема / • Единицы)

**Стало:** реальные компоненты:
- `SwitchCompat` для тёмной темы (пересоздаёт `Activity`)
- `MaterialRadioButton` для выбора единиц (°C / °F)
- Карточки с `CardView` (стилизованные)
- Информация о приложении

**Реализованы:**
- Сохранение в `SharedPreferences` (`myweather_prefs`)
- Немедленное применение настроек

---

### 2.4 ⏳ Состояние загрузки

**TODO:** Добавить `ProgressBar` в `fragment_now.xml` вместо "—"/⏳

```xml
<ProgressBar
    android:id="@+id/progress_loading"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_gravity="center"
    android:visibility="gone" />
```

В `NowFragment.showLoading()` вызвать:
```java
progressBar.setVisibility(View.VISIBLE);
scrollContent.setVisibility(View.GONE);
```

---

### 2.5 ⏳ Карта: динамический список городов

**Текущее состояние:** 10 жёстко зашитых российских городов в `MapFragment.CITIES[][]`

**Требуется обсуждение:**
- Вариант A: Города выбираются по видимой области карты (сложно, требует геокодирования в реальном времени)
- Вариант B: Города = последние N найденных пользователем (из `FavoriteCitiesManager`)
- Вариант C: Оставить жёсткий список, но добавить возможность редактирования в Настройках

**Рекомендация:** Вариант B (города из избранного — логично и просто)

---

### 📊 Итого по разделу 2

| Пункт | Статус | Заметки |
|-------|--------|---------|
| 2.1 Палитра | ✅ | Light + Dark themes, 27 цветов |
| 2.2 Иконки | ✅ | 4 SVG, заменили эмодзи |
| 2.3 SettingsFragment | ✅ | SwitchCompat + RadioButton, SharedPreferences |
| 2.4 ProgressBar | ⏳ | Нужно добавить вручную |
| 2.5 Карта | ⏳ | Требует выбора варианта |

---

**Дальше:** раздел 3 (если нужно) или собрать проект?
