package com.example.mediaplayer;

public class Media {
    private String name;
    private String path;
    private String duration;
    private String thumbnailUrl;

    public Media(String name, String path, String duration, String thumbnailUrl) {
        this.name = name;
        this.path = path;
        this.duration = duration;
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getDuration() {
        return duration;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}
