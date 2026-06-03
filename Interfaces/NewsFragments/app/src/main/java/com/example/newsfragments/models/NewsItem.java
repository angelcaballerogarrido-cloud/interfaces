package com.example.newsfragments.models;

import java.io.Serializable;

public class NewsItem implements Serializable {
    private String title;
    private String shortDescription;
    private String thumbnailUrl;
    private String content;
    private String largeImageUrl;
    private String source; // CSV o RSS
    private String date; // Formato YYYY-MM-DD
    private int importance; // Escala 1 a 5

    public NewsItem(String title, String shortDescription, String thumbnailUrl,
                    String content, String largeImageUrl) {
        this.title = title;
        this.shortDescription = shortDescription;
        this.thumbnailUrl = thumbnailUrl;
        this.content = content;
        this.largeImageUrl = largeImageUrl;
        this.source = "CSV";
        this.date = "2026-05-22"; // Valor por defecto
        this.importance = 3;      // Valor por defecto
    }

    public NewsItem(String title, String shortDescription, String thumbnailUrl,
                    String content, String largeImageUrl, String date, int importance) {
        this.title = title;
        this.shortDescription = shortDescription;
        this.thumbnailUrl = thumbnailUrl;
        this.content = content;
        this.largeImageUrl = largeImageUrl;
        this.source = "CSV";
        this.date = date;
        this.importance = importance;
    }

    public String getTitle() { return title; }
    public String getShortDescription() { return shortDescription; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getContent() { return content; }
    public String getLargeImageUrl() { return largeImageUrl; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public int getImportance() { return importance; }
    public void setImportance(int importance) { this.importance = importance; }
}

