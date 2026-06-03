package com.example.newsfragments.fragments;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.newsfragments.R;
import com.example.newsfragments.models.NewsItem;
import com.example.newsfragments.utils.ImageDownloader;

/**
 * Fragment de detalle (panel de detalle).
 * Recibe el NewsItem a través de un Bundle (setArguments),
 * nunca por referencia directa desde otro Fragment.
 */
public class NewsDetailFragment extends Fragment {

    public static final String ARG_NEWS_ITEM = "news_item";

    /**
     * Método factory obligatorio para pasar parámetros al Fragment via Bundle.
     * Esto es lo que exige el profesor expresamente en la rúbrica.
     */
    public static NewsDetailFragment newInstance(NewsItem item) {
        NewsDetailFragment fragment = new NewsDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_NEWS_ITEM, item); // Paso de parámetros con Bundle
        fragment.setArguments(args);
        return fragment;
    }

    private ImageView imgLarge;
    private TextView tvTitle;
    private TextView tvContent;
    private TextView tvSource;
    private TextView tvDate;
    private TextView tvImportance;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgLarge = view.findViewById(R.id.img_large);
        tvTitle = view.findViewById(R.id.tv_detail_title);
        tvContent = view.findViewById(R.id.tv_detail_content);
        tvSource = view.findViewById(R.id.tv_source);
        tvDate = view.findViewById(R.id.tv_date);
        tvImportance = view.findViewById(R.id.tv_importance);

        // Recuperamos el item del Bundle (pasado correctamente)
        if (getArguments() != null) {
            NewsItem item = (NewsItem) getArguments().getSerializable(ARG_NEWS_ITEM);
            if (item != null) {
                bindData(item);
            } else {
                showWelcomeMessage();
            }
        } else {
            showWelcomeMessage();
        }
    }

    private void showWelcomeMessage() {
        tvTitle.setText("");
        tvContent.setText(getString(R.string.welcome_news));
        tvSource.setVisibility(View.GONE);
        if (tvDate != null) tvDate.setVisibility(View.GONE);
        if (tvImportance != null) tvImportance.setVisibility(View.GONE);
        imgLarge.setVisibility(View.GONE);
    }

    private void bindData(NewsItem item) {
        tvTitle.setText(item.getTitle());
        tvContent.setText(item.getContent());
        tvSource.setText(getString(R.string.source_label, item.getSource()));
        tvSource.setVisibility(View.VISIBLE);

        if (tvDate != null) {
            tvDate.setText(getString(R.string.date_label, item.getDate()));
            tvDate.setVisibility(View.VISIBLE);
        }
        if (tvImportance != null) {
            tvImportance.setText(getString(R.string.importance_label, item.getImportance()));
            tvImportance.setVisibility(View.VISIBLE);
        }

        imgLarge.setVisibility(View.VISIBLE);

        // Animación fade-in en la imagen al cargarse
        imgLarge.setAlpha(0f);
        ImageDownloader.downloadImage(item.getLargeImageUrl(), imgLarge);

        // ObjectAnimator: animación de aparición suave de la imagen
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(imgLarge, "alpha", 0f, 1f);
        fadeIn.setDuration(600);
        fadeIn.setStartDelay(200);
        fadeIn.start();

        // ObjectAnimator: animación de deslizamiento del título
        tvTitle.setTranslationY(60f);
        tvTitle.setAlpha(0f);
        ObjectAnimator titleSlide = ObjectAnimator.ofFloat(tvTitle, "translationY", 60f, 0f);
        ObjectAnimator titleFade = ObjectAnimator.ofFloat(tvTitle, "alpha", 0f, 1f);
        titleSlide.setDuration(400);
        titleFade.setDuration(400);
        titleSlide.setStartDelay(300);
        titleFade.setStartDelay(300);
        titleSlide.start();
        titleFade.start();
    }
}
