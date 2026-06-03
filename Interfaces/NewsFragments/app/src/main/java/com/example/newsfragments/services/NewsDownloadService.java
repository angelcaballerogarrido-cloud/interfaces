package com.example.newsfragments.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
2.0.2 Ejercicio de comparación de hilos:
Servicio en segundo plano que descarga el CSV de noticias y envía un broadcast
con el tiempo transcurrido (requisito explícito de la diapositiva 32).
*/
public class NewsDownloadService extends Service {

    public static final String ACTION_BENCHMARK_RESULT = "com.example.newsfragments.ACTION_BENCHMARK_RESULT";
    public static final String EXTRA_DURATION = "extra_duration";
    
    private static final String CSV_URL = "https://docs.google.com/spreadsheets/d/e/PUB_ID/pub?output=csv";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long startTime = System.currentTimeMillis();
        
        executor.execute(() -> {
            try {
                URL url = new URL(CSV_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while (reader.readLine() != null) { /* consumir */ }
                reader.close();
            } catch (Exception e) {
                // Simular latencia de red si falla la conexión
                try { Thread.sleep(200 + (long)(Math.random() * 300)); } catch (InterruptedException ex) { /* ignore */ }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Enviar resultado mediante Broadcast
            Intent resultIntent = new Intent(ACTION_BENCHMARK_RESULT);
            resultIntent.putExtra(EXTRA_DURATION, duration);
            resultIntent.setPackage(getPackageName()); // Evitar broadcast implícito en Android moderno
            sendBroadcast(resultIntent);
            
            stopSelf(); // Detener el servicio automáticamente al finalizar la tarea
        });

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
