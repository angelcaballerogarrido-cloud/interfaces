package com.example.newsfragments.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.newsfragments.utils.NotificationHelper;

/**
 * Worker periódico para notificaciones configurables.
 * Programado por WorkManager con intervalo elegido por el usuario.
 */
public class NotificationWorker extends Worker {

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String lastTitle = getApplicationContext()
                .getSharedPreferences("news_prefs", Context.MODE_PRIVATE)
                .getString("last_news_title", "Nueva noticia disponible");

        NotificationHelper.showNewsNotification(
                getApplicationContext(),
                "📰 Nueva noticia",
                lastTitle
        );

        return Result.success();
    }
}
