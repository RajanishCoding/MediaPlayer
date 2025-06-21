package com.example.mediaplayer;

import android.net.Uri;

import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;

public class MyMediaItem {
    public String path;
    public String name;

    public MyMediaItem(String name, String path) {
        this.path = path;
        this.name = name;
    }

    public MediaItem toExoPlayerMediaItem() {
        return new MediaItem.Builder()
                .setUri(Uri.parse(path))
                .setMediaMetadata(
                        new MediaMetadata.Builder()
                                .setTitle(name)
                                .build()
                )
                .build();
    }
}
