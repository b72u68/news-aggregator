package com.example.newsaggregator;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class NewsArticleAdapter extends RecyclerView.Adapter<NewsArticleViewHolder> {

    private final MainActivity mainActivity;
    private final ArrayList<NewsArticle> articles;

    public NewsArticleAdapter(MainActivity mainActivity, ArrayList<NewsArticle> articles) {
        this.mainActivity = mainActivity;
        this.articles = articles;
    }

    @NonNull
    @Override
    public NewsArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NewsArticleViewHolder(
                LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_article_entry, parent, false)
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull NewsArticleViewHolder holder, int position) {
        Picasso picasso = Picasso.get();
        NewsArticle article = articles.get(position);

        if (article.getTitle().isEmpty()) {
            holder.title.setVisibility(View.GONE);
        } else {
            holder.title.setVisibility(View.VISIBLE);
            holder.title.setText(article.getTitle());
        }

        if (article.getPublishedAt().isEmpty()) {
            holder.publishedAt.setVisibility(View.GONE);
        } else {
            holder.publishedAt.setVisibility(View.VISIBLE);
            holder.publishedAt.setText(formatDateTime(article.getPublishedAt()));
        }

        if (article.getAuthor().isEmpty()) {
            holder.author.setVisibility(View.GONE);
        } else {
            holder.author.setVisibility(View.VISIBLE);
            holder.author.setText(article.getAuthor());
        }

        if (article.getDescription().isEmpty()) {
            holder.description.setVisibility(View.GONE);
        } else {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(article.getDescription());
        }

        holder.pageNum.setText(String.format(Locale.ROOT, "%d of %d", position+1, articles.size()));

        if (article.getUrlToImage() != null) {
            picasso.load(article.getUrlToImage())
                    .error(R.drawable.brokenimage)
                    .placeholder(R.drawable.noimage)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.noimage);
        }

        holder.title.setOnClickListener(v -> onClickURL(article.getUrl()));
        holder.image.setOnClickListener(v -> onClickURL(article.getUrl()));
        holder.description.setOnClickListener(v -> onClickURL(article.getUrl()));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat")
    private String formatDateTime(String datetime) {
        DateTimeFormatter dtf;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");

        String baseDateTime = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d*)?Z$";

        if (datetime.matches(baseDateTime)) {
            dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSSSSSS]'Z'");
        } else {
            dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXXXX");
        }

        try {
            LocalDateTime localDateTime = LocalDateTime.parse(datetime, dtf);
            return localDateTime.format(formatter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return LocalDateTime.now().format(formatter);
    }

    @Override
    public int getItemCount() { return articles.size(); }

    public void onClickURL(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        mainActivity.startActivity(intent);
    }
}
