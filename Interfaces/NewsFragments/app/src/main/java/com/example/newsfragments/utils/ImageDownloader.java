package com.example.newsfragments.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Descargador de imágenes asíncrono con caché LruCache en memoria.
 * Utiliza un ExecutorService de 4 hilos para las descargas de red,
 * sin bloquear jamás el hilo principal (UI Thread).
 */
public class ImageDownloader {

    private static LruCache<String, Bitmap> memoryCache;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private static final Handler uiHandler = new Handler(Looper.getMainLooper());

    static {
        // Asignamos 1/8 de la memoria máxima disponible para la caché
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public static void downloadImage(final String urlString, final ImageView imageView) {
        if (urlString == null || urlString.isEmpty()) return;

        // Comprobamos si ya está en caché
        Bitmap cached = memoryCache.get(urlString);
        if (cached != null) {
            imageView.setImageBitmap(cached);
            return;
        }

        // Tag para evitar imágenes mezcladas en vistas recicladas
        imageView.setTag(urlString);

        executorService.execute(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap downloadedBitmap = BitmapFactory.decodeStream(input);

                if (downloadedBitmap != null) {
                    memoryCache.put(urlString, downloadedBitmap);
                }

                uiHandler.post(() -> {
                    if (imageView.getTag() != null && imageView.getTag().equals(urlString)) {
                        imageView.setImageBitmap(downloadedBitmap);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static Bitmap getBitmapFromCache(String url) {
        return memoryCache.get(url);
    }
}
