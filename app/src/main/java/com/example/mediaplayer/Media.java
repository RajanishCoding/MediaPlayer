package com.example.mediaplayer;

import android.graphics.Bitmap;

public class Media {
    private String name;
    private String path;
    private String size;
    private Bitmap thumbnailUrl;
    private boolean isVideo;

    public Media(String name, String path, String size, Bitmap thumbnailUrl, boolean isVideo) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.thumbnailUrl = thumbnailUrl;
        this.isVideo = isVideo;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getSize() {
        return size;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public Bitmap getThumbnail() {
        return thumbnailUrl;
    }

    public void setThumbnail(Bitmap thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}