package com.home.myweather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
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

        resultat = findViewById(R.id.resultat);
        resultat2 = findViewById(R.id.resultat2);
        resultat3 = findViewById(R.id.resultat3);
        resultat4 = findViewById(R.id.resultat4);
        resultat5 = findViewById(R.id.resultat5);
        mBackground = findViewById(R.id.background);
        user_field = findViewById(R.id.user_field);
        Button main_btn = findViewById(R.id.main_btn);

        weatherRepository = new WeatherRepository();

        if (savedInstanceState != null) {
            lastWeather = (WeatherResponse) savedInstanceState.getSerializable("last_weather");
            selectedBgRes = savedInstanceState.getInt("bg_res", R.drawable.foto4);
            
            if (lastWeather != null) {
                updateUI(lastWeather);
            }
        }
        mBackground.setBackgroundResource(selectedBgRes);

        main_btn.setOnClickListener(view -> {
            // Hide the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            String city = user_field.getText().toString().trim();
            if (!city.isEmpty()) {
                fetchWeather(city);
            } else {
                Toast.makeText(MainActivity.this, "Введите название города", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchWeather(String city) {
        resultat.setText("Загрузка...");
        weatherRepository.fetchWeather(city, null, new WeatherRepository.WeatherCallback() {
            @Override
            public void onSuccess(WeatherResponse weather, GeoLocation geo) {
                lastWeather = weather;
                updateUI(weather);
            }

            @Override
            public void onError(String message) {
                resultat.setText("Ошибка, нужен В..Н, интернет не работает");
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateUI(WeatherResponse weather) {
        resultat.setText(String.format(Locale.getDefault(), "Температура: %.1f°C", weather.main.temp));
        resultat2.setText(String.format(Locale.getDefault(), "Ветер: %.1f м/с", weather.wind.speed));
        resultat3.setText(String.format(Locale.getDefault(), "Давление: %d гПа", weather.main.pressure));
        resultat4.setText(String.format(Locale.getDefault(), "Влажность: %d%%", weather.main.humidity));
        resultat5.setText(weather.weather[0].description);
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
