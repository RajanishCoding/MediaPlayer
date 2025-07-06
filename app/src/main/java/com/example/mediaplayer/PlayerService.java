package com.example.mediaplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.session.CommandButton;
import androidx.media3.session.MediaNotification;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;
import androidx.media3.session.SessionCommand;
import androidx.media3.session.SessionResult;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@UnstableApi
public class PlayerService extends MediaSessionService {

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "media_playback_channel";

    private static ExoPlayer player;
    private String mediaName;
    private final IBinder binder = (IBinder) new LocalBinder();
    MediaSession mediaSession;

    private boolean isForeground;

    private boolean isInitialized;
    private String currentMediaPath;

    private static final String ACTION_LOOP_MODE = "ACTION_LOOP_MODE";
    private static final String ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE";
    private static final String ACTION_NEXT = "ACTION_NEXT";
    private static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    private static final String ACTION_SHUFFLE = "ACTION_SHUFFLE";


    private PlayerActivity playerActivity;

    private static final String TAG = "tag_service";


    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);

        player = new ExoPlayer.Builder(this)
                .setTrackSelector(new DefaultTrackSelector(this))
                .build();

        mediaSession = new MediaSession.Builder(this, player).build();

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Log.d(TAG, "Player State Changed: " + playbackState);
                updateNotification();
            }

            @Override
            public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                Log.d(TAG, "PlayWhenReady Changed: " + playWhenReady);
                updateNotification();
            }

            @Override
            public void onMediaMetadataChanged(@NonNull MediaMetadata mediaMetadata) {
                Log.d(TAG, "MediaMetadata Changed: " + mediaMetadata.title);
                updateNotification(); // Update when track info changes
            }

            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                updateNotification();
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                Log.d(TAG, "IsPlaying Changed: " + isPlaying);
                updateNotification();
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(bgPlayReceiver, new IntentFilter("BG_PLAY_STATUS"));
    }


    @Nullable
    @Override
    public MediaSession onGetSession(@NonNull MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        mediaName = intent.getStringExtra("name");
        String mediaPath = intent.getStringExtra("path");

        Log.d(TAG, "onStartCommand: YES");

        if (mediaPath != null) {
            Log.d(TAG, "onStartCommandI: " + player + "  " + currentMediaPath + "  " + mediaPath);
            currentMediaPath = mediaPath;
        }

        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case ACTION_PLAY_PAUSE:
                    if (player.isPlaying()) {
                        player.pause();
                    } else {
                        player.play();
                    }
                    break;
                case ACTION_NEXT:
                    player.seekToNextMediaItem();
                    break;
                case ACTION_PREVIOUS:
                    player.seekToPreviousMediaItem();
                    break;
                case ACTION_SHUFFLE:
                    player.setShuffleModeEnabled(!player.getShuffleModeEnabled());
                    break;
            }
        }

        return START_STICKY;
    }


    @Override
    public void onTaskRemoved(@Nullable Intent rootIntent) {
        if (!player.getPlayWhenReady()
                || player.getMediaItemCount() == 0
                || player.getPlaybackState() == Player.STATE_ENDED) {

            if (player != null) {
                player.release();
            }

            if (mediaSession != null) {
                mediaSession.release();
                mediaSession = null;
            }

            stopForegroundNotification();
            stopSelf();
        }
    }


    private void initializePlayer(String mediaPath) {
        isInitialized = true;
        Log.d(TAG, "initializePlayerS: YES");

    }


    private Notification createNotification() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        boolean isPlaying = player.isPlaying();
        int loopValue = player.getRepeatMode();
        boolean isShuffling = player.getShuffleModeEnabled();

        // Getting current media metadata for notification content
        MediaMetadata mediaMetadata = player.getMediaMetadata();
        String title = mediaMetadata.title != null ? mediaMetadata.title.toString() : "Unknown Title";
        String artist = mediaMetadata.artist != null ? mediaMetadata.artist.toString() : "Unknown Artist";
        Bitmap albumArt = null;

        Log.d("servicePLay", "createNotification: " + title + " , " + artist);

        // Load album art (replace with your actual asynchronous loading logic)
        // For simplicity in this example, we'll use a static drawable icon.
        albumArt = BitmapFactory.decodeResource(getResources(), R.drawable.icon1); // Your default/placeholder icon

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon1)
                .setLargeIcon(albumArt)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setContentTitle(title)
                .setContentText(artist)
                .setContentIntent(createContentPendingIntent()); // This is optional if you have a specific action on clicking the notification

        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(MediaSessionCompat.Token.fromToken(mediaSession.getPlatformToken()))
                .setShowActionsInCompactView(0, 1, 2));

        builder.addAction(
                (loopValue == 0) ? generateAction(androidx.media3.session.R.drawable.media3_icon_repeat_off, "Shuffle Off", ACTION_LOOP_MODE) :
                        (loopValue == 1)  ? generateAction(androidx.media3.session.R.drawable.media3_icon_repeat_one, "Shuffle One", ACTION_LOOP_MODE) :
                                generateAction(androidx.media3.session.R.drawable.media3_icon_repeat_all, "Shuffle All", ACTION_LOOP_MODE)
        );
        builder.addAction(generateAction(R.drawable.baseline_skip_previous_24, "Previous", ACTION_PREVIOUS));
        builder.addAction(generateAction(isPlaying ? R.drawable.baseline_pause_24 : R.drawable.baseline_play_arrow_24, isPlaying ? "Pause" : "Play", ACTION_PLAY_PAUSE));
        builder.addAction(generateAction(R.drawable.baseline_skip_next_24, "Next", ACTION_NEXT));
        builder.addAction(
                (isShuffling) ? generateAction(androidx.media3.session.R.drawable.media3_icon_shuffle_on, "Shuffle Enable", ACTION_SHUFFLE) :
                        generateAction(androidx.media3.session.R.drawable.media3_icon_shuffle_off, "Shuffle Disable", ACTION_SHUFFLE));

//        builder.setProgress((int) player.getDuration(), (int) player.getCurrentPosition(), false);
        return builder.build();
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(this, PlayerService.class);
        intent.setAction(intentAction);
        return new NotificationCompat.Action.Builder(icon, title,
                PendingIntent.getService(this, intentAction.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT))
                .build();
    }

    private void updateNotification() {
        if (!isForeground || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Notification notification = createNotification();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void startForegroundNotification() {
        if (mediaSession == null) {
            mediaSession = new MediaSession.Builder(this, player).build();
        }

        Notification notification = createNotification();

        if (notification == null) {
            stopForegroundNotification();
            Log.w(TAG, "Notification could not be created, stopping foreground if active.");
            return;
        }

        isForeground = true;
        startForeground(NOTIFICATION_ID, notification);
    }

    private void stopForegroundNotification() {
        isForeground = false;
        stopForeground(Service.STOP_FOREGROUND_REMOVE);

        if (mediaSession != null) {
            mediaSession.release();
            mediaSession = null;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Video Playback";
            String description = "Channel for media playback notifications";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }


    public class LocalBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return binder;
    }


    private final BroadcastReceiver bgPlayReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isBGPlay = intent.getBooleanExtra("isBGPlay", false);
            Log.d("isBGPLAY", "onReceive: " + isBGPlay);
            if (isBGPlay) startForegroundNotification();
            else stopForegroundNotification();
        }
    };


    private PendingIntent createContentPendingIntent() {
        Intent contentIntent = new Intent(this, PlayerActivity.class);
        contentIntent.setAction(Intent.ACTION_MAIN);
        contentIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        // FLAG_IMMUTABLE is crucial for Android 12+
        return PendingIntent.getActivity(this, 0, contentIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }

        if (mediaSession != null) {
            mediaSession.release();
            mediaSession = null;
        }

        stopForegroundNotification();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bgPlayReceiver);
    }

    public void stopService() {
        stopSelf();
    }

    public static ExoPlayer getPlayer(){
        if (player != null){
            return player;
        }
        return null;
    }


}