package com.example.mediaplayer;

import androidx.media3.common.MediaItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaylistManager {
    private List<MediaItem> originalList;
    private List<MediaItem> currentList; // Shuffled list
    private int currentIndex = 0;
    private boolean isShuffling = false;
    private boolean isLooping = false;

    public PlaylistManager(List<MediaItem> mediaItems) {
        this.originalList = new ArrayList<>(mediaItems);
        this.currentList = new ArrayList<>(mediaItems);
    }

    public MediaItem getCurrentItem() {
        return currentList.get(currentIndex);
    }

    public void next() {
        if (isLooping) return; // Stay on current
        currentIndex = (currentIndex + 1) % currentList.size();
    }

    public void previous() {
        currentIndex = (currentIndex - 1 + currentList.size()) % currentList.size();
    }

    public void setShuffling(boolean shuffle) {
        this.isShuffling = shuffle;
        if (shuffle) {
            Collections.shuffle(currentList);
        } else {
            currentList = new ArrayList<>(originalList);
        }
        currentIndex = 0; // Reset index
    }

    public void setLooping(boolean loop) {
        this.isLooping = loop;
    }

    public List<MediaItem> getCurrentList() {
        return currentList;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int index) {
        currentIndex = index;
    }
}

