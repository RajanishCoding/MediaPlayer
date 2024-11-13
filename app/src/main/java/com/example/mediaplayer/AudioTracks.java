package com.example.mediaplayer;

import androidx.media3.common.TrackGroup;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;

public class AudioTracks {
    private TrackGroup trackGroup;
    private int trackIndex;
    private String label;
    private String language;
    private int channels;
    private boolean isSelected;

    public AudioTracks(TrackGroup trackGroup, int trackIndex, String label, String language, int channels, boolean isSelected) {
        this.trackGroup = trackGroup;
        this.trackIndex = trackIndex;
        this.label = label;
        this.language = language;
        this.channels = channels;
        this.isSelected = isSelected;
    }

    public TrackGroup getTrackGroup() {
        return trackGroup;
    }

    public int getTrackIndex() {
        return trackIndex;
    }

    public String getLabel() {
        return label;
    }

    public String getLanguage() {
        return language;
    }

    public int getChannels() {
        return channels;
    }

    public boolean isSelected() {
        return isSelected;
    }
}