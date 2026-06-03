package com.example.newsfragments.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.newsfragments.workers.NotificationWorker;

public class NewsAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Ejecuta el worker que comprueba las noticias o lanza la notificación
        OneTimeWorkRequest checkNewsRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .build();

        WorkManager.getInstance(context).enqueue(checkNewsRequest);
    }
}
