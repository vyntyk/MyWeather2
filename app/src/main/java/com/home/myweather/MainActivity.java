package com.home.myweather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView resultat;
    private TextView resultat2;
    private TextView resultat3;
    private TextView resultat4;
    private TextView resultat5;
    private ConstraintLayout mBackground;
    private EditText user_field;
    private WeatherRepository weatherRepository;
    private WeatherResponse lastWeather;
    private int selectedBgRes = R.drawable.foto4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Скрываем навигационную панель (три кнопки) и статусбар —
        // immersive sticky: панели появляются по свайпу и снова прячутся.
        hideSystemUI();

        resultat  = findViewById(R.id.resultat);
        resultat2 = findViewById(R.id.resultat2);
        resultat3 = findViewById(R.id.resultat3);
        resultat4 = findViewById(R.id.resultat4);
        resultat5 = findViewById(R.id.resultat5);
        mBackground = findViewById(R.id.background);
        user_field  = findViewById(R.id.user_field);
        Button main_btn = findViewById(R.id.main_btn);

        weatherRepository = new WeatherRepository();

        if (savedInstanceState != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                lastWeather = savedInstanceState.getSerializable("last_weather", WeatherResponse.class);
            } else {
                lastWeather = (WeatherResponse) savedInstanceState.getSerializable("last_weather");
            }
            selectedBgRes = savedInstanceState.getInt("bg_res", R.drawable.foto4);
            if (lastWeather != null) {
                updateUI(lastWeather);
            }
        }
        mBackground.setBackgroundResource(selectedBgRes);

        main_btn.setOnClickListener(view -> {
            // Скрываем клавиатуру
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            String city = user_field.getText().toString().trim();
            if (!city.isEmpty()) {
                fetchWeather(city);
            } else {
                Toast.makeText(MainActivity.this,
                        "Введите название города", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchWeather(String city) {
        // FIX: сбрасываем все поля, чтобы не показывались данные предыдущего запроса
        resultat.setText("Загрузка...");
        resultat2.setText("—");
        resultat3.setText("—");
        resultat4.setText("—");
        resultat5.setText("—");

        weatherRepository.fetchWeather(city, null, new WeatherRepository.WeatherCallback() {

            @Override
            public void onSuccess(WeatherResponse weather, GeoLocation geo) {
                // FIX: проверяем жизненный цикл перед обновлением UI,
                // чтобы не обращаться к уже уничтоженной Activity (утечка памяти / краш)
                runOnUiThread(() -> {
                    if (isDestroyed() || isFinishing()) return;
                    lastWeather = weather;
                    updateUI(weather);
                });
            }

            @Override
            public void onError(String message) {
                // FIX: аналогичная защита для ветки ошибки
                runOnUiThread(() -> {
                    if (isDestroyed() || isFinishing()) return;
                    resultat.setText("Нет соединения с интернетом");
                    resultat2.setText("—");
                    resultat3.setText("—");
                    resultat4.setText("—");
                    resultat5.setText("—");
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void updateUI(WeatherResponse weather) {
        // FIX: защита от NPE — проверяем все вложенные объекты перед обращением
        if (weather == null || weather.getMain() == null) return;

        resultat.setText(String.format(Locale.getDefault(),
                "Температура: %.1f°C", weather.getMain().getTemp()));

        if (weather.getWind() != null) {
            resultat2.setText(String.format(Locale.getDefault(),
                    "Ветер: %.1f м/с", weather.getWind().getSpeed()));
        } else {
            resultat2.setText("Ветер: нет данных");
        }

        resultat3.setText(String.format(Locale.getDefault(),
                "Давление: %d гПа", weather.getMain().getPressure()));

        resultat4.setText(String.format(Locale.getDefault(),
                "Влажность: %d%%", weather.getMain().getHumidity()));

        WeatherResponse.WeatherCondition[] conditions = weather.getWeather();
        if (conditions != null && conditions.length > 0 && conditions[0] != null) {
            String desc = conditions[0].getDescription();
            resultat5.setText(desc != null && !desc.isEmpty() ? desc : "—");
        } else {
            resultat5.setText("—");
        }
    }

    public void BG(View view) {
        int id = view.getId();
        if (id == R.id.btn1) {
            selectedBgRes = R.drawable.foto1;
        } else if (id == R.id.btn2) {
            selectedBgRes = R.drawable.foto2;
        } else if (id == R.id.btn3) {
            selectedBgRes = R.drawable.foto3;
        } else {
            // Неизвестная кнопка — не меняем фон
            return;
        }
        mBackground.setBackgroundResource(selectedBgRes);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // Переход из другого приложения / шторка возвращает панели — прячем снова.
        if (hasFocus) hideSystemUI();
    }

    @SuppressLint("WrongConstant")
    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+) — новый API
            WindowInsetsController controller =
                    getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.systemBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            // Android 7–10 (API 25–29) — старый API
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (lastWeather != null) {
            outState.putSerializable("last_weather", lastWeather);
        }
        outState.putInt("bg_res", selectedBgRes);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (weatherRepository != null) {
            weatherRepository.cancelPendingRequests();
        }
    }
}