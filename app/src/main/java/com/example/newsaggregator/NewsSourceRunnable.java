package com.example.newsaggregator;

import android.graphics.Color;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

public class NewsSourceRunnable implements Runnable {

    private final MainActivity mainActivity;
    private final HashMap<String, String> countryCodes;
    private final HashMap<String, String> languageCodes;

    private static final String sourceURL = "https://newsapi.org/v2/sources";
    private static final String apiKey = "87ac462d21e6498c8ff6761a9b857712";

    private final ArrayList<NewsSource> sources = new ArrayList<>();
    private final ArrayList<Country> countries = new ArrayList<>();
    private final ArrayList<Language> languages = new ArrayList<>();
    private final ArrayList<Topic> topics = new ArrayList<>();

    public NewsSourceRunnable(MainActivity mainActivity, HashMap<String, String> countryCodes,
                              HashMap<String, String> languageCodes) {
        this.mainActivity = mainActivity;
        this.countryCodes = countryCodes;
        this.languageCodes = languageCodes;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        Uri.Builder buildURL = Uri.parse(sourceURL).buildUpon();
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void handleResults(final String s) {
        if (s == null) {
            mainActivity.runOnUiThread(mainActivity::downloadNewsSourcesFailed);
            return;
        }

        parseNewsSourceJSONObject(s);

        if (sources.size() == 0 || countries.size() == 0 || languages.size() == 0 || topics.size() == 0) {
            mainActivity.runOnUiThread(mainActivity::downloadNewsSourcesFailed);
            return;
        }

        Collections.sort(countries);
        Collections.sort(languages);
        Collections.sort(topics);

        ArrayList<Integer> colors = getColorList(topics.size());
        HashMap<String, Integer> topicAndColor = new HashMap<>();

        for (int i = 0; i < topics.size(); i++) {
            topicAndColor.put(topics.get(i).getName(), colors.get(i));
            topics.get(i).setColor(colors.get(i));
        }

        for (int i = 0; i < sources.size(); i++) {
            Integer color = topicAndColor.get(sources.get(i).getCategory());
            sources.get(i).setColor(color);
        }

        mainActivity.runOnUiThread(() -> mainActivity.handleNewsSourceResults(sources, countries, languages, topics));
    }

    public void parseNewsSourceJSONObject(String s) {
        Set<String> seenCountries = new HashSet<>();
        Set<String> seenLanguages = new HashSet<>();
        Set<String> seenTopics = new HashSet<>();

        try {
            JSONObject jsonMain = new JSONObject(s);
            JSONArray sourcesJSONArray =  jsonMain.getJSONArray("sources");

            for (int i = 0; i < sourcesJSONArray.length(); i++) {
                JSONObject sourceObj = (JSONObject) sourcesJSONArray.get(i);
                String id = sourceObj.getString("id");
                String name = sourceObj.getString("name");
                String category = sourceObj.getString("category");
                String language = sourceObj.getString("language");
                String country = sourceObj.getString("country");

                NewsSource source = new NewsSource(id, name, category, language, country);
                sources.add(source);

                String countryName = countryCodes.get(country.toUpperCase(Locale.ROOT));
                String languageName = languageCodes.get(language.toUpperCase(Locale.ROOT));

                if (!seenCountries.contains(countryName)) {
                    countries.add(new Country(country, countryName));
                    seenCountries.add(countryName);
                }

                if (!seenLanguages.contains(languageName)) {
                    languages.add(new Language(language, languageName));
                    seenLanguages.add(languageName);
                }

                if (!seenTopics.contains(category)) {
                    topics.add(new Topic(category));
                    seenTopics.add(category);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private ArrayList<Integer> getColorList(int numberOfColors) {
        ArrayList<Integer> colors = new ArrayList<>();
        float colorCounter = 0;
        for (int i = 0; i < 360; i += (int) 360 / numberOfColors) {
            float hue = (float) i;
            float saturation = 90 + (colorCounter % 10 + 1);
            float value = 50 +  (colorCounter % 2) * 50;
            colors.add(Color.HSVToColor(new float[]{hue, saturation, value}));
            colorCounter++;
        }
        return colors;
    }
}