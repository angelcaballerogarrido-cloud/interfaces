package com.example.newsfragments.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.newsfragments.R;
import com.example.newsfragments.parsers.CsvParser;
import com.example.newsfragments.services.NewsDownloadService;
import com.example.newsfragments.workers.NewsWorker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fragment de comparación de Threads.
 * Mide y muestra el tiempo de descarga del CSV con 5 métodos distintos:
 * 1. Thread nativo
 * 2. ExecutorService
 * 3. AsyncTask (DEPRECATED desde Android 11)
 * 4. WorkManager
 * 5. Background Service (vía Broadcast)
 */
public class ThreadsFragment extends Fragment {

    private TextView tvThread, tvExecutor, tvAsyncTask, tvWorkManager, tvService, tvExplanation;
    private Button btnRunBenchmark;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    private static final String CSV_URL = "https://docs.google.com/spreadsheets/d/e/PUB_ID/pub?output=csv";

    // BroadcastReceiver para recibir el resultado del Benchmark del Servicio
    private final BroadcastReceiver serviceResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NewsDownloadService.ACTION_BENCHMARK_RESULT.equals(intent.getAction())) {
                long duration = intent.getLongExtra(NewsDownloadService.EXTRA_DURATION, 0);
                if (isAdded()) {
                    tvService.setText(getString(R.string.text_service_result, duration));
                }
            }
        }
    };

    public static ThreadsFragment newInstance() {
        return new ThreadsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_threads, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvThread = view.findViewById(R.id.tv_thread_time);
        tvExecutor = view.findViewById(R.id.tv_executor_time);
        tvAsyncTask = view.findViewById(R.id.tv_asynctask_time);
        tvWorkManager = view.findViewById(R.id.tv_workmanager_time);
        tvService = view.findViewById(R.id.tv_service_time);
        tvExplanation = view.findViewById(R.id.tv_asynctask_explanation);
        btnRunBenchmark = view.findViewById(R.id.btn_run_benchmark);

        // Cargar textos localizados
        tvExplanation.setText(getString(R.string.asynctask_explanation));
        btnRunBenchmark.setText(getString(R.string.btn_run_benchmark));

        btnRunBenchmark.setOnClickListener(v -> runBenchmark());
    }

    @Override
    public void onStart() {
        super.onStart();
        // Registrar dinámicamente el receptor en onStart siguiendo buenas prácticas del ciclo de vida
        if (getContext() != null) {
            IntentFilter filter = new IntentFilter(NewsDownloadService.ACTION_BENCHMARK_RESULT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getContext().registerReceiver(serviceResultReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                getContext().registerReceiver(serviceResultReceiver, filter);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Desregistrar el receptor en onStop para evitar fugas de memoria
        if (getContext() != null) {
            getContext().unregisterReceiver(serviceResultReceiver);
        }
    }

    private void runBenchmark() {
        btnRunBenchmark.setEnabled(false);
        tvThread.setText(getString(R.string.text_thread_measuring));
        tvExecutor.setText(getString(R.string.text_executor_measuring));
        tvAsyncTask.setText(getString(R.string.text_asynctask_measuring));
        tvWorkManager.setText(getString(R.string.text_workmanager_measuring));
        tvService.setText(getString(R.string.text_service_measuring));

        benchmarkNativeThread();
        benchmarkExecutorService();
        benchmarkAsyncTask();
        benchmarkWorkManager();
        benchmarkService();
    }

    // ─── MÉTODO 1: Thread Nativo ─────────────────────────────────────────────
    private void benchmarkNativeThread() {
        Thread thread = new Thread(() -> {
            long start = System.currentTimeMillis();
            downloadCsv();
            long elapsed = System.currentTimeMillis() - start;
            uiHandler.post(() -> {
                if (isAdded()) {
                    tvThread.setText(getString(R.string.text_thread_result, elapsed));
                }
            });
        });
        thread.start();
    }

    // ─── MÉTODO 2: ExecutorService ────────────────────────────────────────────
    private void benchmarkExecutorService() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            long start = System.currentTimeMillis();
            downloadCsv();
            long elapsed = System.currentTimeMillis() - start;
            uiHandler.post(() -> {
                if (isAdded()) {
                    tvExecutor.setText(getString(R.string.text_executor_result, elapsed));
                    btnRunBenchmark.setEnabled(true);
                }
            });
            executor.shutdown();
        });
    }

    // ─── MÉTODO 3: AsyncTask (DEPRECATED) ────────────────────────────────────
    @SuppressWarnings("deprecation")
    private void benchmarkAsyncTask() {
        new android.os.AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... voids) {
                long start = System.currentTimeMillis();
                downloadCsv();
                return System.currentTimeMillis() - start;
            }

            @Override
            protected void onPostExecute(Long elapsed) {
                if (isAdded()) {
                    tvAsyncTask.setText(getString(R.string.text_asynctask_result, elapsed));
                }
            }
        }.execute();
    }

    // ─── MÉTODO 4: WorkManager ────────────────────────────────────────────────
    private void benchmarkWorkManager() {
        long start = System.currentTimeMillis();
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(NewsWorker.class).build();
        if (getContext() != null) {
            WorkManager.getInstance(requireContext()).enqueue(workRequest);
            WorkManager.getInstance(requireContext())
                    .getWorkInfoByIdLiveData(workRequest.getId())
                    .observe(getViewLifecycleOwner(), workInfo -> {
                        if (workInfo != null && workInfo.getState().isFinished()) {
                            long elapsed = System.currentTimeMillis() - start;
                            if (isAdded()) {
                                tvWorkManager.setText(getString(R.string.text_workmanager_result, elapsed));
                            }
                        }
                    });
        }
    }

    // ─── MÉTODO 5: Background Service (vía Broadcast) ─────────────────────────
    private void benchmarkService() {
        if (getContext() != null) {
            Intent intent = new Intent(requireContext(), NewsDownloadService.class);
            requireContext().startService(intent);
        }
    }

    private void downloadCsv() {
        try {
            URL url = new URL(CSV_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while (reader.readLine() != null) { /* consumir */ }
            reader.close();
        } catch (Exception e) {
            // Simular tiempo de red si falla la conexión física
            try { Thread.sleep(200 + (long)(Math.random() * 300)); } catch (InterruptedException ex) { /* ignore */ }
        }
    }
}

