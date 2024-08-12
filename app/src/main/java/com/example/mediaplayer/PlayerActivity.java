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
import androidx.constraintlayout.widget.ConstraintSet;
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

import java.util.Locale;

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
    private boolean isPlay;


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

        Log.d(TAG, "onCreate: ");

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
                onLandscape();
                Log.d(TAG, "onRotate: " + "Landscape");
            }
            else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                onPortrait();
                Log.d(TAG, "onRotate: " + "Portrait");
            }
            isOrientation = true;
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

                    Fullscreen();

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

                    notFullscreen();


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
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onClick(View v) {
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


    private void onLandscape(){
        Log.d(TAG, "onLandscape: YES");
        View back = findViewById(R.id.back_button);
        View tool_title = findViewById(R.id.toolbar_title);
        View audio = findViewById(R.id.audio_tracks_button);
        View subtitle = findViewById(R.id.subtitles_button);
        View decoder = findViewById(R.id.decoder_button);
        View more = findViewById(R.id.more_button);
        View text1 = findViewById(R.id.time1);
        View text2 = findViewById(R.id.time2);
        View lock = findViewById(R.id.lock);
        View rotate = findViewById(R.id.rotate);
        View prev = findViewById(R.id.prev);
        View play = findViewById(R.id.play);
        View next = findViewById(R.id.next);
        View crop = findViewById(R.id.fit_crop);
        View pip = findViewById(R.id.pip);
        View lower_container = findViewById(R.id.lower_container);

        setMargin(back, 0, 15);

        setMargin(tool_title, 0, 20);
        setMargin(tool_title, 1, 30);
        setConstraint(tool_title, ConstraintSet.END, audio, ConstraintSet.START);

        audio.setVisibility(View.VISIBLE);
        subtitle.setVisibility(View.VISIBLE);

        setMargin(more, 1, 18);

        setPadding(text1, 0, 12);
        setPadding(text1, 1, 12);
        setPadding(text2, 0, 12);
        setPadding(text2, 1, 12);

        setSize(lock, 0, 60);

        setSize(rotate, 0, 60);
        setMargin(rotate, 0, 20);

        setSize(prev, 0, 85);
        setSize(play, 0, 110);
        setSize(next, 0, 85);

        setSize(crop, 0, 60);
        setMargin(crop, 1, 20);

        setSize(pip, 0, 60);

        setPadding(lower_container, 0, 20);
        setPadding(lower_container, 1, 20);
    }

    private void onPortrait(){
        Log.d(TAG, "onPortrait: YES");
        View back = findViewById(R.id.back_button);
        View tool_title = findViewById(R.id.toolbar_title);
        View audio = findViewById(R.id.audio_tracks_button);
        View subtitle = findViewById(R.id.subtitles_button);
        View decoder = findViewById(R.id.decoder_button);
        View more = findViewById(R.id.more_button);
        View text1 = findViewById(R.id.time1);
        View text2 = findViewById(R.id.time2);
        View lock = findViewById(R.id.lock);
        View rotate = findViewById(R.id.rotate);
        View prev = findViewById(R.id.prev);
        View play = findViewById(R.id.play);
        View next = findViewById(R.id.next);
        View crop = findViewById(R.id.fit_crop);
        View pip = findViewById(R.id.pip);
        View lower_container = findViewById(R.id.lower_container);

        setMargin(back, 0, 8);

        setMargin(tool_title, 0, 15);
        setMargin(tool_title, 1, 20);
        setConstraint(tool_title, ConstraintSet.END, decoder, ConstraintSet.START);

        audio.setVisibility(View.GONE);
        subtitle.setVisibility(View.GONE);

        setMargin(more, 1, 10);

        setPadding(text1, 0, 5);
        setPadding(text1, 1, 5);
        setPadding(text2, 0, 5);
        setPadding(text2, 1, 5);

        setSize(lock, 0, 50);

        setSize(rotate, 0, 50);
        setMargin(rotate, 0, 0);

        setSize(prev, 0, 50);
        setSize(play, 0, 60);
        setSize(next, 0, 50);

        setSize(crop, 0, 50);
        setMargin(crop, 1, 0);

        setSize(pip, 0, 50);

        setPadding(lower_container, 0, 15);
        setPadding(lower_container, 1, 15);
    }

    private void setSize(View view, int side, int dp){
        int px = DpToPixel(dp, this);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) view.getLayoutParams();

        if(side == 0) {
            params.width = px;
        }
        else if(side == 1) {
            params.height = px;
        }

        view.setLayoutParams(params);
    }

    private void setMargin(View view, int side, int dp){
        int px = DpToPixel(dp, this);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) view.getLayoutParams();

        if(side == 0) {
            params.setMarginStart(px);
        }
        else if(side == 1) {
            params.setMarginEnd(px);
        }

        view.setLayoutParams(params);
    }

    private void setPadding(View view, int side, int dp){
        int px = DpToPixel(dp, this);

        int currentLeftPadding = view.getPaddingLeft();
        int currentTopPadding = view.getPaddingTop();
        int currentRightPadding = view.getPaddingRight();
        int currentBottomPadding = view.getPaddingBottom();

        if (side == 0) {
            view.setPadding(px, currentTopPadding, currentRightPadding, currentBottomPadding);
        }
        else if (side == 1) {
            view.setPadding(currentLeftPadding, currentTopPadding, px, currentBottomPadding);
        }
    }

    private void setConstraint(View StartView, int StartSide, View EndView, int EndSide){
        ConstraintLayout layout = (ConstraintLayout) StartView.getParent();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout);
        constraintSet.connect(StartView.getId(), StartSide, EndView.getId(), EndSide);
        constraintSet.applyTo(layout);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            Log.d(TAG, "onPause: ");

            if (player.isPlaying()) {
                player.pause();
                isPlay = true;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            Log.d(TAG, "onPause: ");

            if (isPlay) {
                player.play();
                isPlay = false;
            }
        }
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
    }


    private void Fullscreen() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    private void notFullscreen() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_VISIBLE
        );
    }

    public String MillisToTime(long millis) {
        long minutes = (millis / (1000 * 60)) % 60;
        long seconds = (millis / 1000) % 60;
        return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
    }

    public int DpToPixel(float dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}