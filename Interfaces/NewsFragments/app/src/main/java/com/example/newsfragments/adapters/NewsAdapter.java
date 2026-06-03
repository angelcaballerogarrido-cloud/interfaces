package com.example.newsfragments.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.newsfragments.R;
import com.example.newsfragments.models.NewsItem;
import com.example.newsfragments.utils.ImageDownloader;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends BaseAdapter implements Filterable {

    private final Context context;
    private List<NewsItem> newsList;
    private List<NewsItem> originalNewsList;
    private final LayoutInflater inflater;
    private CustomFilter filter;

    public NewsAdapter(Context context, List<NewsItem> newsList) {
        this.context = context;
        this.newsList = newsList;
        this.originalNewsList = new ArrayList<>(newsList);
        this.inflater = LayoutInflater.from(context);
    }

    public void updateData(List<NewsItem> newNewsList) {
        this.originalNewsList = new ArrayList<>(newNewsList);
        this.newsList = new ArrayList<>(newNewsList);
        notifyDataSetChanged();
    }

    @Override public int getCount() { return newsList.size(); }
    @Override public Object getItem(int position) { return newsList.get(position); }
    @Override public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_news, parent, false);
            holder = new ViewHolder();
            holder.imgThumbnail = convertView.findViewById(R.id.img_thumbnail);
            holder.tvTitle = convertView.findViewById(R.id.tv_title);
            holder.tvShortDesc = convertView.findViewById(R.id.tv_short_desc);
            holder.tvTime = convertView.findViewById(R.id.tv_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        NewsItem item = newsList.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvShortDesc.setText(item.getShortDescription());
        
        if (holder.tvTime != null) {
            holder.tvTime.setText("Hace 2h");
        }

        holder.imgThumbnail.setImageBitmap(null);
        ImageDownloader.downloadImage(item.getThumbnailUrl(), holder.imgThumbnail);

        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new CustomFilter();
        }
        return filter;
    }

    private class CustomFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                results.values = originalNewsList;
                results.count = originalNewsList.size();
            } else {
                List<NewsItem> filtered = new ArrayList<>();
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (NewsItem item : originalNewsList) {
                    if (item.getTitle().toLowerCase().contains(filterPattern) ||
                        item.getShortDescription().toLowerCase().contains(filterPattern)) {
                        filtered.add(item);
                    }
                }
                results.values = filtered;
                results.count = filtered.size();
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            newsList = (List<NewsItem>) results.values;
            notifyDataSetChanged();
        }
    }

    static class ViewHolder {
        ImageView imgThumbnail;
        TextView tvTitle;
        TextView tvShortDesc;
        TextView tvTime;
    }
}
