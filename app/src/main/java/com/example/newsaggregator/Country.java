package com.example.newsaggregator;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Country implements Comparable<Country>, Serializable {

    private final String code;
    private final String name;

    public Country(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() { return code; }
    public String getName() { return name; }

    @Override
    public int compareTo(Country country) { return name.compareTo(country.getName()); }

    @NonNull
    public String toString() { return name; }
}
