package com.example.qtrobot;

public class CardModel {
    public static final int TYPE_IMAGE = 0;
    public static final int TYPE_VIDEO = 1;

    private String title;
    private String description;
    private int imageResId;
    private int type;

    // Standard image card
    public CardModel(String title, String description, int imageResId) {
        this.title = title;
        this.description = description;
        this.imageResId = imageResId;
        this.type = TYPE_IMAGE;
    }

    // Video placeholder card
    public CardModel(String title, String description) {
        this.title = title;
        this.description = description;
        this.imageResId = 0;
        this.type = TYPE_VIDEO;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getImageResId() { return imageResId; }
    public int getType() { return type; }
}
