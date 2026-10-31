package com.home.myweather.testing;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.home.myweather.R;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FragmentTestActivity extends AppCompatActivity {
    public static final int CONTAINER_ID = View.generateViewId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_MyWeather);
        super.onCreate(savedInstanceState);
        FrameLayout container = new FrameLayout(this);
        container.setId(CONTAINER_ID);
        setContentView(container);
    }
}
