package com.example.newsfragments.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BenchmarkUtils {

    private static final String TAG = "BenchmarkUtils";

    public static void probarThread() {
        long startTime = System.currentTimeMillis();
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simula trabajo
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long endTime = System.currentTimeMillis();
            Log.d(TAG, "Thread execution time: " + (endTime - startTime) + "ms");
        }).start();
    }

    public static void probarExecutor() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        long startTime = System.currentTimeMillis();
        executor.execute(() -> {
            try {
                Thread.sleep(1000); // Simula trabajo
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long endTime = System.currentTimeMillis();
            Log.d(TAG, "ExecutorService execution time: " + (endTime - startTime) + "ms");
        });
        executor.shutdown();
    }
}
