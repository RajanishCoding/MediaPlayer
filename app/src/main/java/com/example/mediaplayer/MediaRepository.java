package com.example.mediaplayer;

public class MediaRepository {
    private static final MediaRepository instance = new MediaRepository();

    private PlaylistManager playlistManager;

    private MediaRepository() {}

    public static MediaRepository getInstance() {
        return instance;
    }

    public void setPlaylistManager(PlaylistManager manager) {
        this.playlistManager = manager;
    }

    public PlaylistManager getPlaylistManager() {
        return playlistManager;
    }
}


