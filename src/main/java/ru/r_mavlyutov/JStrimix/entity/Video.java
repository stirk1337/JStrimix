package ru.r_mavlyutov.JStrimix.entity;

public class Video {
    private Long id;
    private String title;
    private String description;
    private int durationSec;
    private int views;

    public Video() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDuration() {
        return durationSec;
    }

    public void setDuration(int durationSec) {
        this.durationSec = durationSec;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }
}