package com.example.newsaggregator;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Topic implements Comparable<Topic>, Serializable {

    private final String name;
    private Integer color;

    public Topic(String name) {
        this.name = name;
    }

    public void setColor(Integer color) { this.color = color; }
    public String getName() { return name; }
    public Integer getColor() { return color; }

    @Override
    public int compareTo(Topic topic) { return name.compareTo(topic.getName()); }

    @NonNull
    public String toString() { return name; }
}
