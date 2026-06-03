package com.example.newsfragments.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.newsfragments.parsers.CsvParser;

import java.util.concurrent.CountDownLatch;

/**
 * Worker de WorkManager para tareas en segundo plano.
 * Puede descargarse en segundo plano garantizadamente,
 * incluso si la app está cerrada.
 */
public class NewsWorker extends Worker {

    private static final String TAG = "NewsWorker";

    public NewsWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "NewsWorker: iniciando descarga en background...");

        // Simulamos descarga de datos
        CountDownLatch latch = new CountDownLatch(1);
        CsvParser.fetchAndParseCsv(
            "https://docs.google.com/spreadsheets/d/e/PUB_ID/pub?output=csv",
            newsList -> {
                Log.d(TAG, "NewsWorker: descargadas " + newsList.size() + " noticias.");
                // Guardamos el título de la última noticia en SharedPreferences
                if (!newsList.isEmpty()) {
                    getApplicationContext()
                        .getSharedPreferences("news_prefs", Context.MODE_PRIVATE)
                        .edit()
                        .putString("last_news_title", newsList.get(0).getTitle())
                        .apply();
                }
                latch.countDown();
            }
        );

        try {
            latch.await();
        } catch (InterruptedException e) {
            return Result.failure();
        }

        return Result.success();
    }
}
