package com.example.mediaplayer;

public class MediaRepository {
    private static MediaRepository instance = new MediaRepository();

    private PlaylistManager videoPlaylistManager;
    private PlaylistManager audioPlaylistManager;

    private MediaRepository() {}

    public static MediaRepository getInstance() {
        if (instance == null) instance = new MediaRepository();
        return instance;
    }

    public void setVideoPlaylistManager(PlaylistManager manager) {
        this.videoPlaylistManager = manager;
    }

    public void setAudioPlaylistManager(PlaylistManager manager) {
        this.audioPlaylistManager = manager;
    }

    public PlaylistManager getVideoPlaylistManager() {
        return videoPlaylistManager;
    }

    public PlaylistManager getAudioPlaylistManager() {
        return audioPlaylistManager;
    }
}


