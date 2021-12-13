package com.example.newsaggregator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Menu menu;

    private DrawerLayout menuDrawerLayout;
    private ListView menuDrawerList;
    private ActionBarDrawerToggle menuDrawerToggle;

    private final ArrayList<NewsSource> sources = new ArrayList<>();
    private final ArrayList<Country> countries = new ArrayList<>();
    private final ArrayList<Language> languages = new ArrayList<>();
    private final ArrayList<Topic> topics = new ArrayList<>();
    private final ArrayList<NewsArticle> articles = new ArrayList<>();

    private String selectedCountryCode = null;
    private String selectedLanguageCode = null;
    private String selectedTopic = null;

    private final HashMap<String, String> countryCodes = new HashMap<>();
    private final HashMap<String, String> languageCodes = new HashMap<>();

    private final ArrayList<NewsSource> currentSources = new ArrayList<>();

    private ArrayAdapter<NewsSource> sourcesAdapter;

    private NewsArticleAdapter newsArticlesAdapter;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadCountryCodes();
        loadLanguageCodes();

        setTitle("News Gateway");

        menuDrawerLayout = findViewById(R.id.news_source_drawer);
        menuDrawerList = findViewById(R.id.news_source_list);

        sourcesAdapter = new ArrayAdapter<NewsSource>(this, R.layout.news_source_item, currentSources) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(R.id.news_source_name);
                textView.setTextColor(currentSources.get(position).getColor());
                return view;
            }
        };

        menuDrawerList.setAdapter(sourcesAdapter);

        menuDrawerList.setOnItemClickListener(
                (parent, view, position, id) -> selectNewsSource(position)
        );

        menuDrawerToggle = new ActionBarDrawerToggle(
                this,
                menuDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        );

        newsArticlesAdapter = new NewsArticleAdapter(this, articles);
        viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(newsArticlesAdapter);

        new Thread(new NewsSourceRunnable(this, countryCodes, languageCodes)).start();
    }

    private void showDrawerToggleButton(boolean isVisible) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(isVisible);
            getSupportActionBar().setHomeButtonEnabled(isVisible);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void selectNewsSource(int position) {
        viewPager.setBackground(null);
        menuDrawerLayout.closeDrawer(menuDrawerList);

        this.articles.clear();
        newsArticlesAdapter.notifyDataSetChanged();

        NewsSource source = currentSources.get(position);
        setTitle(source.getName());

        findViewById(R.id.loading_image).setVisibility(View.VISIBLE);

        new Thread(new NewsArticleRunnable(this, source.getId())).start();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        menuDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        menuDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (menuDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (item.hasSubMenu())
            return true;

        int parentSubmenu = item.getGroupId();
        int menuItem = item.getItemId();

        switch (parentSubmenu) {
            case 0:
                if (menuItem == 0) selectedTopic = null;
                else selectedTopic = topics.get(menuItem-1).getName();
                break;
            case 1:
                if (menuItem == 0) selectedCountryCode = null;
                else selectedCountryCode = countries.get(menuItem-1).getCode();
                break;
            case 2:
                if (menuItem == 0) selectedLanguageCode = null;
                else selectedLanguageCode = languages.get(menuItem-1).getCode();
                break;
        }

        filterCurrentNewsSources(selectedCountryCode, selectedLanguageCode, selectedTopic);

        setTitle(String.format(Locale.ROOT, "News Gateway (%d)", currentSources.size()));

        return super.onOptionsItemSelected(item);
    }

    private void filterCurrentNewsSources(String countryCode, String languageCode, String topic) {
        ArrayList<NewsSource> tempSources = new ArrayList<>();
        for (int i = 0; i < sources.size(); i++) {
            NewsSource tempSource = sources.get(i);
            if (topic != null && !topic.equals(tempSource.getCategory())) {
                continue;
            }

            if (countryCode != null && !countryCode.equals(tempSource.getCountry())) {
                continue;
            }

            if (languageCode != null && !languageCode.equals(tempSource.getLanguage())) {
                continue;
            }

            tempSources.add(tempSource);
        }
        currentSources.clear();
        currentSources.addAll(tempSources);
        sourcesAdapter.notifyDataSetChanged();
    }

    private void makeMenu() {
        menu.clear();
        SubMenu topicsMenu = menu.addSubMenu(R.string.submenu_topics);
        SubMenu countriesMenu = menu.addSubMenu(R.string.submenu_countries);
        SubMenu languagesMenu = menu.addSubMenu(R.string.submenu_languages);

        topicsMenu.add(0, 0, 0, "all");
        for (int i = 0; i < topics.size(); i++) {
            Spannable s = new SpannableString(topics.get(i).getName());
            s.setSpan(new ForegroundColorSpan(topics.get(i).getColor()), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            topicsMenu.add(0, i+1, i+1, s);
        }

        countriesMenu.add(1, 0, 0, "all");
        for (int i = 0; i < countries.size(); i++) {
            countriesMenu.add(1, i+1, i+1, countries.get(i).getName());
        }

        languagesMenu.add(2, 0, 0, "all");
        for (int i = 0; i < languages.size(); i++) {
            languagesMenu.add(2, i+1, i+1, languages.get(i).getName());
        }
    }

    private void loadCountryCodes() {
        try {
            InputStream is = getResources().openRawResource(R.raw.country_codes);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JSONArray jsonArray = new JSONObject(sb.toString()).getJSONArray("countries");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String code = jsonObject.getString("code");
                String name = jsonObject.getString("name");

                countryCodes.put(code, name);
            }

            reader.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadLanguageCodes() {
        try {
            InputStream is = getResources().openRawResource(R.raw.language_codes);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JSONArray jsonArray = new JSONObject(sb.toString()).getJSONArray("languages");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String code = jsonObject.getString("code");
                String name = jsonObject.getString("name");

                languageCodes.put(code, name);
            }

            reader.close();
            is.close();
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "Cannot find Note.json file.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleNewsSourceResults(ArrayList<NewsSource> sources, ArrayList<Country> countries,
                                        ArrayList<Language> languages, ArrayList<Topic> topics) {

        if (sources.size() == 0 || countries.size() == 0 || languages.size() == 0 || topics.size() == 0) {
            downloadNewsSourcesFailed();
            return;
        }

        this.sources.clear();
        this.currentSources.clear();
        this.countries.clear();
        this.languages.clear();
        this.topics.clear();

        this.sources.addAll(sources);
        this.currentSources.addAll(sources);
        this.countries.addAll(countries);
        this.languages.addAll(languages);
        this.topics.addAll(topics);

        makeMenu();

        sourcesAdapter.notifyDataSetChanged();

        setTitle(String.format(Locale.ROOT, "News Gateway (%d)", currentSources.size()));
        showDrawerToggleButton(true);

        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }

    public void downloadNewsSourcesFailed() {
        showDrawerToggleButton(false);

        this.sources.clear();
        this.currentSources.clear();
        this.countries.clear();
        this.languages.clear();
        this.topics.clear();
        sourcesAdapter.notifyDataSetChanged();

        menu.clear();

        setTitle("News Gateway");

        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void handleNewsArticleResults(ArrayList<NewsArticle> articles) {
        this.articles.clear();
        this.articles.addAll(articles);
        newsArticlesAdapter.notifyDataSetChanged();
        viewPager.setCurrentItem(0);

        findViewById(R.id.loading_image).setVisibility(View.GONE);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void downloadNewsArticlesFailed() {
        this.articles.clear();
        newsArticlesAdapter.notifyDataSetChanged();
        findViewById(R.id.loading_image).setVisibility(View.GONE);
    }
}