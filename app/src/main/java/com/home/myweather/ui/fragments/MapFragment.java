package com.home.myweather.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.home.myweather.BuildConfig;
import com.home.myweather.MainActivity;
import com.home.myweather.R;
import com.home.myweather.utils.WeatherTileLayer;

import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.maps.Style;
import org.maplibre.android.style.layers.RasterLayer;
import org.maplibre.android.style.sources.RasterSource;
import org.maplibre.android.style.sources.TileSet;

/**
 * MapFragment — карта погоды на базе MapLibre + OpenStreetMap + OWM тайлы.
 *
 * Возможности:
 *  - OSM базовый слой
 *  - Погодные слои: температура, осадки, облака, ветер
 *  - Переключение слоёв кнопками внизу
 *  - Текущее местоположение (через MainActivity.requestGeoLocation)
 *  - Масштабирование и жесты (встроено в MapLibre)
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    // Координаты по умолчанию (Москва)
    private static final double DEFAULT_LAT  = 55.751244;
    private static final double DEFAULT_LON  = 37.618423;
    private static final double DEFAULT_ZOOM = 5.0;

    // OSM стиль
    private static final String OSM_STYLE_URL =
            "https://basemaps.cartocdn.com/gl/voyager-gl-style/style.json";

    private MapView          mapView;
    private MapLibreMap      mapLibreMap;
    private WeatherTileLayer.Layer activeLayer = null; // null = нет OWM слоя

    // Кнопки слоёв
    private TextView btnNone, btnTemp, btnPrecip, btnClouds, btnWind;

    // Последнее известное местоположение
    private double lastLat = DEFAULT_LAT;
    private double lastLon = DEFAULT_LON;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        MapLibre.getInstance(requireContext());
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = v.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        initLayerButtons(v);
        initLocationButton(v);

        return v;
    }

    // ── Инициализация кнопок ────────────────────────────────────────────

    private void initLayerButtons(View v) {
        btnNone   = v.findViewById(R.id.btn_layer_none);
        btnTemp   = v.findViewById(R.id.btn_layer_temp);
        btnPrecip = v.findViewById(R.id.btn_layer_precipitation);
        btnClouds = v.findViewById(R.id.btn_layer_clouds);
        btnWind   = v.findViewById(R.id.btn_layer_wind);

        btnNone  .setOnClickListener(x -> switchLayer(null));
        btnTemp  .setOnClickListener(x -> switchLayer(WeatherTileLayer.Layer.TEMPERATURE));
        btnPrecip.setOnClickListener(x -> switchLayer(WeatherTileLayer.Layer.PRECIPITATION));
        btnClouds.setOnClickListener(x -> switchLayer(WeatherTileLayer.Layer.CLOUDS));
        btnWind  .setOnClickListener(x -> switchLayer(WeatherTileLayer.Layer.WIND));
    }

    private void initLocationButton(View v) {
        FloatingActionButton fab = v.findViewById(R.id.fab_my_location);
        fab.setOnClickListener(x -> {
            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).requestGeoLocation();
            }
        });
    }

    // ── OnMapReadyCallback ───────────────────────────────────────────────

    @Override
    public void onMapReady(@NonNull MapLibreMap map) {
        mapLibreMap = map;

        // Загружаем OSM стиль
        map.setStyle(new Style.Builder().fromUri(OSM_STYLE_URL), style -> {
            // Стиль загружен — можно добавлять слои
            // Если есть активный слой (после поворота) — восстанавливаем
            if (activeLayer != null) {
                addOWMLayer(style, activeLayer);
            }
        });

        // Позиция камеры по умолчанию
        map.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(new LatLng(lastLat, lastLon))
                        .zoom(DEFAULT_ZOOM)
                        .build()
        ));

        // Включаем жесты (по умолчанию в MapLibre уже включены)
        map.getUiSettings().setAllGesturesEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setAttributionEnabled(true);
    }

    // ── Переключение слоёв ───────────────────────────────────────────────

    private void switchLayer(@Nullable WeatherTileLayer.Layer newLayer) {
        activeLayer = newLayer;
        updateButtonStates();

        if (mapLibreMap == null) return;
        Style style = mapLibreMap.getStyle();
        if (style == null) return;

        // Удаляем все OWM слои
        for (WeatherTileLayer.Layer l : WeatherTileLayer.Layer.values()) {
            removeOWMLayer(style, l);
        }

        // Добавляем новый (если выбран)
        if (newLayer != null) {
            addOWMLayer(style, newLayer);
        }
    }

    private void addOWMLayer(@NonNull Style style, @NonNull WeatherTileLayer.Layer layer) {
        String apiKey = BuildConfig.OPENWEATHER_API_KEY;
        if (apiKey == null || apiKey.isEmpty()) {
            Toast.makeText(getContext(), "API ключ OWM не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        String sourceId = WeatherTileLayer.sourceId(layer);
        String layerId  = WeatherTileLayer.layerId(layer);
        String tileUrl  = WeatherTileLayer.tileUrl(layer, apiKey);

        // Источник ещё не добавлен?
        if (style.getSource(sourceId) == null) {
            TileSet tileSet = new TileSet("2.2.0", tileUrl);
            tileSet.setMaxZoom(12f);
            tileSet.setMinZoom(0f);
            RasterSource source = new RasterSource(sourceId, tileSet, 256);
            style.addSource(source);
        }

        // Слой ещё не добавлен?
        if (style.getLayer(layerId) == null) {
            RasterLayer rasterLayer = new RasterLayer(layerId, sourceId);
            rasterLayer.setProperties(
                    org.maplibre.android.style.layers.PropertyFactory.rasterOpacity(0.7f)
            );
            style.addLayer(rasterLayer);
        }
    }

    private void removeOWMLayer(@NonNull Style style, @NonNull WeatherTileLayer.Layer layer) {
        String layerId  = WeatherTileLayer.layerId(layer);
        String sourceId = WeatherTileLayer.sourceId(layer);
        if (style.getLayer(layerId) != null) {
            style.removeLayer(layerId);
        }
        if (style.getSource(sourceId) != null) {
            style.removeSource(sourceId);
        }
    }

    // ── Подсветка активной кнопки ────────────────────────────────────────

    private void updateButtonStates() {
        setActive(btnNone,   activeLayer == null);
        setActive(btnTemp,   activeLayer == WeatherTileLayer.Layer.TEMPERATURE);
        setActive(btnPrecip, activeLayer == WeatherTileLayer.Layer.PRECIPITATION);
        setActive(btnClouds, activeLayer == WeatherTileLayer.Layer.CLOUDS);
        setActive(btnWind,   activeLayer == WeatherTileLayer.Layer.WIND);
    }

    private void setActive(TextView btn, boolean active) {
        if (btn == null) return;
        btn.setBackgroundResource(active
                ? R.drawable.layer_btn_active
                : R.drawable.layer_btn_inactive);
    }

    // ── Публичный метод: переместить карту к местоположению ─────────────

    @SuppressLint("MissingPermission")
    public void moveToLocation(double lat, double lon) {
        lastLat = lat;
        lastLon = lon;
        if (mapLibreMap == null) return;
        mapLibreMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(new LatLng(lat, lon))
                        .zoom(10.0)
                        .build()
        ), 800);
    }

    // ── Жизненный цикл MapView ───────────────────────────────────────────

    @Override public void onStart()   { super.onStart();   mapView.onStart();   }
    @Override public void onResume()  { super.onResume();  mapView.onResume();  }
    @Override public void onPause()   { super.onPause();   mapView.onPause();   }
    @Override public void onStop()    { super.onStop();    mapView.onStop();    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
