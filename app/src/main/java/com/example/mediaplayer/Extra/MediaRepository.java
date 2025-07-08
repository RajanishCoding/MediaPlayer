package com.example.mediaplayer.Extra;

import androidx.media3.common.MediaItem;

import java.util.List;

public class MediaRepository {
    private static MediaRepository instance = new MediaRepository();

//    private PlaylistManager videoPlaylistManager;
//    private PlaylistManager audioPlaylistManager;

    private List<MediaItem> videoPlaylist;
    private List<MediaItem> audioPlaylist;

    private MediaRepository() {}

    public static MediaRepository getInstance() {
        if (instance == null) instance = new MediaRepository();
        return instance;
    }

//    public void setVideoPlaylistManager(PlaylistManager manager) {
//        this.videoPlaylistManager = manager;
//    }

//    public void setAudioPlaylistManager(PlaylistManager manager) {
//        this.audioPlaylistManager = manager;
//    }

//    public PlaylistManager getVideoPlaylistManager() {
//        return videoPlaylist;
//    }

//    public PlaylistManager getAudioPlaylistManager() {
//        return audioPlaylist;
//    }

    public void setVideoPlaylist(List<MediaItem> videoPlaylist) {
        this.videoPlaylist = videoPlaylist;
    }

    public void setAudioPlaylist(List<MediaItem> audioPlaylist) {
        this.audioPlaylist = audioPlaylist;
    }

    public List<MediaItem> getVideoPlaylist() {
        return videoPlaylist;
    }

    public List<MediaItem> getAudioPlaylist() {
        return audioPlaylist;
    }
}


