package com.example.newsadapterviews.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newsadapterviews.R;
import com.example.newsadapterviews.models.NewsItem;
import com.example.newsadapterviews.utils.ImageDownloader;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ImageView imgLarge = findViewById(R.id.img_large);
        TextView tvDetailTitle = findViewById(R.id.tv_detail_title);
        TextView tvDetailSubtitle = findViewById(R.id.tv_detail_subtitle);
        TextView tvDetailContent = findViewById(R.id.tv_detail_content);

        NewsItem item = (NewsItem) getIntent().getSerializableExtra("news_item");
        
        if (item != null) {
            tvDetailTitle.setText(item.getTitle());
            tvDetailSubtitle.setText(item.getShortDescription());
            tvDetailContent.setText(item.getContent());
            
            ImageDownloader.downloadImage(item.getLargeImageUrl(), imgLarge);
        }
    }
}
