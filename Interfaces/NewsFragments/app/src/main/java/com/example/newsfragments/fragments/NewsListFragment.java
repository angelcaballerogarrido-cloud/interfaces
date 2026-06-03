package com.example.newsfragments.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.newsfragments.R;
import com.example.newsfragments.adapters.NewsAdapter;
import com.example.newsfragments.models.NewsItem;
import com.example.newsfragments.utils.DataRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Fragment de lista de noticias (panel maestro).
 * Comunica la selección a la Activity a través de la interfaz OnNewsSelectedListener,
 * NUNCA directamente con el DetailFragment.
 */
public class NewsListFragment extends Fragment {

    public interface OnNewsSelectedListener {
        void onNewsSelected(NewsItem item);
    }

    private OnNewsSelectedListener listener;
    private ListView listView;
    private ProgressBar progressBar;
    private NewsAdapter adapter;
    private final List<NewsItem> newsList = new ArrayList<>();
    private final List<NewsItem> allNewsList = new ArrayList<>(); // Lista maestra para búsquedas
    private int currentSortCriteria = 0; // 0 = fecha, 1 = importancia
    private String currentSearchQuery = "";

    public static NewsListFragment newInstance() {
        return new NewsListFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnNewsSelectedListener) {
            listener = (OnNewsSelectedListener) context;
        } else {
            throw new RuntimeException(context + " debe implementar OnNewsSelectedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_list, container, false);
        listView = view.findViewById(R.id.list_news);
        progressBar = view.findViewById(R.id.progress_bar);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new NewsAdapter(requireContext(), newsList);
        listView.setAdapter(adapter);

        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(
                requireContext(), R.anim.layout_list_animation);
        listView.setLayoutAnimation(animation);

        listView.setOnItemClickListener((AdapterView<?> parent, View v, int position, long id) -> {
            NewsItem item = newsList.get(position);
            if (listener != null) {
                listener.onNewsSelected(item);
            }
        });

        loadData();
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        DataRepository.getInstance().fetchNews(new DataRepository.NewsCallback() {
            @Override
            public void onSuccess(List<NewsItem> noticias) {
                progressBar.setVisibility(View.GONE);
                allNewsList.clear();
                allNewsList.addAll(noticias);
                applyFilterAndSort();
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                // Aquí podrías mostrar un Toast de error
            }
        });
    }

    /**
     * Filtra la lista según la consulta y aplica el criterio de ordenación actual.
     */
    public void performSearch(String query) {
        currentSearchQuery = query;
        applyFilterAndSort();
    }

    /**
     * Aplica ordenación según el criterio (0 = Fecha, 1 = Importancia).
     */
    public void performSort(int criteria) {
        currentSortCriteria = criteria;
        applyFilterAndSort();
    }

    private void applyFilterAndSort() {
        newsList.clear();
        
        // 1. Filtrado
        if (currentSearchQuery == null || currentSearchQuery.isEmpty()) {
            newsList.addAll(allNewsList);
        } else {
            String query = currentSearchQuery.toLowerCase(Locale.getDefault());
            for (NewsItem item : allNewsList) {
                if (item.getTitle().toLowerCase(Locale.getDefault()).contains(query) ||
                    item.getShortDescription().toLowerCase(Locale.getDefault()).contains(query)) {
                    newsList.add(item);
                }
            }
        }

        // 2. Ordenación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (currentSortCriteria == 0) {
                // Fecha descendente (más recientes primero)
                newsList.sort((a, b) -> b.getDate().compareTo(a.getDate()));
            } else {
                // Importancia descendente (de 5 a 1)
                newsList.sort((a, b) -> Integer.compare(b.getImportance(), a.getImportance()));
            }
        } else {
            // Fallback para APIs antiguas sin Java 8 streams/lambda sort
            Collections.sort(newsList, (a, b) -> {
                if (currentSortCriteria == 0) {
                    return b.getDate().compareTo(a.getDate());
                } else {
                    return Integer.compare(b.getImportance(), a.getImportance());
                }
            });
        }

        adapter.notifyDataSetChanged();
        listView.scheduleLayoutAnimation();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}

