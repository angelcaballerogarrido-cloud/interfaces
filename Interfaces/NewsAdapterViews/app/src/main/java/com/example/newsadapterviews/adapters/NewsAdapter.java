package com.example.newsadapterviews.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.newsadapterviews.R;
import com.example.newsadapterviews.models.NewsItem;
import com.example.newsadapterviews.utils.ImageDownloader;

import java.util.List;

public class NewsAdapter extends BaseAdapter {

    private Context context;
    private List<NewsItem> newsList;
    private LayoutInflater inflater;

    public NewsAdapter(Context context, List<NewsItem> newsList) {
        this.context = context;
        this.newsList = newsList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return newsList.size();
    }

    @Override
    public Object getItem(int position) {
        return newsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // ¡La clave de los 20 puntos! Reutilizar convertView y usar el patrón ViewHolder (Wrapper)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_news, parent, false);
            holder = new ViewHolder();
            holder.imgThumbnail = convertView.findViewById(R.id.img_thumbnail);
            holder.tvTitle = convertView.findViewById(R.id.tv_title);
            holder.tvShortDesc = convertView.findViewById(R.id.tv_short_desc);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        NewsItem item = newsList.get(position);
        
        holder.tvTitle.setText(item.getTitle());
        holder.tvShortDesc.setText(item.getShortDescription());

        // Limpiar la imagen anterior para no mostrar imágenes mezcladas por el reciclaje rápido
        holder.imgThumbnail.setImageBitmap(null);
        
        // Cargar imagen de forma asíncrona con caché
        ImageDownloader.downloadImage(item.getThumbnailUrl(), holder.imgThumbnail);

        return convertView;
    }

    static class ViewHolder {
        ImageView imgThumbnail;
        TextView tvTitle;
        TextView tvShortDesc;
    }
}
