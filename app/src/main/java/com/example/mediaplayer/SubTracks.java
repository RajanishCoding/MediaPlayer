package com.example.mediaplayer;

import androidx.media3.common.TrackGroup;

public class SubTracks {
    private TrackGroup trackGroup;
    private int trackIndex;
    private String label;
    private String language;
    private boolean isSelected;

    public SubTracks(TrackGroup trackGroup, int trackIndex, String label, String language, boolean isSelected) {
        this.trackGroup = trackGroup;
        this.trackIndex = trackIndex;
        this.label = label;
        this.language = language;
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

    public boolean isSelected() {
        return isSelected;
    }
}