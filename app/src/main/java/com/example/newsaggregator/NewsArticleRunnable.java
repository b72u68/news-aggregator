package com.example.newsaggregator;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class NewsArticleRunnable implements Runnable{

    private final MainActivity mainActivity;
    private final String source;

    private static final String articleUrl = "https://newsapi.org/v2/top-headlines";
    private static final String apiKey = "87ac462d21e6498c8ff6761a9b857712";

    public NewsArticleRunnable(MainActivity mainActivity, String source) {
        this.mainActivity = mainActivity;
        this.source = source;
    }

    @Override
    public void run() {
        Uri.Builder buildURL = Uri.parse(articleUrl).buildUpon();
        buildURL.appendQueryParameter("sources", source);
        buildURL.appendQueryParameter("apiKey", apiKey);
        String urlToUse = buildURL.build().toString();
        StringBuilder sb = new StringBuilder();

        try {
            URL url = new URL(urlToUse);
            HttpURLConnection connection = (HttpURLConnection)  url.openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("User-Agent", "");
            connection.connect();

            if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                handleResults(null);
                return;
            }

            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            reader.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            handleResults(null);
            return;
        }

        handleResults(sb.toString());
    }

    public void handleResults(final String s) {
        if (s == null) {
            mainActivity.runOnUiThread(mainActivity::downloadNewsArticlesFailed);
            return;
        }

        ArrayList<NewsArticle> articles = parseNewsArticleJSONObject(s);

        if (articles == null) {
            mainActivity.runOnUiThread(mainActivity::downloadNewsArticlesFailed);
            return;
        }

        mainActivity.runOnUiThread(() -> mainActivity.handleNewsArticleResults(articles));
    }

    public ArrayList<NewsArticle> parseNewsArticleJSONObject(String s) {
        try {
            JSONObject jsonMain = new JSONObject(s);
            JSONArray articlesJSONArray =  jsonMain.getJSONArray("articles");

            ArrayList<NewsArticle> articles = new ArrayList<>();

            for (int i = 0; i < articlesJSONArray.length(); i++) {
                JSONObject articleObj = (JSONObject) articlesJSONArray.get(i);

                String author = null;
                if (articleObj.has("author") && !articleObj.getString("author").equals("null")) {
                    author = articleObj.getString("author");
                }

                String title = null;
                if (articleObj.has("title") && !articleObj.getString("title").equals("null")) {
                    title = articleObj.getString("title");
                }

                String description = null;
                if (articleObj.has("description") && !articleObj.getString("description").equals("null")) {
                    description = articleObj.getString("description");
                }

                String url = null;
                if (articleObj.has("url") && !(articleObj.getString("url").equals("null") || articleObj.getString("url").trim().isEmpty())) {
                    url = articleObj.getString("url");
                }

                String urlToImage = null;
                if (articleObj.has("urlToImage") && !(articleObj.getString("urlToImage").equals("null") || articleObj.getString("urlToImage").trim().isEmpty())) {
                    urlToImage = articleObj.getString("urlToImage");
                }

                String publishedAt = null;
                if (articleObj.has("publishedAt") && !articleObj.getString("publishedAt").equals("null")) {
                    publishedAt = articleObj.getString("publishedAt");
                }

                NewsArticle article = new NewsArticle(author, title, description, url, urlToImage, publishedAt);
                articles.add(article);
            }
            return articles;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}