package com.example.newsadapterviews.parsers;

import android.os.Handler;
import android.os.Looper;

import com.example.newsadapterviews.models.NewsItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RssParser {
    
    public interface OnNewsParsedListener {
        void onParsed(List<NewsItem> newsList);
    }

    public static void fetchAndParseRss(String rssUrl, OnNewsParsedListener listener) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            List<NewsItem> list = new ArrayList<>();
            // Aquí iría el código real de parseo XML con XmlPullParser
            // Para mantener la flexibilidad del proyecto simulamos la extracción
            try {
                // Simulamos latencia de red para demostrar asincronía
                Thread.sleep(1000); 
                
                list.add(new NewsItem(
                        "[RSS] Última hora en Tecnología",
                        "Novedades destacadas del feed RSS",
                        "https://via.placeholder.com/150/FF8800/FFFFFF",
                        "Este es un ejemplo de cómo un parser distinto (RSS) devuelve la misma estructura de objetos NewsItem, permitiendo reutilizar el 100% de la interfaz gráfica y el NewsAdapter.",
                        "https://via.placeholder.com/600x400/FF8800/FFFFFF"
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }

            handler.post(() -> listener.onParsed(list));
        });
    }
}
