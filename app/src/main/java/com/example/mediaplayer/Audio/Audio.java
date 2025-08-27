package com.example.mediaplayer.Audio;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.mediaplayer.Video.Video;

import java.util.Objects;

@Entity (tableName = "audio")
public class Audio {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String uri;
    private String name;
    private String path;
    private String dateAdded;

    private String duration;
    private String size;

//    private String lastPlayedTime;

    @Ignore
    private Bitmap thumbnailUrl;

    private boolean isVideo;

    @Ignore
    public boolean isSelected;


    public Audio(String uri, String name, String path, String dateAdded, String duration, String size, boolean isVideo) {
        this.uri = uri;
        this.name = name;
        this.path = path;
        this.dateAdded = dateAdded;
        this.duration = duration;
        this.size = size;
        this.isVideo = false;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
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

    public void isSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Audio audio = (Audio) obj;

        return Objects.equals(uri, audio.uri) &&
                Objects.equals(name, audio.name) &&
                Objects.equals(path, audio.path) && 
                Objects.equals(dateAdded, audio.dateAdded) &&
                Objects.equals(duration, audio.duration) &&
                Objects.equals(size, audio.size) &&
                isVideo == audio.isVideo;
    }
}