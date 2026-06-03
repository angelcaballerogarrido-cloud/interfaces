package com.example.newsfragments.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newsfragments.R;
import com.example.newsfragments.widget.NewsAlarmReceiver;
import com.example.newsfragments.utils.NotificationHelper;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchNotif;
    private Spinner spinnerFrecuencia;
    private TextView lblFrecuencia;
    private Button btnTestNow;

    private static final String PREFS_NAME = "NewsPrefs";
    private static final String KEY_ENABLED = "notif_enabled";
    private static final String KEY_INTERVAL = "notif_interval";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        switchNotif = findViewById(R.id.switch_alarm_notifications);
        spinnerFrecuencia = findViewById(R.id.spinner_alarm_interval);
        lblFrecuencia = findViewById(R.id.lbl_alarm_interval);
        btnTestNow = findViewById(R.id.btn_test_alarm);

        String[] tiempos = {"15 Minutos", "30 Minutos", "1 Hora", "12 Horas"};
        final long[] milisReales = {
                15 * 60 * 1000L,
                30 * 60 * 1000L,
                60 * 60 * 1000L,
                12 * 60 * 60 * 1000L
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tiempos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrecuencia.setAdapter(adapter);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isEnabled = prefs.getBoolean(KEY_ENABLED, false);
        long savedInterval = prefs.getLong(KEY_INTERVAL, milisReales[0]);

        switchNotif.setChecked(isEnabled);
        spinnerFrecuencia.setEnabled(isEnabled);
        lblFrecuencia.setAlpha(isEnabled ? 1.0f : 0.5f);

        int spinnerPos = 0;
        for (int i = 0; i < milisReales.length; i++) {
            if (milisReales[i] == savedInterval) spinnerPos = i;
        }
        spinnerFrecuencia.setSelection(spinnerPos);

        switchNotif.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_ENABLED, isChecked).apply();
            spinnerFrecuencia.setEnabled(isChecked);
            lblFrecuencia.setAlpha(isChecked ? 1.0f : 0.5f);

            if (isChecked) {
                int selectedPos = spinnerFrecuencia.getSelectedItemPosition();
                programarAlarmaConAlarmManager(milisReales[selectedPos]);
                Toast.makeText(this, "Alarma Activada", Toast.LENGTH_SHORT).show();
            } else {
                cancelarAlarma();
                Toast.makeText(this, "Alarma Cancelada", Toast.LENGTH_SHORT).show();
            }
        });

        spinnerFrecuencia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                long milis = milisReales[position];
                prefs.edit().putLong(KEY_INTERVAL, milis).apply();

                if (switchNotif.isChecked()) {
                    programarAlarmaConAlarmManager(milis);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnTestNow.setOnClickListener(v -> {
            NotificationHelper.showNewsNotification(
                    this, "📰 Prueba AlarmManager",
                    "Notificación disparada desde ajustes manualmente."
            );
        });
    }

    private void programarAlarmaConAlarmManager(long intervaloMilis) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NewsAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + intervaloMilis,
                    intervaloMilis,
                    pendingIntent
            );
        }
    }

    private void cancelarAlarma() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NewsAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
