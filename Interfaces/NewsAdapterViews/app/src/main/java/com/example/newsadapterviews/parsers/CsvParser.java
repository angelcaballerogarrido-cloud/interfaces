package com.example.newsadapterviews.parsers;

import android.os.Handler;
import android.os.Looper;

import com.example.newsadapterviews.models.NewsItem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CsvParser {
    
    public interface OnNewsParsedListener {
        void onParsed(List<NewsItem> newsList);
    }

    public static void fetchAndParseCsv(String csvUrl, OnNewsParsedListener listener) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            List<NewsItem> list = new ArrayList<>();
            try {
                URL url = new URL(csvUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                
                String line;
                boolean isFirst = true;
                while ((line = reader.readLine()) != null) {
                    if (isFirst) { isFirst = false; continue; } // Skip header
                    // Limpiar comillas problemáticas generadas por Google Sheets si todo el texto está en la columna A
                    line = line.replace("\"", "");
                    String[] tokens = line.split(",");
                    if (tokens.length >= 5) {
                        list.add(new NewsItem(
                            tokens[0].trim(),
                            tokens[1].trim(),
                            tokens[2].trim(),
                            tokens[3].trim(),
                            tokens[4].trim()
                        ));
                    }
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
                // Fallback a datos estáticos si falla la URL del profesor
                list.addAll(getMockData());
            }

            handler.post(() -> listener.onParsed(list));
        });
    }

    private static List<NewsItem> getMockData() {
        List<NewsItem> mock = new ArrayList<>();
        mock.add(new NewsItem("Android 16 Revelado", "Nuevas funcionalidades del SO.", "https://via.placeholder.com/150", "Google ha anunciado oficialmente Android 16 con enormes mejoras en la privacidad, la gestión térmica y optimizaciones nativas para inteligencia artificial.", "https://via.placeholder.com/600x400"));
        mock.add(new NewsItem("IA en móviles", "La IA generativa llega a tu bolsillo.", "https://via.placeholder.com/150/FF0000/FFFFFF", "Los nuevos procesadores permiten ejecutar potentes modelos de lenguaje complejos sin depender de conexión a internet. Esto cambiará cómo interactuamos con las apps de la vida diaria.", "https://via.placeholder.com/600x400/FF0000/FFFFFF"));
        mock.add(new NewsItem("Desarrollo Eficiente", "Cómo usar ViewHolders correctamente.", "https://via.placeholder.com/150/00FF00/000000", "El uso de ViewHolders y reciclar la convertView ahorra drásticamente el consumo de memoria y CPU al scrollear en una lista. Esto es fundamental para sacar la nota máxima en este proyecto.", "https://via.placeholder.com/600x400/00FF00/000000"));
        mock.add(new NewsItem("El fin de Gallery", "StackView y ViewPager toman el relevo.", "https://via.placeholder.com/150/0000FF/FFFFFF", "Aunque Gallery se encuentra obsoleto desde la API 16 en favor de ViewPager, sigue siendo un excelente componente didáctico para entender el polimorfismo de AdapterViews.", "https://via.placeholder.com/600x400/0000FF/FFFFFF"));
        return mock;
    }
}
