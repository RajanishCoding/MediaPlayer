package com.example.mediaplayer.Audio;

import android.graphics.Bitmap;

public class Audio {
    private String name;
    private String path;
    private String dateAdded;

    private String duration;
    private String size;

    private String lastPlayedTime;
    private Bitmap thumbnailUrl;
    private boolean isVideo;

    public Audio(String name, String path, String date, Bitmap thumbnailUrl, boolean isVideo) {
        this.name = name;
        this.path = path;
        this.dateAdded = date;
        this.thumbnailUrl = thumbnailUrl;
        this.isVideo = isVideo;
    }


    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setSize(String size) {
        this.size = size;
    }


    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public String getDuration() {
        return duration;
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