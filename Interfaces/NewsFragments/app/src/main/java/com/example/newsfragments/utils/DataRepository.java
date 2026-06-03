package com.example.newsfragments.utils;

import android.os.Handler;
import android.os.Looper;

import com.example.newsfragments.models.NewsItem;
import com.example.newsfragments.parsers.CsvParser;

import java.util.List;
import java.util.Locale;

public class DataRepository {

    private static DataRepository instance;

    private static final String CSV_URL_ES = "https://docs.google.com/spreadsheets/d/e/2PACX-1vT1sg_v1LAThNKfcCsC02dqBMG53bJ2CEBDcNGdjxDQm5CXP-nm8BM4E6zTEIAnN_LTacHuXAcugB3H/pub?gid=511573264&single=true&output=csv";
    private static final String CSV_URL_EN = "https://docs.google.com/spreadsheets/d/e/2PACX-1vT1sg_v1LAThNKfcCsC02dqBMG53bJ2CEBDcNGdjxDQm5CXP-nm8BM4E6zTEIAnN_LTacHuXAcugB3H/pub?gid=2089152978&single=true&output=csv";
    private static final String CSV_URL_FR = "https://docs.google.com/spreadsheets/d/e/2PACX-1vT1sg_v1LAThNKfcCsC02dqBMG53bJ2CEBDcNGdjxDQm5CXP-nm8BM4E6zTEIAnN_LTacHuXAcugB3H/pub?gid=1392834842&single=true&output=csv";

    public interface NewsCallback {
        void onSuccess(List<NewsItem> noticias);
        void onError(Exception e);
    }

    private DataRepository() {
    }

    public static synchronized DataRepository getInstance() {
        if (instance == null) {
            instance = new DataRepository();
        }
        return instance;
    }

    public String getUrlForLocale() {
        String lang = Locale.getDefault().getLanguage();
        if (lang.equals("en")) {
            return CSV_URL_EN;
        } else if (lang.equals("fr")) {
            return CSV_URL_FR;
        }
        return CSV_URL_ES;
    }

    public void fetchNews(final NewsCallback callback) {
        String urlString = getUrlForLocale();
        
        CsvParser.fetchAndParseCsv(urlString, new CsvParser.OnNewsParsedListener() {
            @Override
            public void onParsed(List<NewsItem> newsList) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (newsList != null && !newsList.isEmpty()) {
                        callback.onSuccess(newsList);
                    } else {
                        callback.onError(new Exception("No data found or parsing failed"));
                    }
                });
            }
        });
    }
}
