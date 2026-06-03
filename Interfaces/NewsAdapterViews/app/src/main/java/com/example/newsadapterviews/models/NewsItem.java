package com.example.newsadapterviews.models;

import java.io.Serializable;

public class NewsItem implements Serializable {
    private String title;
    private String shortDescription;
    private String thumbnailUrl;
    private String content;
    private String largeImageUrl;

    public NewsItem(String title, String shortDescription, String thumbnailUrl, String content, String largeImageUrl) {
        this.title = title;
        this.shortDescription = shortDescription;
        this.thumbnailUrl = thumbnailUrl;
        this.content = content;
        this.largeImageUrl = largeImageUrl;
    }

    public String getTitle() { return title; }
    public String getShortDescription() { return shortDescription; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getContent() { return content; }
    public String getLargeImageUrl() { return largeImageUrl; }
}
