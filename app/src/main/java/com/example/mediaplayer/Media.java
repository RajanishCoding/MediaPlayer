package com.example.mediaplayer;

import android.graphics.Bitmap;
import android.widget.ImageView;

public class Media {
    private String name;
    private String path;
    private String duration;
    private Bitmap thumbnailUrl;

    public Media(String name, String path, String duration, Bitmap thumbnailUrl) {
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

    public Bitmap getThumbnail() {
        return thumbnailUrl;
    }

    public void setThumbnail(Bitmap thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}