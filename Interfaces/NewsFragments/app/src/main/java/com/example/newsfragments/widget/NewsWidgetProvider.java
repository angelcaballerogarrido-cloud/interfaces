package com.example.newsfragments.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.newsfragments.R;
import com.example.newsfragments.activities.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Widget de pantalla de inicio que muestra:
 * - Hora actual (se actualiza cada minuto via ACTION_TIME_TICK)
 * - Titular de la última noticia (desde SharedPreferences)
 */
public class NewsWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        // Actualizamos el widget cada minuto con ACTION_TIME_TICK
        if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName component = new ComponentName(context, NewsWidgetProvider.class);
            int[] ids = manager.getAppWidgetIds(component);
            for (int id : ids) {
                updateWidget(context, manager, id);
            }
        }
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Hora actual formateada
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        // Titular de la última noticia
        String lastTitle = context.getSharedPreferences("news_prefs", Context.MODE_PRIVATE)
                .getString("last_news_title", "Abre la app para cargar noticias");

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_news);
        views.setTextViewText(R.id.widget_time, currentTime);
        views.setTextViewText(R.id.widget_news_title, lastTitle);

        // Al pulsar el widget → abre la app
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
