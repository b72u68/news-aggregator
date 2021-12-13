package com.example.newsaggregator;

import androidx.annotation.NonNull;

public class NewsSource {

    private final String id;
    private final String name;
    private final String category;
    private final String language;
    private final String country;
    private Integer color;

    public NewsSource(String id, String name, String category, String language, String country) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.language = language;
        this.country = country;
    }

    public void setColor(Integer color) { this.color = color; }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getLanguage() { return language; }
    public String getCountry() { return country; }
    public Integer getColor() { return color; }

    @NonNull
    public String toString() { return name; }
}