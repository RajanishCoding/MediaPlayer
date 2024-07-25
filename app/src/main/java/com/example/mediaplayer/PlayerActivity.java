package com.example.mediaplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

public class PlayerActivity extends AppCompatActivity {
    private static final String TAG = "tag";
    private static ExoPlayer player;  // Make the player instance static

    private TextView time1;
    private TextView time2;
    private SeekBar seekbar;
    private Handler handler;
    private Runnable updateSeekBar;

    private TextView ToolbarText;

    private ImageButton playButton;
    private ImageButton backButton;
    private ImageButton rotateButton;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Handler handler_surface;
    private Runnable runnable_surface;

    private Toolbar toolbar;
    private ConstraintLayout constraintLayout;
    private ImageButton fitcropButton;
    private boolean isFitScreen = true;

    private boolean f = false;
    private boolean surface_click_frag = false;

    private String media_name;
    private String media_path;

    private PlayerService playerService;
    private boolean isBound = false;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            playerService = binder.getService();
            player = PlayerService.getPlayer();
            if (player != null) {
                initializePlayer();
            }
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Set status bar color
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.toolbar_background_dark));
        getWindow().getDecorView().setSystemUiVisibility(0);

        playButton = findViewById(R.id.play);

        time1 = findViewById(R.id.time1);
        time2 = findViewById(R.id.time2);
        seekbar = findViewById(R.id.seekbar);

        ToolbarText = findViewById(R.id.toolbar_title);
        backButton = findViewById(R.id.back_button);
        rotateButton = findViewById(R.id.rotate);

        surfaceView = findViewById(R.id.surface_view);
        toolbar = findViewById(R.id.toolbar);
        constraintLayout = findViewById(R.id.full_container);

        fitcropButton = findViewById(R.id.fit_crop);


        Intent intent = getIntent();
        media_name = intent.getStringExtra("Name");
        media_path = intent.getStringExtra("Path");


        if (player == null){
            Intent serviceIntent = new Intent(this, PlayerService.class);
            serviceIntent.putExtra("name", media_name);
            serviceIntent.putExtra("path", media_path);
            bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
            startService(serviceIntent);
        }
        else {
            // Player already initialized -- When Rotated
            player.setVideoSurfaceHolder(surfaceHolder);
            updateUI();
        }


        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                Log.d(TAG, "surfaceCreated");
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                player.setVideoSurfaceHolder(null);
            }
        });


        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player != null) {
                    if (!player.isPlaying()) {
                        player.play();
                        playButton.setImageResource(R.drawable.baseline_pause_circle_outline_24);
                    } else {
                        player.pause();
                        playButton.setImageResource(R.drawable.baseline_play_circle_outline_24);
                    }
                }
            }
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                time1.setText(MillisToTime(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                f = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                player.seekTo(seekBar.getProgress());
                f = false;
            }
        });

        handler = new Handler();
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (player != null) {
                    if (player.isPlaying() && !f) {
                        seekbar.setProgress((int) player.getCurrentPosition());
                    }

                    if (player.isPlaying()){
                        playButton.setImageResource(R.drawable.baseline_pause_circle_outline_24);
                    }
                    else {
                        playButton.setImageResource(R.drawable.baseline_play_circle_outline_24);
                    }

                    // Execute your code here, e.g., setting a surface to the player
                    if (player != null && surfaceView.getHolder().getSurface().isValid()) {
                        player.setVideoSurfaceHolder(surfaceHolder);
                    }
                }
                handler.postDelayed(this, 50);
            }
        };
        handler.postDelayed(updateSeekBar, 50);

        backButton.setOnClickListener(v -> NavUtils.navigateUpFromSameTask(PlayerActivity.this));

        rotateButton.setOnClickListener(v -> {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                Log.d(TAG, "onRotate: " + "Landscape");
            }
            else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                Log.d(TAG, "onRotate: " + "Portrait");
            }
        });

        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!surface_click_frag){
                    Animation slideOutBottom = AnimationUtils.loadAnimation(PlayerActivity.this, R.anim.slide_out_bottom);
                    Animation slideOutTop = AnimationUtils.loadAnimation(PlayerActivity.this, R.anim.slide_out_top);
                    constraintLayout.startAnimation(slideOutBottom);
                    toolbar.startAnimation(slideOutTop);

                    slideOutBottom.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            toolbar.setVisibility(View.GONE);
                            constraintLayout.setVisibility(View.GONE);
                            surface_click_frag = true;
                            Log.d("Top", "onAnimationEnd: Yes");
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }

                else {
                    Animation slideInBottom = AnimationUtils.loadAnimation(PlayerActivity.this, R.anim.slide_in_bottom);
                    Animation slideInTop = AnimationUtils.loadAnimation(PlayerActivity.this, R.anim.slide_in_top);
                    constraintLayout.startAnimation(slideInBottom);
                    toolbar.startAnimation(slideInTop);

                    slideInBottom.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            toolbar.setVisibility(View.VISIBLE);
                            constraintLayout.setVisibility(View.VISIBLE);
                            surface_click_frag = false;
                            Log.d("Bottom", "onAnimationStart: Yes");
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
            }
        });

        // Fit to Screen and Crop to Screen Button Setting
        fitcropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adjustAspectRatio();
            }
        });
    }


    // Initializing Player
    private void initializePlayer() {
        Log.d("TAG", "initializePlayer: "+ player);
        ToolbarText.setText(media_name);
        playButton.setImageResource(R.drawable.baseline_pause_circle_outline_24);
        player.setVideoSurfaceHolder(surfaceHolder);
        fitToScreen(player.getVideoSize().width, player.getVideoSize().height, surfaceView.getWidth(), surfaceView.getHeight());

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    seekbar.setMax((int) player.getDuration());
                    time1.setText(MillisToTime(player.getCurrentPosition()));
                    time2.setText(MillisToTime(player.getDuration()));
                }

                if (playbackState == Player.STATE_ENDED) {
                    player.pause();
                    playButton.setImageResource(R.drawable.baseline_play_circle_outline_24);
                    seekbar.setProgress(0);
                    player.seekTo(0);
                }
            }
        });
    }

    private void updateUI() {
        ToolbarText.setText(media_name);
        seekbar.setMax((int) player.getDuration());
        seekbar.setProgress((int) player.getCurrentPosition());
        time1.setText(MillisToTime(player.getCurrentPosition()));
        time2.setText(MillisToTime(player.getDuration()));

        if (player.isPlaying()) {
            playButton.setImageResource(R.drawable.baseline_pause_circle_outline_24);
        }
        else {
            playButton.setImageResource(R.drawable.baseline_play_circle_outline_24);
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateSeekBar);
        if (isFinishing()) {
            if (player != null) {
                player.release();  // Ensure the player is released to avoid memory leaks
                player = null;  // Set the static player to null
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Handle configuration changes here
        updateUI();
    }

    public void adjustAspectRatio(){
        int videoWidth = player.getVideoSize().width;
        int videoHeight = player.getVideoSize().height;
        int viewWidth = surfaceView.getWidth();
        int viewHeight = surfaceView.getHeight();

        if (!isFitScreen) {
            fitcropButton.setImageResource(R.drawable.baseline_fit_screen_24);
            fitToScreen(videoWidth, videoHeight, viewWidth, viewHeight);
            isFitScreen = true;
        }
        else {
            fitcropButton.setImageResource(R.drawable.baseline_crop_din_24);
            cropToScreen(videoWidth, videoHeight, viewWidth, viewHeight);
            isFitScreen = false;
        }
    }

    public void fitToScreen(int videoWidth, int videoHeight, int viewWidth, int viewHeight) {
        double aspectRatio = (double) videoWidth / videoHeight;
        int newWidth, newHeight;

        if (viewWidth / aspectRatio <= viewHeight) {
            // Fit by width
            newWidth = viewWidth;
            newHeight = (int) (viewWidth / aspectRatio);
        } else {
            // Fit by height
            newWidth = (int) (viewHeight * aspectRatio);
            newHeight = viewHeight;
        }

        ViewGroup.LayoutParams params = surfaceView.getLayoutParams();
        params.width = newWidth;
        params.height = newHeight;
        surfaceView.setLayoutParams(params);
        surfaceView.requestLayout(); // Ensure layout is updated
    }


    public void cropToScreen(int videoWidth, int videoHeight, int viewWidth, int viewHeight) {
        double aspectRatio = (double) videoWidth / videoHeight;
        int newWidth, newHeight;

        if (viewWidth / aspectRatio >= viewHeight) {
            // Crop by width
            newWidth = (int) (viewHeight * aspectRatio);
            newHeight = viewHeight;
        } else {
            // Crop by height
            newWidth = viewWidth;
            newHeight = (int) (viewWidth / aspectRatio);
        }

        ViewGroup.LayoutParams params = surfaceView.getLayoutParams();
        params.width = newWidth;
        params.height = newHeight;
        surfaceView.setLayoutParams(params);
        surfaceView.requestLayout(); // Ensure layout is updated
    }



    public String MillisToTime(long millis) {
        long minutes = (millis / (1000 * 60)) % 60;
        long seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}