package com.example.mediaplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;
//import androidx.media.session.MediaSessionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

public class PlayerService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "media_playback_channel";
    private static ExoPlayer player;
    private final IBinder binder = new LocalBinder();
    private String media_name;
    private String media_path;

    private MediaSessionCompat mediaSession;

    public static ExoPlayer getPlayer() {
        if (player != null) {
            return player;
        }
        return null;
    }

    public class LocalBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        mediaSession = new MediaSessionCompat(this, "MediaSessionTag");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setMediaButtonReceiver(null);
        updateMediaSessionMetadata();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        media_name = intent.getStringExtra("name");
        media_path = intent.getStringExtra("path");

        if (player == null) {
            initializePlayer();
        }

        if (player != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case "ACTION_PLAY":
                        player.play();
                        break;
                    case "ACTION_PAUSE":
                        player.pause();
                        break;
                    case "ACTION_STOP":
                        player.stop();
                        stopForeground(true);
                        stopSelf();
                        return START_NOT_STICKY;
                }
                updateNotification();
            }
            startForeground(NOTIFICATION_ID, createNotification());
        }
        return START_NOT_STICKY;
    }

    private void initializePlayer() {
        player = new ExoPlayer.Builder(this).build();
        player.setMediaItem(MediaItem.fromUri(media_path));
        player.prepare();
        player.play();
        updateMediaSessionMetadata();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }

        if (mediaSession != null) {
            mediaSession.release();
        }
    }

    private void updateMediaSessionMetadata() {
        if (mediaSession != null && player != null) {
            mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, media_name)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Artist Name") // You can update artist info as needed
                    .build());
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, PlayerActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent playIntent = new Intent(this, PlayerService.class).setAction("ACTION_PLAY");
        PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent pauseIntent = new Intent(this, PlayerService.class).setAction("ACTION_PAUSE");
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(this, PlayerService.class).setAction("ACTION_STOP");
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 2, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(media_name)
                .setContentText("Playing")
                .setSmallIcon(R.drawable.icon) // Ensure this is a valid drawable
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setContentIntent(contentPendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(new NotificationCompat.Action.Builder(R.drawable.baseline_play_arrow_24, "Play", playPendingIntent).build())
                .addAction(new NotificationCompat.Action.Builder(R.drawable.baseline_pause_24, "Pause", pausePendingIntent).build())
                .addAction(new NotificationCompat.Action.Builder(R.drawable.icon, "Stop", stopPendingIntent).build())
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2) // Show play, pause, and stop in compact view
                        .setMediaSession(mediaSession.getSessionToken()));

        return builder.build();
    }

    private void updateNotification() {
        Notification notification = createNotification();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = "Media Player";
            String description = "Media Playback Notifications";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
