package com.home.myweather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
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
            lastWeather   = (WeatherResponse) savedInstanceState.getSerializable("last_weather");
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
                    resultat.setText("Ошибка, нужен В..Н, интернет не работает");
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
        if (weather == null || weather.main == null) return;

        resultat.setText(String.format(Locale.getDefault(),
                "Температура: %.1f°C", weather.main.temp));

        if (weather.wind != null) {
            resultat2.setText(String.format(Locale.getDefault(),
                    "Ветер: %.1f м/с", weather.wind.speed));
        } else {
            resultat2.setText("Ветер: нет данных");
        }

        resultat3.setText(String.format(Locale.getDefault(),
                "Давление: %d гПа", weather.main.pressure));

        resultat4.setText(String.format(Locale.getDefault(),
                "Влажность: %d%%", weather.main.humidity));

        if (weather.weather != null
                && weather.weather.length > 0
                && weather.weather[0] != null) {
            String desc = weather.weather[0].description;
            resultat5.setText(desc != null && !desc.isEmpty() ? desc : "—");
        } else {
            resultat5.setText("—");
        }
    }

    @SuppressLint("NonConstantResourceId")
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