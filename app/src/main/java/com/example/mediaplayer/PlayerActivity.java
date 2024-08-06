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
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.OptIn;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

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
    private PlayerView playerView;
    private SurfaceHolder surfaceHolder;
    private Handler handler_surface;
    private Runnable runnable_surface;

    private Toolbar toolbar;
    private ConstraintLayout PlaybackControls_Container;
    private ImageButton fitcropButton;
    private boolean isFitScreen = true;

    private boolean f = false;
    private boolean surface_click_frag = false;

    private String media_name;
    private String media_path;

    private PlayerService playerService;
    private boolean isBound = false;

    private boolean isVideo;
    private boolean isOrientation;


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

        View mainView = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View view, @NonNull WindowInsetsCompat insets) {
                // Get status bar height from WindowInsetsCompat
                int statusBarSize_Top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                int statusBarSize_Left = insets.getInsets(WindowInsetsCompat.Type.statusBars()).left;
                int statusBarSize_Right = insets.getInsets(WindowInsetsCompat.Type.statusBars()).right;
                int navBarSize_Bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                int navBarSize_Left = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).left;
                int navBarSize_Right = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).right;

                int cutoutSize_Left = insets.getInsets(WindowInsetsCompat.Type.displayCutout()).left;
                int cutoutSize_Right = insets.getInsets(WindowInsetsCompat.Type.displayCutout()).right;
                int cutoutSize_Top = insets.getInsets(WindowInsetsCompat.Type.displayCutout()).top;
                Log.d(TAG, "onApplyWindowInsets: " + cutoutSize_Top);
                Log.d(TAG, "onApplyWindowInsets: " + cutoutSize_Left);
                Log.d(TAG, "onApplyWindowInsets: " + cutoutSize_Right);

                int orientation = getResources().getConfiguration().orientation;
                int rotation = getWindowManager().getDefaultDisplay().getRotation();

                // Update margins or padding
                ViewGroup.MarginLayoutParams params_toolbar = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
                ViewGroup.MarginLayoutParams params_bottom = (ViewGroup.MarginLayoutParams) PlaybackControls_Container.getLayoutParams();

                params_toolbar.setMargins(0, 0, 0, 0);
                params_bottom.setMargins(0, 0, 0, 0);

                if (statusBarSize_Left > 0) {
                    params_toolbar.leftMargin = statusBarSize_Left;
                    params_bottom.leftMargin = statusBarSize_Left;
                }
                else if (statusBarSize_Right > 0) {
                    params_toolbar.rightMargin = statusBarSize_Right;
                    params_bottom.rightMargin = statusBarSize_Right;
                }
                else if (statusBarSize_Top > 0) {
                    params_toolbar.topMargin = statusBarSize_Top;
                }

                if (navBarSize_Left > 0) {
                    params_toolbar.leftMargin = navBarSize_Left;
                    params_bottom.leftMargin = navBarSize_Left;
                }
                else if (navBarSize_Right > 0) {
                    params_toolbar.rightMargin = navBarSize_Right;
                    params_bottom.rightMargin = navBarSize_Right;
                }
                else if (navBarSize_Bottom > 0) {
                    params_bottom.bottomMargin = navBarSize_Bottom;
                }

//                if (orientation == Configuration.ORIENTATION_LANDSCAPE){
//                    if (rotation == Surface.ROTATION_90) {
//                        params_toolbar.leftMargin = statusBarSize_Left;
//                        params_bottom.rightMargin = navBarSize_Right;
//                    }
//                    else if (rotation == Surface.ROTATION_270){
//                        params_toolbar.rightMargin = statusBarSize_Right;
//                        params_bottom.leftMargin = navBarSize_Left;
//                    }
//                }
//                else {
//                    params_toolbar.topMargin = statusBarSize_Top;
//                    params_bottom.bottomMargin = navBarSize_Bottom;
//                }
                toolbar.setLayoutParams(params_toolbar);
                PlaybackControls_Container.setLayoutParams(params_bottom);

                return insets;
            }
        });

        // Set status bar color
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.toolbar_background_dark));

        initializeViews();

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
            updateUI();
        }


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
//                    if (player != null && surfaceView.getHolder().getSurface().isValid()) {
//                        player.setVideoSurfaceHolder(surfaceHolder);
//                    }
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

//        View decorView = getWindow().getDecorView();

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            decorView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
//                @NonNull
//                @Override
//                public WindowInsets onApplyWindowInsets(@NonNull View view, @NonNull WindowInsets insets) {
//                    // Adjust padding for the notch area
//                    DisplayCutout cutout = insets.getDisplayCutout();
//                    if (cutout != null) {
//                        view.setPadding(
//                                cutout.getSafeInsetLeft(),
//                                cutout.getSafeInsetTop(),
//                                cutout.getSafeInsetRight(),
//                                cutout.getSafeInsetBottom()
//                        );
//                    }
//                    return insets.consumeSystemWindowInsets();
//                }
//            });
//        }

        playerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!surface_click_frag){
                    Animation slideOutBottom = AnimationUtils.loadAnimation(PlayerActivity.this, R.anim.slide_out_bottom);
                    Animation slideOutTop = AnimationUtils.loadAnimation(PlayerActivity.this, R.anim.slide_out_top);
                    PlaybackControls_Container.startAnimation(slideOutBottom);
                    toolbar.startAnimation(slideOutTop);


//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                        WindowInsetsController insetsController = getWindow().getInsetsController();
//                        if (insetsController != null) {
//                            insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
//                            insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_SWIPE);
//                        }
//                    }
//                    else {
                    getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    );

                    slideOutBottom.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            toolbar.setVisibility(View.GONE);
                            PlaybackControls_Container.setVisibility(View.GONE);
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
                    PlaybackControls_Container.startAnimation(slideInBottom);
                    toolbar.startAnimation(slideInTop);

//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                        WindowInsetsController insetsController = getWindow().getInsetsController();
//                        if (insetsController != null) {
//                            insetsController.show(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
//                            insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_DEFAULT);
//                        }
//                    }
//                    else {
                    getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                                    View.SYSTEM_UI_FLAG_VISIBLE
                    );


                    slideInBottom.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            toolbar.setVisibility(View.VISIBLE);
                            PlaybackControls_Container.setVisibility(View.VISIBLE);
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


    public void initializeViews() {
        playButton = findViewById(R.id.play);

        time1 = findViewById(R.id.time1);
        time2 = findViewById(R.id.time2);
        seekbar = findViewById(R.id.seekbar);

        ToolbarText = findViewById(R.id.toolbar_title);
        backButton = findViewById(R.id.back_button);
        rotateButton = findViewById(R.id.rotate);

        playerView = findViewById(R.id.surface_view);

        toolbar = findViewById(R.id.toolbar);
        PlaybackControls_Container = findViewById(R.id.full_container);

        fitcropButton = findViewById(R.id.fit_crop);
    }


    // Initializing Player
    private void initializePlayer() {
        Log.d("TAG", "initializePlayer: " + player);
        ToolbarText.setText(media_name);
        playButton.setImageResource(R.drawable.baseline_pause_circle_outline_24);
        playerView.setPlayer(player);

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
        playerView.setPlayer(player);
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isOrientation", true);
        Log.d(TAG, "onSaveInstanceState: YES : " + isOrientation);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isOrientation = savedInstanceState.getBoolean("isOrientation");
        Log.d(TAG, "onRestoreInstanceState: YES : " + isOrientation);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            updateUI();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            Log.d(TAG, "onPause: "+ isOrientation);
            if (isOrientation) {
                isOrientation = false;
            }
            else {
                player.pause();
            }
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d(TAG, "onStop: ");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
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
        Log.d(TAG, "onConfigurationChanged: YES");

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_player);
        }
        else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            setContentView(R.layout.activity_player_land);
        }

        initializeViews();
        updateUI();

    }

    @OptIn(markerClass = UnstableApi.class)
    public void adjustAspectRatio(){
        if (!isFitScreen) {
            fitcropButton.setImageResource(R.drawable.baseline_fit_screen_24);
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            isFitScreen = true;
        }
        else {
            fitcropButton.setImageResource(R.drawable.baseline_crop_din_24);
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            isFitScreen = false;
        }
    }


    public String MillisToTime(long millis) {
        long minutes = (millis / (1000 * 60)) % 60;
        long seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}