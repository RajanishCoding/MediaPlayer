package com.example.mediaplayer.Video;

import android.graphics.Bitmap;
import android.util.Log;

public class Video {
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
    public boolean isSelected;

    public Video(String name, String path, String date, Bitmap thumbnailUrl, boolean isVideo) {
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
        return duration != null ? duration : "0";
    }

    public String getResolution() {
        return resolution ;//!= null ? resolution : "0";
    }

    public String getFrameRate() {
        Log.d("frameRate", "getFrameRate: " + frameRate);
        return frameRate != null ? String.format("%.2f", Double.parseDouble(frameRate)): "0";
    }

    public String getSize() {
        return size != null ? size : "0";
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean getIsSelected() {
        return isSelected;
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