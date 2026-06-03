package com.example.newsadapterviews.utils;

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

public class ImageDownloader {

    // Caché en memoria obligatoria para los puntos extras
    private static LruCache<String, Bitmap> memoryCache;
    private static ExecutorService executorService = Executors.newFixedThreadPool(4);
    private static Handler uiHandler = new Handler(Looper.getMainLooper());

    static {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public static void downloadImage(String urlString, ImageView imageView) {
        if (urlString == null || urlString.isEmpty()) return;

        Bitmap bitmap = memoryCache.get(urlString);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        }

        imageView.setTag(urlString);

        executorService.execute(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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
}
