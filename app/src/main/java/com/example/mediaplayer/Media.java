package com.example.mediaplayer;

import android.graphics.Bitmap;
import android.widget.TextView;

public class Media {
    private String name;
    private String path;
    private String dateAdded;

    private String duration;
    private String resolution;
    private String frameRate;
    private String size;

    private String lastPlayedTime;
    private Bitmap thumbnailUrl;
    private boolean isVideo;

    public Media(String name, String path, String date, Bitmap thumbnailUrl, boolean isVideo) {
        this.name = name;
        this.path = path;
        this.dateAdded = date;
        this.thumbnailUrl = thumbnailUrl;
        this.isVideo = isVideo;
    }


    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public void setFrameRate(String fps) {
        this.frameRate = fps;
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

    public String getResolution() {
        return resolution;
    }

    public String getFrameRate() {
        return frameRate;
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