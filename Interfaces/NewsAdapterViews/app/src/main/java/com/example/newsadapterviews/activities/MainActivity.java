package com.example.newsadapterviews.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.StackView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newsadapterviews.R;
import com.example.newsadapterviews.adapters.NewsAdapter;
import com.example.newsadapterviews.models.NewsItem;
import com.example.newsadapterviews.parsers.CsvParser;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity {

    private Spinner spinnerViewType;
    private FrameLayout containerAdapterView;
    private List<NewsItem> currentNews = new ArrayList<>();
    private NewsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerViewType = findViewById(R.id.spinner_view_type);
        containerAdapterView = findViewById(R.id.container_adapter_view);

        adapter = new NewsAdapter(this, currentNews);

        setupSpinner();
        loadData();
    }

    private void setupSpinner() {
        String[] viewTypes = {"ListView", "GridView", "StackView", "Gallery"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, viewTypes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerViewType.setAdapter(spinnerAdapter);

        spinnerViewType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changeAdapterViewType(viewTypes[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadData() {
        Toast.makeText(this, "Descargando datos CSV...", Toast.LENGTH_SHORT).show();
        String csvUrl = "https://docs.google.com/spreadsheets/d/e/2PACX-1vT1sg_v1LAThNKfcCsC02dqBMG53bJ2CEBDcNGdjxDQm5CXP-nm8BM4E6zTEIAnN_LTacHuXAcugB3H/pub?gid=511573264&single=true&output=csv";
        
        CsvParser.fetchAndParseCsv(csvUrl, newsList -> {
            currentNews.clear();
            currentNews.addAll(newsList);
            adapter.notifyDataSetChanged();
            Toast.makeText(MainActivity.this, "Datos cargados y cacheados", Toast.LENGTH_SHORT).show();
            changeAdapterViewType(spinnerViewType.getSelectedItem().toString());
        });
    }

    private void changeAdapterViewType(String type) {
        containerAdapterView.removeAllViews();
        AdapterView<?> adapterView = null;

        switch (type) {
            case "ListView":
                ListView listView = new ListView(this);
                adapterView = listView;
                break;
            case "GridView":
                GridView gridView = new GridView(this);
                gridView.setNumColumns(2);
                gridView.setHorizontalSpacing(8);
                gridView.setVerticalSpacing(8);
                adapterView = gridView;
                break;
            case "StackView":
                StackView stackView = new StackView(this);
                adapterView = stackView;
                break;
            case "Gallery":
                Gallery gallery = new Gallery(this);
                gallery.setSpacing(16);
                adapterView = gallery;
                break;
        }

        if (adapterView != null) {
            adapterView.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            
            // Un mismo Adaptador para todos los AdapterViews
            @SuppressWarnings("unchecked")
            AdapterView rawAdapterView = (AdapterView) adapterView;
            rawAdapterView.setAdapter(adapter);

            adapterView.setOnItemClickListener((parent, view, position, id) -> {
                NewsItem item = currentNews.get(position);
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("news_item", item);
                startActivity(intent);
            });

            containerAdapterView.addView(adapterView);
        }
    }
}
