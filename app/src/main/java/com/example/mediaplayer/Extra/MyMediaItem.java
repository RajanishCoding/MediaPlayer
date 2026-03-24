package com.example.mediaplayer.Extra;

import android.net.Uri;
import android.os.Bundle;

import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;

public class MyMediaItem {
    public String path;
    public String name;
    public String uri;
    public boolean isVideo;

    public MyMediaItem(String name, String uri, String path, boolean isVideo) {
        this.name = name;
        this.uri = uri;
        this.path = path;
        this.isVideo = isVideo;
    }

    public MediaItem toExoPlayerMediaItem() {
        Bundle extras = new Bundle();
        extras.putString("path", path);
//        extras.putBoolean("isVideo", isVideo);

        return new MediaItem.Builder()
                .setUri(Uri.parse(uri))
                .setMediaMetadata(
                        new MediaMetadata.Builder()
                                .setTitle(name)
                                .setExtras(extras)
                                .build()
                )
                .build();
    }
}
