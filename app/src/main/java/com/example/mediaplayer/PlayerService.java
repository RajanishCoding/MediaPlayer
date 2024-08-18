package com.example.mediaplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;

public class PlayerService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "media_playback_channel";
    private static ExoPlayer player;
    private MediaSessionCompat mediaSessionCompat;
    private String mediaName;
    private final IBinder binder = (IBinder) new LocalBinder();
    MediaSession mediaSession;
    private boolean buffering;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

//\        buffer_view = findViewById()

//        createNotificationChannel();
        mediaSessionCompat = new MediaSessionCompat(this, "MediaSessionTag");
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSessionCompat.setMediaButtonReceiver(null);
        mediaSessionCompat.setCallback(new MediaSessionCompat.Callback(){});

        updateMediaSessionMetadata();

//        handler.postDelayed(updateNotificationRunnable, 100); // Update every second
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaName = intent.getStringExtra("name");
        String mediaPath = intent.getStringExtra("path");

        if (player == null) {
            initializePlayer(mediaPath);
        }

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
                    handler.removeCallbacks(updateNotificationRunnable);
                    return START_STICKY;
            }
            updateNotification();
        }


//        startForeground(NOTIFICATION_ID, createNotification());
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(@Nullable Intent rootIntent) {
        if (!player.getPlayWhenReady()
                || player.getMediaItemCount() == 0
                || player.getPlaybackState() == Player.STATE_ENDED) {
            // Stop the service if not playing, continue playing in the background
            // otherwise.
            stopForeground(true);
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }

        if (mediaSessionCompat != null) {
            mediaSessionCompat.release();
        }

//        handler.removeCallbacks(updateNotificationRunnable);
    }

    private void initializePlayer(String mediaPath) {
        player = new ExoPlayer.Builder(this).build();
        mediaSession = new MediaSession.Builder(this, player).build();
        player.setMediaItem(MediaItem.fromUri(mediaPath));
        player.prepare();
        player.play();
        updateMediaSessionMetadata();

    }


    private void updateMediaSessionMetadata() {
        if (mediaSessionCompat != null && player != null) {
            long duration = player.getDuration();
            long position = player.getCurrentPosition();

            mediaSessionCompat.setMetadata(new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mediaName)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Artist Name")
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                    .build());
        }
    }


    private Notification createNotification() {
//        PendingIntent playPendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY);
//        PendingIntent pausePendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PAUSE);

        Intent playIntent = new Intent(this, PlayerService.class).setAction("ACTION_PLAY");
        PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent pauseIntent = new Intent(this, PlayerService.class).setAction("ACTION_PAUSE");
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(this, PlayerService.class).setAction("ACTION_STOP");
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 2, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.icon))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setContentTitle(mediaName)
                .setContentText("Playing")
//                .setCustomContentView(remoteViews)
                .setContentIntent(playPendingIntent) // This is optional if you have a specific action on clicking the notification

                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0)
                        .setMediaSession(mediaSessionCompat.getSessionToken()))

                .addAction(new NotificationCompat.Action.Builder(R.drawable.baseline_close_24, "Stop", stopPendingIntent).build())
                .addAction(new NotificationCompat.Action.Builder(R.drawable.baseline_skip_previous_24, "Previous", null).build())
                .addAction(new NotificationCompat.Action.Builder(player.isPlaying() ? R.drawable.baseline_pause_circle_outline_24 : R.drawable.baseline_play_circle_outline_24, player.isPlaying() ? "Pause" : "Play", player.isPlaying() ? pausePendingIntent : playPendingIntent).build())
                .addAction(new NotificationCompat.Action.Builder(R.drawable.baseline_skip_next_24, "Next", null).build())
                .addAction(new NotificationCompat.Action.Builder(R.drawable.baseline_close_24, "Stop", stopPendingIntent).build());

        builder.setProgress((int) player.getDuration(), (int) player.getCurrentPosition(), false);
        return builder.build();
    }

    private Handler handler = new Handler();
    private Runnable updateNotificationRunnable = new Runnable() {
        @Override
        public void run() {
            updateNotification();
            handler.postDelayed(this, 100); // Update every second
        }
    };

    private void updateNotification() {
        Notification notification = createNotification();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

//        Log.d("TAG", "updateNotification: " + player.getCurrentPosition() + "  " + player.getDuration());
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Media Playback";
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

    private class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            if (player != null) {
                player.play();
                updateNotification();
            }
        }

        @Override
        public void onPause() {
            if (player != null) {
                player.pause();
                updateNotification();
            }
        }

        @Override
        public void onStop() {
            if (player != null) {
                player.stop();
                stopForeground(true);
                stopSelf();
                handler.removeCallbacks(updateNotificationRunnable);
            }
        }

        @Override
        public void onSeekTo(long pos) {
            if (player != null) {
                player.seekTo(pos);
                updateNotification();
            }
        }
    }

    public static ExoPlayer getPlayer(){
        if (player != null){
            return player;
        }
        return null;
    }
}

