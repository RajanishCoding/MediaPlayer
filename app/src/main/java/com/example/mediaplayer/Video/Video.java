package com.example.mediaplayer.Video;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity
public class Video {

    @NonNull
    @PrimaryKey
    private String uri;
    
    private String name;
    private String path;
    private String dateAdded;

    private Long duration;
    private Integer resolution;
    private Double frameRate;
    private Long size;

//    private String lastPlayedTime;

    @Ignore
    private Bitmap thumbnailUrl;

    private boolean isVideo;
    
    @Ignore
    public boolean isSelected;


    public Video(String uri, String name, String path, String dateAdded,  boolean isVideo) {
        this.uri = uri;
        this.name = name;
        this.path = path;
        this.dateAdded = dateAdded;
        this.isVideo = true;
    }


    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Integer getResolution() {
        return resolution ;//!= null ? resolution : "0";
    }

    public void setResolution(Integer resolution) {
        this.resolution = resolution;
    }

    public Double getFrameRate() {
        Log.d("frameRate", "getFrameRate: " + frameRate);
        return frameRate;
    }

    public void setFrameRate(Double fps) {
        this.frameRate = fps;
    }

    public Long getSize() {
        return size;
    }
    
    public void setSize(Long size) {
        this.size = size;
    }

//    public String getLastPlayedTime() {
//        return lastPlayedTime;
//    }

    public void setLastPlayedTime(String lastPlayedTime) {
//        this.lastPlayedTime = lastPlayedTime;
    }
    
    public Bitmap getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(Bitmap thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public boolean isVideo() {
        return isVideo;
    }
    
    public void setVideo(boolean video) {
        isVideo = video;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Video video = (Video) obj;
        Log.d("kklwi", "equals:  " + isSelected + video.isSelected);

        return Objects.equals(uri, video.uri) &&
                Objects.equals(name, video.name) &&
                Objects.equals(path, video.path) &&
                Objects.equals(dateAdded, video.dateAdded) &&
                Objects.equals(duration, video.duration) &&
                Objects.equals(resolution, video.resolution) &&
                Objects.equals(frameRate, video.frameRate) &&
                Objects.equals(size, video.size) &&
                isSelected == video.isSelected &&
//                Objects.equals(lastPlayedTime, video.lastPlayedTime) &&
                isVideo == video.isVideo;
    }
}