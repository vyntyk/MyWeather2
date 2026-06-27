package com.home.myweather;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity extends AppCompatActivity {

    private UiController ui;
    private LocationHelper locationHelper;
    private WeatherRepository weatherRepository;
    private WeatherResponse lastWeather;
    private ConstraintLayout mBackground;
    private EditText userField;
    private int selectedBgRes = R.drawable.foto4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBackground = findViewById(R.id.background);
        userField   = findViewById(R.id.user_field);

        ui = new UiController(this,
                findViewById(R.id.resultat),  findViewById(R.id.resultat2),
                findViewById(R.id.resultat3), findViewById(R.id.resultat4),
                findViewById(R.id.resultat5), userField);

        ui.hideSystemUI();

        weatherRepository = new WeatherRepository();
        locationHelper    = new LocationHelper(this);

        findViewById(R.id.main_btn).setOnClickListener(v -> onSearchClick(v));
        findViewById(R.id.geo_btn).setOnClickListener(v -> onGeoClick());

        if (savedInstanceState != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                lastWeather = savedInstanceState.getSerializable("last_weather", WeatherResponse.class);
            } else {
                lastWeather = (WeatherResponse) savedInstanceState.getSerializable("last_weather");
            }
            selectedBgRes = savedInstanceState.getInt("bg_res", R.drawable.foto4);
            if (lastWeather != null) ui.showWeather(lastWeather);
        }
        mBackground.setBackgroundResource(selectedBgRes);
    }

    private void onSearchClick(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        String city = userField.getText().toString().trim();
        if (city.isEmpty()) {
            Toast.makeText(this, "Введите название города", Toast.LENGTH_SHORT).show();
            return;
        }
        ui.showLoading("Загрузка...");
        weatherRepository.fetchWeather(city, null, weatherCallback);
    }

    private void onGeoClick() {
        ui.showLoading("Определяем местоположение...");
        locationHelper.requestLocation(new LocationHelper.Callback() {
            @Override public void onLocationReady(double lat, double lon) {
                weatherRepository.fetchWeatherByCoords(lat, lon, weatherCallback);
            }
            @Override public void onError(String message) {
                runOnUiThread(() -> ui.showError(message));
            }
        });
    }

    private final WeatherRepository.WeatherCallback weatherCallback =
            new WeatherRepository.WeatherCallback() {
                @Override public void onSuccess(WeatherResponse w, GeoLocation geo) {
                    runOnUiThread(() -> {
                        if (isDestroyed() || isFinishing()) return;
                        lastWeather = w;
                        ui.showWeather(w);
                    });
                }
                @Override public void onError(String message) {
                    runOnUiThread(() -> {
                        if (isDestroyed() || isFinishing()) return;
                        ui.showError("Нет соединения с интернетом");
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                    });
                }
            };

    public void BG(View view) {
        int id = view.getId();
        if      (id == R.id.btn1) selectedBgRes = R.drawable.foto1;
        else if (id == R.id.btn2) selectedBgRes = R.drawable.foto2;
        else if (id == R.id.btn3) selectedBgRes = R.drawable.foto3;
        else return;
        mBackground.setBackgroundResource(selectedBgRes);
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) ui.hideSystemUI();
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        if (lastWeather != null) out.putSerializable("last_weather", lastWeather);
        out.putInt("bg_res", selectedBgRes);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (weatherRepository != null) weatherRepository.cancelPendingRequests();
    }
}
