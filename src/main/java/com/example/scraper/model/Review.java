package com.example.scraper.model;

public class Review {
    private String text;

    public Review() {}

    public Review(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
