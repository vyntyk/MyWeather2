package com.home.myweather.ui.fragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.home.myweather.data.model.WeatherResponse;
import com.home.myweather.data.network.RetrofitClient;
import com.home.myweather.data.network.WeatherApiService;
import com.home.myweather.utils.WeatherTileLayer;

import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.maps.Style;
import org.maplibre.android.plugins.annotation.Symbol;
import org.maplibre.android.plugins.annotation.SymbolManager;
import org.maplibre.android.plugins.annotation.SymbolOptions;
import org.maplibre.android.style.layers.PropertyFactory;
import org.maplibre.android.style.layers.RasterLayer;
import org.maplibre.android.style.sources.RasterSource;
import org.maplibre.android.style.sources.TileSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final double DEFAULT_LAT  = 55.751244;
    private static final double DEFAULT_LON  = 37.618423;
    private static final double DEFAULT_ZOOM = 5.0;
    private static final String OSM_STYLE_URL =
            "https://basemaps.cartocdn.com/gl/voyager-gl-style/style.json";
    private static final String TEMP_ICON_ID = "temp_label_icon";

    private static final double[][] CITIES = {
            {55.751244, 37.618423},
            {59.939095, 30.315868},
            {56.838011, 60.597474},
            {43.115542, 131.885495},
            {51.660781, 39.200296},
            {51.768205, 55.096903},
            {54.989342, 82.904632},
            {53.195873, 50.100193},
            {48.708048, 44.513916},
            {55.030199, 82.920430},
    };

    private MapView          mapView;
    private MapLibreMap      mapLibreMap;
    private WeatherTileLayer.Layer activeLayer = null;
    private SymbolManager    symbolManager;
    private final List<Symbol> tempSymbols = new ArrayList<>();
    private ExecutorService  executor;
    private final Handler    mainHandler = new Handler(Looper.getMainLooper());

    private TextView btnNone, btnTemp, btnPrecip, btnClouds, btnWind;
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

    @Override
    public void onMapReady(@NonNull MapLibreMap map) {
        mapLibreMap = map;

        map.setStyle(new Style.Builder().fromUri(OSM_STYLE_URL), style -> {
            symbolManager = new SymbolManager(mapView, map, style);
            symbolManager.setIconAllowOverlap(true);
            symbolManager.setTextAllowOverlap(true);

            if (activeLayer != null) {
                if (activeLayer == WeatherTileLayer.Layer.TEMPERATURE) {
                    loadTemperatureMarkers();
                } else {
                    addOWMLayer(style, activeLayer);
                }
            }
        });

        map.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(new LatLng(lastLat, lastLon))
                        .zoom(DEFAULT_ZOOM)
                        .build()
        ));

        map.getUiSettings().setAllGesturesEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setAttributionEnabled(true);
    }

    private void switchLayer(@Nullable WeatherTileLayer.Layer newLayer) {
        activeLayer = newLayer;
        updateButtonStates();

        if (mapLibreMap == null) return;
        Style style = mapLibreMap.getStyle();
        if (style == null) return;

        for (WeatherTileLayer.Layer l : WeatherTileLayer.Layer.values()) {
            if (l != WeatherTileLayer.Layer.TEMPERATURE) {
                removeOWMLayer(style, l);
            }
        }
        clearTemperatureMarkers();

        if (newLayer == WeatherTileLayer.Layer.TEMPERATURE) {
            loadTemperatureMarkers();
        } else if (newLayer != null) {
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

        if (style.getSource(sourceId) == null) {
            TileSet tileSet = new TileSet("2.2.0", tileUrl);
            tileSet.setMaxZoom(12f);
            tileSet.setMinZoom(0f);
            RasterSource source = new RasterSource(sourceId, tileSet, 256);
            style.addSource(source);
        }

        if (style.getLayer(layerId) == null) {
            float opacity = (layer == WeatherTileLayer.Layer.CLOUDS
                    || layer == WeatherTileLayer.Layer.PRECIPITATION) ? 0.95f : 0.8f;
            RasterLayer rasterLayer = new RasterLayer(layerId, sourceId);
            rasterLayer.setProperties(PropertyFactory.rasterOpacity(opacity));
            style.addLayer(rasterLayer);
        }
    }

    private void removeOWMLayer(@NonNull Style style, @NonNull WeatherTileLayer.Layer layer) {
        String layerId  = WeatherTileLayer.layerId(layer);
        String sourceId = WeatherTileLayer.sourceId(layer);
        if (style.getLayer(layerId) != null)   style.removeLayer(layerId);
        if (style.getSource(sourceId) != null) style.removeSource(sourceId);
    }

    private void loadTemperatureMarkers() {
        String apiKey = BuildConfig.OPENWEATHER_API_KEY;
        if (apiKey == null || apiKey.isEmpty() || symbolManager == null) return;

        if (executor == null || executor.isShutdown()) {
            executor = Executors.newFixedThreadPool(4);
        }

        executor.execute(() -> {
            CountDownLatch latch = new CountDownLatch(CITIES.length);
            List<double[]> results = Collections.synchronizedList(new ArrayList<>());

            // Исправлено: RetrofitClient.getInstance().getApiService()
            WeatherApiService service = RetrofitClient.getInstance().getApiService();

            for (double[] city : CITIES) {
                double lat = city[0];
                double lon = city[1];

                Call<WeatherResponse> call = service.getCurrentWeather(
                        lat, lon, apiKey, "metric", "ru"
                );

                call.enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<WeatherResponse> call,
                                           @NonNull Response<WeatherResponse> response) {
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                double temp = response.body().getMain().getTemp();
                                results.add(new double[]{lat, lon, temp});
                            }
                        } finally {
                            latch.countDown();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<WeatherResponse> call,
                                          @NonNull Throwable t) {
                        latch.countDown();
                    }
                });
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            mainHandler.post(() -> {
                if (!isAdded() || symbolManager == null) return;
                clearTemperatureMarkers();

                Style style = mapLibreMap != null ? mapLibreMap.getStyle() : null;
                if (style == null) return;

                if (style.getImage(TEMP_ICON_ID) == null) {
                    style.addImage(TEMP_ICON_ID, createTransparentBitmap());
                }

                for (double[] r : results) {
                    String tempText = String.format("%.0f°C", r[2]);
                    SymbolOptions opts = new SymbolOptions()
                            .withLatLng(new LatLng(r[0], r[1]))
                            .withIconImage(TEMP_ICON_ID)
                            .withTextField(tempText)
                            .withTextSize(14f)
                            .withTextColor(tempColor(r[2]))
                            .withTextHaloColor("rgba(255,255,255,1)")
                            .withTextHaloWidth(2f)
                            .withTextOffset(new Float[]{0f, 0f});
                    tempSymbols.add(symbolManager.create(opts));
                }
            });
        });
    }

    private String tempColor(double temp) {
        if (temp <= 0)  return "rgba(50,120,220,1)";
        if (temp <= 10) return "rgba(80,180,180,1)";
        if (temp <= 20) return "rgba(60,160,60,1)";
        if (temp <= 28) return "rgba(220,150,0,1)";
        return "rgba(210,50,30,1)";
    }

    private Bitmap createTransparentBitmap() {
        Bitmap bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        bmp.eraseColor(Color.TRANSPARENT);
        return bmp;
    }

    private void clearTemperatureMarkers() {
        if (symbolManager != null && !tempSymbols.isEmpty()) {
            symbolManager.delete(tempSymbols);
            tempSymbols.clear();
        }
    }

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
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
            executor = null;
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
