package com.example.newsfragments.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.newsfragments.R;
import com.example.newsfragments.fragments.NewsDetailFragment;
import com.example.newsfragments.fragments.NewsListFragment;
import com.example.newsfragments.fragments.ThreadsFragment;
import com.example.newsfragments.models.NewsItem;
import com.example.newsfragments.utils.NotificationHelper;

import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity implements NewsListFragment.OnNewsSelectedListener {

    private boolean isDualPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        isDualPane = (findViewById(R.id.detail_container) != null);
        NotificationHelper.createNotificationChannel(this);

        // Solicitar permiso de notificaciones en tiempo de ejecución para Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        setupControls();

        // Registrar listener para ocultar controles de búsqueda/ordenación al ver el detalle o hilos en portrait
        getSupportFragmentManager().addOnBackStackChangedListener(this::updateControlsVisibility);
        updateControlsVisibility();

        if (savedInstanceState == null) {
            NewsListFragment listFragment = NewsListFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.list_container, listFragment)
                    .commit();

            if (isDualPane) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_container, NewsDetailFragment.newInstance(null))
                        .commit();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.list_container);
        boolean isDetailOrThreads = !isDualPane && (currentFragment instanceof NewsDetailFragment || currentFragment instanceof ThreadsFragment);
        
        MenuItem settingsItem = menu.findItem(R.id.action_settings);
        MenuItem threadsItem = menu.findItem(R.id.action_threads);
        
        if (settingsItem != null) {
            settingsItem.setVisible(!isDetailOrThreads);
        }
        if (threadsItem != null) {
            threadsItem.setVisible(!isDetailOrThreads);
        }
        
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getSupportFragmentManager().popBackStack();
            return true;
        } else if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.action_threads) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
            ft.replace(R.id.list_container, ThreadsFragment.newInstance());
            ft.addToBackStack(null);
            ft.commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public void onNewsSelected(NewsItem item) {
        NewsDetailFragment detailFragment = NewsDetailFragment.newInstance(item);

        if (isDualPane) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.detail_container, detailFragment)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                    .replace(R.id.list_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void setupControls() {
        EditText etSearch = findViewById(R.id.et_search);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Fragment listFragment = getSupportFragmentManager().findFragmentById(R.id.list_container);
                    if (listFragment instanceof NewsListFragment) {
                        ((NewsListFragment) listFragment).performSearch(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }

        Spinner spinnerSort = findViewById(R.id.spinner_sort);
        if (spinnerSort != null) {
            String[] sortOptions = { getString(R.string.sort_date), getString(R.string.sort_importance) };
            ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortOptions);
            sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSort.setAdapter(sortAdapter);

            spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Fragment listFragment = getSupportFragmentManager().findFragmentById(R.id.list_container);
                    if (listFragment instanceof NewsListFragment) {
                        ((NewsListFragment) listFragment).performSort(position);
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }

    private void updateControlsVisibility() {
        android.util.Log.d("MainActivity", "updateControlsVisibility called");
        View controls = findViewById(R.id.layout_controls);
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.list_container);
        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
        
        boolean isDetailOrThreads = currentFragment instanceof NewsDetailFragment || currentFragment instanceof ThreadsFragment || backStackCount > 0;
        
        if (!isDualPane) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(isDetailOrThreads);
                if (currentFragment instanceof NewsDetailFragment) {
                    Bundle args = currentFragment.getArguments();
                    if (args != null) {
                        NewsItem item = (NewsItem) args.getSerializable(NewsDetailFragment.ARG_NEWS_ITEM);
                        if (item != null) {
                            getSupportActionBar().setTitle(item.getTitle());
                        } else {
                            getSupportActionBar().setTitle(R.string.app_name);
                        }
                    } else {
                        getSupportActionBar().setTitle(R.string.app_name);
                    }
                } else if (currentFragment instanceof ThreadsFragment) {
                    getSupportActionBar().setTitle("Procesamiento en Hilos");
                } else {
                    getSupportActionBar().setTitle(R.string.app_name);
                }
            }
            if (controls != null) {
                if (isDetailOrThreads) {
                    controls.setVisibility(View.GONE);
                } else {
                    controls.setVisibility(View.VISIBLE);
                }
            }
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setTitle(R.string.app_name);
            }
            if (controls != null) {
                controls.setVisibility(View.VISIBLE);
            }
        }
        
        invalidateOptionsMenu();
    }
}
