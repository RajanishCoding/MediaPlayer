package com.example.mediaplayer.Audio;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.mediaplayer.Video.Video;

import java.util.Objects;

@Entity (tableName = "audio")
public class Audio {
    
    @NonNull
    @PrimaryKey
    private String uri;
    
    private String name;
    private String path;
    private String dateAdded;

    private Long duration;
    private Long size;

//    private String lastPlayedTime;

    @Ignore
    private Bitmap thumbnailUrl;

    private boolean isVideo;

    private boolean isNewAudio = true;
    private boolean isLastPlayed;

    @Nullable
    private String extraMetadata;

    @Ignore
    public boolean isSelected;


    public Audio(String uri, String name, String path, String dateAdded, boolean isVideo) {
        this.uri = uri;
        this.name = name;
        this.path = path;
        this.dateAdded = dateAdded;
        this.isVideo = false;
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

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
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

    public boolean isNewAudio() {
        return isNewAudio;
    }

    public void setIsNewAudio(boolean isNew) {
        isNewAudio = isNew;
    }

    public boolean isLastPlayed() {
        return isLastPlayed;
    }

    public void setLastPlayed(boolean isLastPlayed) {
        this.isLastPlayed = isLastPlayed;
    }

    public String getExtraMetadata() {
        return extraMetadata;
    }

    public void setExtraMetadata(String extraMetadata) {
        this.extraMetadata = extraMetadata;
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
//                Objects.equals(lastPlayedTime, video.lastPlayedTime) &&
                isVideo == audio.isVideo &&
                isNewAudio == audio.isNewAudio &&
                isLastPlayed == audio.isLastPlayed;
    }
}