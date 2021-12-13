package com.example.newsaggregator;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Language implements Comparable<Language>, Serializable {

    private final String code;
    private final String name;

    public Language(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() { return code; }
    public String getName() { return name; }

    @Override
    public int compareTo(Language language) { return name.compareTo(language.getName()); }

    @NonNull
    public String toString() { return name; }
}
