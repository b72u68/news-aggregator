package com.example.newsaggregator;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NewsArticleViewHolder extends RecyclerView.ViewHolder {

    TextView title;
    TextView author;
    TextView publishedAt;
    ImageView image;
    TextView description;
    TextView pageNum;

    public NewsArticleViewHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.article_title);
        publishedAt = itemView.findViewById(R.id.article_published_at);
        author = itemView.findViewById(R.id.article_author);
        image = itemView.findViewById(R.id.article_image);
        pageNum = itemView.findViewById(R.id.article_page);
        description = itemView.findViewById(R.id.article_description);
    }
}
