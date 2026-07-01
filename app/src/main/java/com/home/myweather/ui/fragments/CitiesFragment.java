package com.home.myweather.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.home.myweather.R;
import com.home.myweather.helpers.FavoriteCitiesManager;
import com.home.myweather.ui.adapters.FavoriteCitiesAdapter;
import com.home.myweather.MainActivity;

public class CitiesFragment extends Fragment {

    private EditText etCityInput;
    private RecyclerView rvCities;
    private TextView tvEmpty;
    private FavoriteCitiesManager citiesManager;
    private FavoriteCitiesAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cities, container, false);

        etCityInput = v.findViewById(R.id.et_city_input);
        rvCities = v.findViewById(R.id.rv_favorite_cities);
        tvEmpty = v.findViewById(R.id.tv_empty_cities);

        citiesManager = new FavoriteCitiesManager(requireContext());

        rvCities.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        adapter = new FavoriteCitiesAdapter(new FavoriteCitiesAdapter.OnCityClickListener() {
            @Override
            public void onCityClick(String cityName) {
                onFavoriteCityClick(cityName);
            }

            @Override
            public void onCityRemove(String cityName) {
                citiesManager.removeCity(cityName);
                updateCitiesList();
                Toast.makeText(requireContext(), "Город удален", Toast.LENGTH_SHORT).show();
            }
        });
        
        rvCities.setAdapter(adapter);

        v.findViewById(R.id.btn_add_city).setOnClickListener(vv -> onAddCityClick());

        updateCitiesList();

        return v;
    }

    private void onAddCityClick() {
        String cityName = etCityInput.getText().toString().trim();
        
        if (cityName.isEmpty()) {
            Toast.makeText(requireContext(), "Введите название города", Toast.LENGTH_SHORT).show();
            return;
        }

        citiesManager.addCity(cityName);
        etCityInput.setText("");
        hideKeyboard();
        updateCitiesList();
        Toast.makeText(requireContext(), "Город добавлен", Toast.LENGTH_SHORT).show();
    }

    private void updateCitiesList() {
        List<String> cities = citiesManager.getCities();
        
        if (cities.isEmpty()) {
            rvCities.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            rvCities.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            adapter.submitList(cities);
        }
    }

    private void onFavoriteCityClick(String cityName) {
        hideKeyboard();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).loadWeatherFromFavoriteCity(cityName);
        }
    }

    private void hideKeyboard() {
        etCityInput.clearFocus();
        InputMethodManager imm = (InputMethodManager) requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getView() != null) {
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }
}

