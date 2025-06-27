package com.example.mediaplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.OptIn;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.Tracks;
import androidx.media3.common.VideoSize;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.analytics.AnalyticsListener;
import androidx.media3.exoplayer.source.TrackGroupArray;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@UnstableApi
public class PlayerActivity extends AppCompatActivity {
    private static final String TAG = "tag";
    private static ExoPlayer player;  // Make the player instance static

    private TextView time1;
    private TextView time2;
    private SeekBar seekbar;

    private TextView ToolbarText;

    private ImageButton playButton;
    private ImageButton prevButton;
    private ImageButton nextButton;
    private ImageButton backButton;
    private ImageButton rotateButton;
    private ImageButton lockButton;
    private ImageButton unlockButton;
    private ImageButton decoderButton;
    private ImageButton audioTrackButton;
    private ImageButton subTrackButton;

    private Handler handler;
    private Runnable updateSeekBar;
    private Runnable controlsRunnable;
    private Runnable lockScreenRunnable;

    private SurfaceView surfaceView;
    private PlayerView playerView;

    private Toolbar toolbar;
    private ConstraintLayout PlaybackControls_Container;
    private ImageButton fitcropButton;
    private boolean isFitScreen = true;

    private boolean f;
    private boolean surface_click_frag = false;

    private String media_name;
    private String media_path;
    private int currentIndex = 0;

    private boolean isBound = false;

    private boolean isOrientation;
    private boolean isPlay;

    private boolean isInAnimation;
    private boolean isOutAnimation;

    private View buffer_view;

    private boolean isTime1ButtonClicked;
    private boolean isTime2ButtonClicked;

    private boolean isBufferingFinished;
    private boolean isVideoFile;
    private boolean isBackgroundPlay;

    private SharedPreferences playerPrefs;
    private SharedPreferences.Editor playerPrefsEditor;

    private GestureDetector gestureDetector;
    private AudioManager audioManager;
    private AudioAttributes audioAttributes;
    private AudioFocusRequest focusRequest;
    private boolean wasPlaying_Focus;
    private boolean hasAudioFocus;

    private int screenWidth;
    private int halfWidth;
    private int Q3_Width;
    private int Q4_Width;
    private float totalScrollX = 0f;
    private float totalScrollY1 = 0f;
    private float totalScrollY2 = 0f;

    private View volumeLayout;
    private View brightnessLayout;
    private TextView volumeText;
    private TextView brightnessText;

    private View seekLayout;
    private TextView seekCurrentTimeText;
    private TextView seekTimeText;

    private View speedToastLayout;

    private View forwardLayout;
    private View rewindLayout;
    private TextView forwardText;
    private TextView rewindText;

    private long doubleTapSeekTime = 10000;

    private int seekCurrentTime;
    private int seekTime;

    private boolean isLongPressed;
    private boolean isScrolling;
    private boolean isSeeking;
    private boolean isVolumeChanging;
    private boolean isBrightnessChanging;

    private boolean isControlsShowing;
    private boolean isControlsHidden;
    private boolean isControlsShowingByAction;

    private boolean isExpandViewsShowing;

    private boolean isSeekbarMoving;
    private boolean isSeekbarUpdating;

    private boolean isScreenLocked;
    private boolean isLockScreenShowing;

    private LinearLayout expandView;
    private ScrollView expandScrollView;
    private ImageButton expandB;

    private TextView speedText_Expand;
    private LinearLayout speedExpandB;
    private LinearLayout speedLayout;
    private TextView speedText;
    private Slider sliderSpeed;
    private ImageButton incrSpeed;
    private ImageButton decrSpeed;
    private Button speed75;
    private Button speed100;
    private Button speed125;
    private Button speed150;
    private Button speed175;
    private Button speed200;
    private float PlaybackSpeed = 1;

    private enum GestureDirection {NONE, HORIZONTAL, VERTICAL}

    private GestureDirection gestureDirection = GestureDirection.NONE;

    private boolean isPlaying;
    private boolean wasPlaying;

    private boolean isFirstTimePlaying;

    private View audioTracksContainer;
    private ImageButton audioTracks_BackButton;
    private RecyclerView audioTracksRecyclerView;
    private DefaultTrackSelector audioTrackSelector;
    private AudioTracksAdapter audioTracksAdapter;
    private List<AudioTracks> audioTracksList;

    private View subTracksContainer;
    private ImageButton subTracks_BackButton;
    private RecyclerView subTracksRecyclerView;
    private DefaultTrackSelector subTrackSelector;
    private SubTracksAdapter subTracksAdapter;
    private List<SubTracks> subTracksList;

    private List<MediaItem> videoList;
    private PlaylistManager manager;
    private MediaItem mediaItem;

    private float lastPlayedTime;
    private float lastPlayedFile;


    @OptIn(markerClass = UnstableApi.class)
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

//                int orientation = getResources().getConfiguration().orientation;
//                int rotation = getWindowManager().getDefaultDisplay().getRotation();

                // Update margins or padding
                ViewGroup.MarginLayoutParams params_toolbar = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
                ViewGroup.MarginLayoutParams params_bottom = (ViewGroup.MarginLayoutParams) PlaybackControls_Container.getLayoutParams();

                params_toolbar.setMargins(0, 0, 0, 0);
                params_bottom.setMargins(0, 0, 0, 0);

                if (statusBarSize_Left > 0) {
                    params_toolbar.leftMargin = statusBarSize_Left;
                    params_bottom.leftMargin = statusBarSize_Left;
                } else if (statusBarSize_Right > 0) {
                    params_toolbar.rightMargin = statusBarSize_Right;
                    params_bottom.rightMargin = statusBarSize_Right;
                } else if (statusBarSize_Top > 0) {
                    params_toolbar.topMargin = statusBarSize_Top;
                }

                if (navBarSize_Left > 0) {
                    params_toolbar.leftMargin = navBarSize_Left;
                    params_bottom.leftMargin = navBarSize_Left;
                } else if (navBarSize_Right > 0) {
                    params_toolbar.rightMargin = navBarSize_Right;
                    params_bottom.rightMargin = navBarSize_Right;
                } else if (navBarSize_Bottom > 0) {
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

        notFullscreen();
        updateScreenDimension();

        playButton = findViewById(R.id.play);
        prevButton = findViewById(R.id.prev);
        nextButton = findViewById(R.id.next);

        time1 = findViewById(R.id.time1);
        time2 = findViewById(R.id.time2);
        seekbar = findViewById(R.id.seekbar);

        ToolbarText = findViewById(R.id.toolbar_title);
        backButton = findViewById(R.id.back_button);
        rotateButton = findViewById(R.id.rotate);
        lockButton = findViewById(R.id.lock);
        unlockButton = findViewById(R.id.unlock);
        decoderButton = findViewById(R.id.decoder_button);
        audioTrackButton = findViewById(R.id.audio_tracks_button);
        subTrackButton = findViewById(R.id.sub_tracks_button);

        playerView = findViewById(R.id.surface_view);

        toolbar = findViewById(R.id.toolbar);
        PlaybackControls_Container = findViewById(R.id.full_container);

        fitcropButton = findViewById(R.id.fit_crop);

        buffer_view = findViewById(R.id.buffer_layout);
        buffer_view.setVisibility(View.VISIBLE);

        audioTracks_BackButton = findViewById(R.id.audioTracks_BackButton);
        audioTracksContainer = findViewById(R.id.audioTracksContainer);
        audioTracksRecyclerView = findViewById(R.id.recyclerAudioTracksList);
        audioTracksRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        subTracks_BackButton = findViewById(R.id.subTracks_BackButton);
        subTracksContainer = findViewById(R.id.subTracksContainer);
        subTracksRecyclerView = findViewById(R.id.recyclerSubTracksList);
        subTracksRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        volumeLayout = findViewById(R.id.volume_layout);
        brightnessLayout = findViewById(R.id.brightness_layout);
        speedToastLayout = findViewById(R.id.speedToast_layout);
        volumeText = findViewById(R.id.volume_text);
        brightnessText = findViewById(R.id.brightness_text);

        seekLayout = findViewById(R.id.seek_layout);
        seekCurrentTimeText = findViewById(R.id.seek_current_time);
        seekTimeText = findViewById(R.id.seek_time);

        forwardLayout = findViewById(R.id.forward_layout);
        rewindLayout = findViewById(R.id.rewind_layout);
        forwardText = findViewById(R.id.forward_text);
        rewindText = findViewById(R.id.rewind_text);

        expandB = findViewById(R.id.expandB);
        expandView = findViewById(R.id.expandView);
        expandScrollView = findViewById(R.id.expandScroll);

        speedText_Expand = findViewById(R.id.speedText_expand);
        speedExpandB = findViewById(R.id.speed_expand);
        speedLayout = findViewById(R.id.speedLayout);
        speedText = findViewById(R.id.speedText);
        sliderSpeed = findViewById(R.id.sliderSpeed);
        incrSpeed = findViewById(R.id.incrSpeed);
        decrSpeed = findViewById(R.id.decrSpeed);
        speed75 = findViewById(R.id.speed75);
        speed100 = findViewById(R.id.speed100);
        speed125 = findViewById(R.id.speed125);
        speed150 = findViewById(R.id.speed150);
        speed175 = findViewById(R.id.speed175);
        speed200 = findViewById(R.id.speed200);

        videoList = new ArrayList<>();

        audioTracksList = new ArrayList<>();
        subTracksList = new ArrayList<>();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);


        playerPrefs = getSharedPreferences("PlayerPrefs", Context.MODE_PRIVATE);
        playerPrefsEditor = playerPrefs.edit();


        Intent intent = getIntent();
        media_name = intent.getStringExtra("Name");
        media_path = intent.getStringExtra("Path");
        isVideoFile = intent.getBooleanExtra("isVideo", false);
        currentIndex = intent.getIntExtra("currentIndex", 0);

        playerPrefsEditor.putString("lastPlayedFileName", media_name);
        playerPrefsEditor.putString("lastPlayedFilePath", media_path);
        playerPrefsEditor.putBoolean("lastPlayedFile_isVideo", isVideoFile);
        playerPrefsEditor.apply();

        Log.d("isVideoFile", "onCreate: " + isVideoFile);

        Intent serviceIntent = new Intent(this, PlayerService.class);
        serviceIntent.putExtra("name", media_name);
        serviceIntent.putExtra("path", media_path);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        startService(serviceIntent);


        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekbarMoving = true;

                if (isControlsShowing) {
                    removeControlsRunnable();
                }

                seekTimeText.setVisibility(View.GONE);
                seekLayout.setVisibility(View.VISIBLE);
                seekLayout.startAnimation(fadeIn);

                wasPlaying = player.isPlaying();
                player.pause();
                f = true;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    player.seekTo(progress);
                    seekCurrentTime = (int) player.getCurrentPosition();
                    seekCurrentTimeText.setText(MillisToTime(seekCurrentTime));
                }

                if (isTime1ButtonClicked) {
                    time1.setText("-" + MillisToTime(player.getDuration() - progress));
                } else {
                    time1.setText(MillisToTime(progress));
                }

                if (isTime2ButtonClicked) {
                    time2.setText("-" + MillisToTime(player.getDuration() - progress));
                } else {
                    time2.setText(MillisToTime(player.getDuration()));
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                player.seekTo(seekBar.getProgress());
                isSeekbarMoving = false;

                if (isControlsShowing) {
                    controlsToast();
                }

                if (wasPlaying) {
                    playMedia();
                    wasPlaying = false;
                }

                seekTime = 0;
                seekLayout.startAnimation(fadeOut);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        seekLayout.setVisibility(View.GONE);
                        seekTimeText.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                f = false;
            }
        });

        handler = new Handler();
        seekbarUpdater();


        playButton.setOnClickListener(v -> {
            if (player != null) {
                if (!player.isPlaying()) {
                    playMedia();
//                        player.play();
                    playButton.setImageResource(R.drawable.baseline_pause_circle_outline_24);
                    Log.d(TAG, "onPLAY: YES");
                } else {
                    player.pause();
                    removeControlsRunnable();
                    playButton.setImageResource(R.drawable.baseline_play_circle_outline_24);
                    Log.d(TAG, "onPLAY: NO");
                }
            }
        });

        prevButton.setOnClickListener(v -> {
            manager.previous();
            initializePlayer(manager.getCurrentItem(), false);
        });

        nextButton.setOnClickListener(v -> {
            manager.next();
            initializePlayer(manager.getCurrentItem(), false);
        });


        backButton.setOnClickListener(v -> finish());

        rotateButton.setOnClickListener(v -> {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                Log.d(TAG, "onRotate: " + "Landscape");
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                Log.d(TAG, "onRotate: " + "Portrait");
            }
            isOrientation = true;
        });

        fitcropButton.setOnClickListener(new View.OnClickListener() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onClick(View v) {
                if (!isFitScreen) {
                    fitcropButton.setImageResource(R.drawable.baseline_fit_screen_24);
                    playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                    isFitScreen = true;
                } else {
                    fitcropButton.setImageResource(R.drawable.baseline_crop_din_24);
                    playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                    isFitScreen = false;
                }
            }
        });

        lockButton.setOnClickListener(v -> {
            isScreenLocked = true;
            hideControls();
            removeControlsRunnable();
            showLockButton();
            lockScreenToast();
        });

        unlockButton.setOnClickListener(v -> {
            isScreenLocked = false;
            hideLockButton();
            removeLockScreenRunnable();
        });


        time1.setOnClickListener(v -> {
            if (!isTime1ButtonClicked) {
                time1.setText("-" + MillisToTime(player.getDuration() - player.getCurrentPosition()));
                isTime1ButtonClicked = true;
            } else {
                time1.setText(MillisToTime(player.getCurrentPosition()));
                isTime1ButtonClicked = false;
            }
            Log.d(TAG, "onClickTime1: " + isTime1ButtonClicked);
        });

        time2.setOnClickListener(v -> {
            if (!isTime2ButtonClicked) {
                time2.setText("-" + MillisToTime(player.getDuration() - player.getCurrentPosition()));
                isTime2ButtonClicked = true;
            } else {
                time2.setText(MillisToTime(player.getDuration()));
                isTime2ButtonClicked = false;
            }
            Log.d(TAG, "onClickTime2: " + isTime2ButtonClicked);
        });

        expandB.setOnClickListener(v -> {
            Log.d("dgiuhors", "onCreate: jbissg");
            showExpandViewsLayout(75, 300);
            isExpandViewsShowing = true;
            hideControls();
            removeControlsRunnable();
        });


        audioTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAudioTracks();
            }
        });

        audioTracks_BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAudioTracks();
            }
        });

        subTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSubTracks();
            }
        });

        subTracks_BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSubTracks();
            }
        });

        // Expand Views Buttons ---->

        speedExpandB.setOnClickListener(v -> {
            ;
        });


        sliderSpeed.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                PlaybackSpeed = value;
                if (player != null) player.setPlaybackSpeed(PlaybackSpeed);
                speedText.setText(String.format("%sX", PlaybackSpeed));
            }
        });

        sliderSpeed.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                speedText_Expand.setText(String.format("%sX", PlaybackSpeed));
            }
        });

        incrSpeed.setOnClickListener(v -> {
            PlaybackSpeed += 0.05F;
            if (player != null) player.setPlaybackSpeed(PlaybackSpeed);
            sliderSpeed.setValue(PlaybackSpeed);
            speedText.setText(String.format(Locale.ROOT, "%.2fX", PlaybackSpeed));
        });

        decrSpeed.setOnClickListener(v -> {
            PlaybackSpeed -= 0.05F;
            if (player != null) player.setPlaybackSpeed(PlaybackSpeed);
            sliderSpeed.setValue(PlaybackSpeed);
            speedText.setText(String.format(Locale.ROOT, "%.2fX", PlaybackSpeed));
        });

        Button[] speedButtons = {speed75, speed100, speed125, speed150, speed175, speed200};
        for (Button btn : speedButtons) {
            btn.setOnClickListener(v -> {
                String speedStr = ((Button) v).getText().toString();
                PlaybackSpeed = Float.parseFloat(speedStr);
                if (player != null) player.setPlaybackSpeed(PlaybackSpeed);
                sliderSpeed.setValue(PlaybackSpeed);
                speedText.setText(String.format("%sX", PlaybackSpeed));
            });
        }

        playerView.setOnTouchListener((v, event) -> {
            if (!isScreenLocked) {
                if (!isAudioTracksShowing && !isSubTracksShowing && !isExpandViewsShowing)
                    gestureDetector.onTouchEvent(event);

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (isAudioTracksShowing) {
                        hideAudioTracks();
                        isAudioTracksShowing = false;
                    }

                    if (isSubTracksShowing) {
                        hideSubTracks();
                        isSubTracksShowing = false;
                    }

                    if (isExpandViewsShowing) {
                        hideExpandViewsLayout();
                        isExpandViewsShowing = false;
                    }

                    if (isLongPressed) {
                        isLongPressed = false;
                        player.setPlaybackSpeed(1);
                        speedToastLayout.startAnimation(fadeOut);
                        fadeOut.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                speedToastLayout.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                    }

                    if (isSeeking) {
                        if (isControlsShowing) {
                            controlsToast();
                        } else {
                            hideBottomControls();
                        }

                        if (wasPlaying) {
                            playMedia();
                            wasPlaying = false;
                        }

                        seekTime = 0;
                        seekLayout.startAnimation(fadeOut);
                        fadeOut.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                seekLayout.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                    }

                    if (isScrolling) {
                        if (isBrightnessChanging) {
                            playerPrefsEditor.putFloat("Brightness", getWindow().getAttributes().screenBrightness);
                            playerPrefsEditor.apply();
                            Log.d("inittt", "onCreate: " + playerPrefs.getFloat("Brightness", 0f));
                        }

                        isScrolling = false;
                        isSeeking = false;
                        isVolumeChanging = false;
                        isBrightnessChanging = false;
                        gestureDirection = GestureDirection.NONE;
                        Log.d("Scrolling", "onScroll: " + gestureDirection.name());
                    }


                }
            }

            else {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!isLockScreenShowing) {
                        showLockButton();
                    }
                    lockScreenToast();
                }
            }

            v.performClick();
            return true;
        });

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // Handle single tap
                if (!surface_click_frag && !isInAnimation && !isOutAnimation) {
                    removeControlsRunnable();
                    hideControls();
                } else if (!isInAnimation && !isOutAnimation && !isSeeking) {
                    showControls();
                }

                return super.onSingleTapUp(e);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (!isLongPressed && !isScrolling) {
                    if (e.getX() <= Q3_Width) {
                        player.seekTo(player.getCurrentPosition() - doubleTapSeekTime);
                        showRewindLayout(rewindLayout, rewindText);
                        Log.d(TAG, "onDoubleTap: " + Q3_Width + " " + e.getX() + " " + screenWidth);
                    } else if (e.getX() <= 2 * Q3_Width) {
                        if (player != null) {
                            if (!player.isPlaying()) {
                                playMedia();
                                isPlaying = true;
                                playButton.setImageResource(R.drawable.baseline_pause_circle_outline_24);
                                Log.d(TAG, "onPLAY: YES");
                            } else {
                                player.pause();
                                isPlaying = false;
                                playButton.setImageResource(R.drawable.baseline_play_circle_outline_24);
                                Log.d(TAG, "onPLAY: NO");
                            }
                            Log.d(TAG, "onDoubleTap: " + 2 * Q3_Width + " " + e.getX() + " " + screenWidth);
                        }
                    } else {
                        player.seekTo(player.getCurrentPosition() + doubleTapSeekTime);
                        showForwardLayout(forwardLayout, forwardText);
                        Log.d(TAG, "onDoubleTap: " + 3 * Q3_Width + " " + e.getX() + " " + screenWidth);
                    }
                }

                return super.onDoubleTap(e);
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (isPlaying && !isScrolling) {
                    isLongPressed = true;
                    hideLayouts(volumeLayout, brightnessLayout);

                    if (e.getX() < halfWidth) {
                        ;
                    } else {
                        speedToastLayout.setVisibility(View.VISIBLE);
                        speedToastLayout.startAnimation(fadeIn);

                        player.setPlaybackSpeed(2);
                    }
                }
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                Log.d("Scrolling", "onScroll: " + gestureDirection.name() + "  " + isSeeking);
                isScrolling = true;

                if (gestureDirection == GestureDirection.NONE && !isLongPressed) {
                    if (Math.abs(distanceX) > Math.abs(distanceY)) {
                        if (isControlsShowing) {
                            removeControlsRunnable();
                        } else {
                            showBottomControls();
                        }

                        seekTimeText.setVisibility(View.VISIBLE);
                        seekLayout.setVisibility(View.VISIBLE);
                        seekLayout.startAnimation(fadeIn);

                        wasPlaying = player.isPlaying();
                        player.pause();
                        isControlsShowingByAction = true;

                        // Lock as horizontal gesture (seeking)
                        gestureDirection = GestureDirection.HORIZONTAL;
                    } else {
                        // Lock as vertical gesture (volume/brightness adjustment)
                        gestureDirection = GestureDirection.VERTICAL;
                    }
                }

                if (gestureDirection == GestureDirection.HORIZONTAL) {
                    isSeeking = true;
                    totalScrollX += distanceX;

                    if (Math.abs(totalScrollX) > 20) {
                        if (totalScrollX > 0) {
                            performSeekByTouch(-1000, false);
                        } else {
                            performSeekByTouch(1000, false);
                        }
                        totalScrollX = 0;
                    }
                }
                else if (gestureDirection == GestureDirection.VERTICAL) {
                    if (e1.getX() < halfWidth) {
                        isBrightnessChanging = true;
                        totalScrollY1 += distanceY;

                        if (Math.abs(totalScrollY1) > 50) {
                            if (totalScrollY1 > 0) {
                                // Scrolled on left, increase volume
                                adjustBrightness(0.034f);
                            } else {
                                // Scrolled on right, decrease volume
                                adjustBrightness(-0.034f);
                            }

                            totalScrollY1 = 0;
                        }
                    } else {
                        isVolumeChanging = true;
                        totalScrollY2 += distanceY;

                        if (Math.abs(totalScrollY2) > 50) {
                            float maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                            int incrementValue = (int) ((5.0 / 150) * maxVolume);
                            incrementValue = Math.max(1, incrementValue);

                            if (totalScrollY2 > 0) {
                                // Scrolled on left, increase volume
                                adjustVolume(incrementValue);
                            } else {
                                // Scrolled on right, decrease volume
                                adjustVolume(-incrementValue);
                            }

                            totalScrollY2 = 0;
                        }
                    }
                }

                return super.onScroll(e1, e2, distanceX, distanceY);
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                // Handle double tap event
                return super.onDoubleTapEvent(e);
            }
        });
    }


    @OptIn(markerClass = UnstableApi.class)
    private void initializePlayer(MediaItem mediaItem, boolean isNamePresent) {
        this.mediaItem = mediaItem;

        Log.d("TAG", "initializePlayer: " + player);

        String name = isNamePresent ? media_name : String.valueOf(mediaItem.mediaMetadata.title);
        ToolbarText.setText(name);
        playButton.setImageResource(R.drawable.baseline_play_circle_outline_24);
        buffer_view.setVisibility(View.VISIBLE);

        isFirstTimePlaying = true;
        player.setMediaItem(mediaItem);
        player.prepare();
        playMedia();

        Log.d("mediaitem56", "initializePlayer: " + player.getMediaItemCount() + "  " + player.getCurrentMediaItemIndex());

        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = playerPrefs.getFloat("Brightness", 0f);
        getWindow().setAttributes(layoutParams);

        Log.d("inittt", "init: " + playerPrefs.getFloat("Brightness", 0f));
        
        player.addListener(new Player.Listener() {
            @Override
            public void onVideoSizeChanged(VideoSize size) {
                Player.Listener.super.onVideoSizeChanged(size);

                int width = size.width;
                int height = size.height;

                if (player.getPlaybackState() == Player.STATE_READY && width != 0 || height != 0) {

                    Log.d("VideoDimensions", "Width: " + width + ", Height: " + height);

                    Log.d("isVideoFile", "listener1: " + isVideoFile);

                    // Rotating logic according to dimensions
                    if (isVideoFile) {
                        if (height > width) {
                            isVideoFile = false;
                        } else {
                            isVideoFile = true;
                        }
                    }

                    Log.d("isVideoFile", "listener2: " + isVideoFile);

                    // Removing the listener after first usage
                    player.removeListener(this);
                }
            }


        });

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                switch (playbackState) {
                    case Player.STATE_READY:
                        if (!isSeekbarUpdating) {
                            handler.post(updateSeekBar);
                            isSeekbarUpdating = true;
                        }

                        Log.d("playback544", "onPlaybackStateChanged PLaying: " + isFirstTimePlaying);
                        if (isFirstTimePlaying) {
                            Log.d("isVideoFile", "initializePlayer: " + isVideoFile);

                            lastPlayedTime = playerPrefs.getFloat("lastTime : " + mediaItem.requestMetadata.mediaUri, 0);
                            player.seekTo((long) lastPlayedTime);

                            if (isVideoFile) {
                                isBackgroundPlay = false;
                                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            }
                            else {
                                isBackgroundPlay = true;
                            }

                            // saveData("isBackgroundPlay", null, isBackgroundPlay);
                            Intent intent = new Intent("BG_PLAY_STATUS_CHANGED");
                            intent.putExtra("bgPlay", isBackgroundPlay);
                            LocalBroadcastManager.getInstance(PlayerActivity.this).sendBroadcast(intent);

                            audioTracksList.clear();
                            audioTrackSelector = (DefaultTrackSelector) player.getTrackSelector();
                            audioTracksAdapter = new AudioTracksAdapter(audioTracksList, audioTrackSelector);
                            audioTracksRecyclerView.setAdapter(audioTracksAdapter);

                            subTracksList.clear();
                            subTrackSelector = (DefaultTrackSelector) player.getTrackSelector();
                            subTracksAdapter = new SubTracksAdapter(subTracksList, subTrackSelector);
                            subTracksRecyclerView.setAdapter(subTracksAdapter);

                            loadAudioTracks();
                            loadSubTracks();

                            buffer_view.setVisibility(View.GONE);
                            seekbar.setMax((int) player.getDuration());
                            seekbar.setProgress((int) player.getCurrentPosition());
                            time1.setText(isTime1ButtonClicked ? "-" + MillisToTime(player.getDuration() - player.getCurrentPosition()) : MillisToTime(player.getCurrentPosition()));
                            time2.setText(isTime2ButtonClicked ? "-" + MillisToTime(player.getDuration() - player.getCurrentPosition()) : MillisToTime(player.getDuration()));
                            isFirstTimePlaying = false;
                            controlsToast();
                        }
                        break;

                    case Player.STATE_ENDED:
                        playerPrefsEditor.putFloat("lastTime : " + mediaItem.requestMetadata.mediaUri, 0);
                        playerPrefsEditor.apply();
                        player.pause();
                        playButton.setImageResource(R.drawable.baseline_play_circle_outline_24);
                        seekbar.setProgress(0);
                        player.seekTo(0);
                        break;

                    case Player.STATE_BUFFERING:
                        Log.d("brightness", "initializePlayer: " + playerPrefs.getFloat("Brightness", 0f));
                        break;

                    case Player.STATE_IDLE:
                        Log.d(TAG, "onPlaybackStateChanged: IDLE");
                        break;
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Log.d("playbackError", "onPlayerError: " + error);
            }
        });

        player.addAnalyticsListener(new AnalyticsListener() {
            @Override
            public void onVideoDecoderInitialized(EventTime eventTime, String decoderName, long initializedTimestampMs, long initializationDurationMs) {
                if (decoderName.contains("hardware")) {
                    decoderButton.setImageResource(R.drawable.sw); // Software decoder is being used
                } else {
                    decoderButton.setImageResource(R.drawable.hw); // Hardware decoder is being used
                }
                Log.d("DecoderInfo", "Decoder Name: " + decoderName);
            }
        });
    }

    @OptIn(markerClass = UnstableApi.class)
    private void loadAudioTracks() {
        // Get current tracks and check for selection
        Tracks currentTracks = player.getCurrentTracks();

        new Thread(() -> {
            // Fetch the track info
            DefaultTrackSelector.MappedTrackInfo mappedTrackInfo = audioTrackSelector.getCurrentMappedTrackInfo();

            if (mappedTrackInfo == null) {
                Log.e("AudioTrack", "No track info available.");
                return;
            }

            for (int rendererIndex=0; rendererIndex<mappedTrackInfo.getRendererCount(); rendererIndex++) {
                int trackType = mappedTrackInfo.getRendererType(rendererIndex);

                if (trackType == C.TRACK_TYPE_VIDEO || trackType == C.TRACK_TYPE_AUDIO) {
                    TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(trackType);

                    // Collect available audio track names
                    List<String> audioTrackNames = new ArrayList<>();
                    for (int i = 0; i < trackGroups.length; i++) {
                        TrackGroup trackGroup = trackGroups.get(i);
                        for (int j = 0; j < trackGroup.length; j++) {
                            Format format = trackGroup.getFormat(j);
                            String trackName = format.language != null ? format.language : "Unknown";
                            audioTrackNames.add(trackName);

                            if (format.sampleMimeType != null) {
                                if (format.sampleMimeType.startsWith("audio/")) {
                                    // Check if this track is selected when played
                                    boolean isSelected = false;
                                    for (Tracks.Group group : currentTracks.getGroups()) {
                                        if (group.getMediaTrackGroup() == trackGroup && group.getTrackFormat(j).sampleMimeType != null) {
                                            if (group.getTrackFormat(j).sampleMimeType.startsWith("audio/")) {
                                                isSelected = group.isTrackSelected(j);
                                                break;
                                            }
                                        }
                                    }
                                    Log.d("AudioTracks", "loadAudioTracks: " + isSelected);

                                    audioTracksList.add(new AudioTracks(trackGroup, j, format.label, getTrackLanguage(format.language), format.channelCount, isSelected));
                                    Log.d("audioTracks", "showAudioTracks: " + format.id + "   " + format.language + "   " + format.channelCount + "   " + format.label + "   " + format.bitrate);
                                }
                            }
                        }
                    }
                }
            }


            runOnUiThread(() -> {
                audioTracksAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    @OptIn(markerClass = UnstableApi.class)
    private void loadSubTracks() {
        // Get current tracks and check for selection
        Tracks currentTracks = player.getCurrentTracks();

        new Thread(() -> {
            // Fetch the track info
            DefaultTrackSelector.MappedTrackInfo mappedTrackInfo = audioTrackSelector.getCurrentMappedTrackInfo();

            if (mappedTrackInfo == null) {
                Log.e("SubTrack", "No track info available.");
                return;
            }

            for (int rendererIndex=0; rendererIndex<mappedTrackInfo.getRendererCount(); rendererIndex++) {
                int trackType = mappedTrackInfo.getRendererType(rendererIndex);

                if (trackType == C.TRACK_TYPE_VIDEO || trackType == C.TRACK_TYPE_TEXT) {
                    TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(trackType);

                    // Collect available audio track names
                    for (int i = 0; i < trackGroups.length; i++) {
                        TrackGroup trackGroup = trackGroups.get(i);
                        for (int j = 0; j < trackGroup.length; j++) {
                            Format format = trackGroup.getFormat(j);
                            if (format.sampleMimeType != null) {
                                if (format.sampleMimeType.startsWith("application/")) {
                                    // Check if this track is selected
                                    boolean isSelected = false;
                                    for (Tracks.Group group : currentTracks.getGroups()) {
                                        if (group.getMediaTrackGroup() == trackGroup && group.getTrackFormat(j).sampleMimeType != null) {
                                            if (group.getTrackFormat(j).sampleMimeType.startsWith("application/")) {
                                                isSelected = group.isTrackSelected(j);
                                                break;
                                            }
                                        }
                                    }
                                    Log.d("SubTracks", "loadSubTracks: " + isSelected);

                                    subTracksList.add(new SubTracks(trackGroup, j, format.label, getTrackLanguage(format.language), isSelected));
                                    Log.d("subTracks", "showAudioTracks: " + format.id + "   " + format.language + "   " + format.channelCount + "   " + format.label + "   " + format.bitrate);
                                }
                            }
                        }
                    }
                }
            }

            runOnUiThread(() -> {
                subTracksAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    private String getTrackLanguage(String language) {
        if (language == null || language.isEmpty()) {
            return "Unknown"; // or "Undetermined"
        }

        Locale locale = new Locale(language);
        return locale.getDisplayLanguage();
    }

//    private boolean getCurrentPlayingAudioTrack() {
//        Tracks currentTracks = player.getCurrentTracks();
//
//        for (Tracks.Group group : currentTracks.getGroups()) {
//            if (group.getType() == C.TRACK_TYPE_AUDIO) { // Ensure it's an audio track group
//
//                for (int i = 0; i < group.length; i++) { // Iterate through all tracks in this group
//                    if (group.isTrackSelected(i)) { // Check if this specific track is selected
//                        Format format = group.getTrackFormat(i);
//
//                        Log.d("SelectedAudioTrack", "Track ID: " + format.id +
//                                ", Language: " + (format.language != null ? format.language : "Unknown") +
//                                ", Channels: " + format.channelCount +
//                                ", Label: " + format.label +
//                                ", Bitrate: " + format.bitrate);
//
//                        // Perform any action with the selected track info
//                    }
//                }
//            }
//        }
//    }

    private void showExpandViewsLayout(long delayBetween, long duration) {
        LinearLayout linearLayout = expandView;
        ScrollView scrollView = expandScrollView;

        scrollView.post(() -> {
            scrollView.setTranslationX(scrollView.getWidth());
            scrollView.setVisibility(View.VISIBLE);

            scrollView.animate().cancel();
            scrollView.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(200)
                    .start();
        });

        int count = linearLayout.getChildCount();
        linearLayout.setVisibility(View.VISIBLE);

        for (int i = 0; i < count; i++) {
            final View view = linearLayout.getChildAt(i);
            view.animate().cancel();
            view.setAlpha(0f);
            view.setVisibility(View.VISIBLE);

            view.animate()
                    .alpha(1f)
                    .setDuration(duration)
                    .setStartDelay(i * delayBetween)
                    .start();
        }
    }

    private void hideExpandViewsLayout() {
        ScrollView scrollView = expandScrollView;

        scrollView.animate().cancel();
        scrollView.animate()
                .translationX(scrollView.getWidth())
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> {
                    scrollView.setVisibility(View.GONE);
                })
                .start();
    }


    private void showControls() {
        isControlsHidden = false;
        isControlsShowing = true;

        Animation slideInBottom = AnimationUtils.loadAnimation(PlayerActivity.this, R.anim.slide_in_bottom);
        Animation slideInTop = AnimationUtils.loadAnimation(PlayerActivity.this, R.anim.slide_in_top);
        PlaybackControls_Container.startAnimation(slideInBottom);
        toolbar.startAnimation(slideInTop);

        slideInBottom.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isInAnimation = true;
                toolbar.setVisibility(View.VISIBLE);
                PlaybackControls_Container.setVisibility(View.VISIBLE);
                surface_click_frag = false;
                notFullscreen();
                Log.d("Bottom", "onAnimationStart: Yes");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isInAnimation = false;
                controlsToast();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void hideControls() {
        isControlsShowing = false;
        isControlsHidden = true;

        Animation slideOutBottom = AnimationUtils.loadAnimation(PlayerActivity.this, R.anim.slide_out_bottom);
        Animation slideOutTop = AnimationUtils.loadAnimation(PlayerActivity.this, R.anim.slide_out_top);
        PlaybackControls_Container.startAnimation(slideOutBottom);

        toolbar.startAnimation(slideOutTop);

        slideOutBottom.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isOutAnimation = true;
                Fullscreen();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isOutAnimation = false;
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

    private void showLockButton() {
        Animation fadeIn = AnimationUtils.loadAnimation(PlayerActivity.this, R.anim.fade_in);
        unlockButton.startAnimation(fadeIn);
        
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                unlockButton.setVisibility(View.VISIBLE);
                isLockScreenShowing = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void hideLockButton() {
        Animation fadeOut = AnimationUtils.loadAnimation(PlayerActivity.this, R.anim.fade_out);
        unlockButton.startAnimation(fadeOut);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                unlockButton.setVisibility(View.GONE);
                isLockScreenShowing = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void showBottomControls() {
        Animation slideInBottom = AnimationUtils.loadAnimation(PlayerActivity.this, R.anim.slide_in_bottom);
        PlaybackControls_Container.startAnimation(slideInBottom);

        slideInBottom.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                PlaybackControls_Container.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void hideBottomControls() {
        Animation slideOutBottom = AnimationUtils.loadAnimation(PlayerActivity.this, R.anim.slide_out_bottom);
        PlaybackControls_Container.startAnimation(slideOutBottom);

        slideOutBottom.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                PlaybackControls_Container.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
    
    private void removeControlsRunnable() {
        if (controlsRunnable != null) {
            handler.removeCallbacks(controlsRunnable);
        }
    }

    private void removeLockScreenRunnable() {
        if (lockScreenRunnable != null) {
            handler.removeCallbacks(lockScreenRunnable);
        }
    }


    private boolean isAudioTracksShowing;
    private boolean isSubTracksShowing;

    private void showAudioTracks() {
        Animation slideInRight = AnimationUtils.loadAnimation(PlayerActivity.this, R.anim.slide_in_right);
        hideControls();
        removeControlsRunnable();
        audioTracksContainer.startAnimation(slideInRight);

        slideInRight.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart (Animation animation){
                audioTracksContainer.setVisibility(View.VISIBLE);
                isAudioTracksShowing = true;
            }
    
            @Override
            public void onAnimationEnd (Animation animation){
            }
    
            @Override
            public void onAnimationRepeat (Animation animation){}
    });
    }

    private void hideAudioTracks() {
        Animation slideOutRight = AnimationUtils.loadAnimation(PlayerActivity.this, R.anim.slide_out_right);
        audioTracksContainer.startAnimation(slideOutRight);

        slideOutRight.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart (Animation animation){}

            @Override
            public void onAnimationEnd (Animation animation){
                isAudioTracksShowing = false;
                audioTracksContainer.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat (Animation animation){}
        });
    }

    private void showSubTracks() {
        Animation slideInRight = AnimationUtils.loadAnimation(PlayerActivity.this, R.anim.slide_in_right);
        hideControls();
        removeControlsRunnable();
        subTracksContainer.startAnimation(slideInRight);

        slideInRight.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart (Animation animation){
                subTracksContainer.setVisibility(View.VISIBLE);
                isSubTracksShowing = true;
            }

            @Override
            public void onAnimationEnd (Animation animation){
            }

            @Override
            public void onAnimationRepeat (Animation animation){}
        });
    }

    private void hideSubTracks() {
        Animation slideOutRight = AnimationUtils.loadAnimation(PlayerActivity.this, R.anim.slide_out_right);
        subTracksContainer.startAnimation(slideOutRight);

        slideOutRight.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart (Animation animation){}

            @Override
            public void onAnimationEnd (Animation animation){
                isSubTracksShowing = false;
                subTracksContainer.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat (Animation animation){}
        });
    }



    private void seekbarUpdater() {
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (player != null) {
                    if (player.isPlaying() && !f) {
//                        Log.d("playback544", "run: " + isTime1ButtonClicked + isTime2ButtonClicked);
                        seekbar.setProgress((int) player.getCurrentPosition());
                    }

                    if (player.isPlaying()) {
                        isPlaying = true;
                        playButton.setImageResource(R.drawable.baseline_pause_circle_outline_24);
                    } else {
                        isPlaying = false;
                        playButton.setImageResource(R.drawable.baseline_play_circle_outline_24);
                    }
                }
                long dur = player != null ? player.getDuration() : 0;
                handler.postDelayed(this, dur < 60000 ? 500 : 1000);
            }
        };
    }

    private void controlsToast() {
        removeControlsRunnable();

        controlsRunnable = () -> {
            hideControls();
        };

        handler.postDelayed(controlsRunnable, 5000);
    }

    private void lockScreenToast() {
        removeLockScreenRunnable();

        lockScreenRunnable = () -> {
            hideLockButton();
        };

        handler.postDelayed(lockScreenRunnable, 3000);
    }



    private void onLandscape(){
        Log.d(TAG, "onLandscape: YES");
        View back = findViewById(R.id.back_button);
        View tool_title = findViewById(R.id.toolbar_title);
        View audio = findViewById(R.id.audio_tracks_button);
        View subtitle = findViewById(R.id.sub_tracks_button);
        View decoder = findViewById(R.id.decoder_button);
        View more = findViewById(R.id.expandB);
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

        View rewind_layout = findViewById(R.id.rewind_layout);
        View rewind_image = findViewById(R.id.rewind_image);
        View rewind_text = findViewById(R.id.rewind_text);

        View forward_layout = findViewById(R.id.forward_layout);
        View forward_image = findViewById(R.id.forward_image);
        View forward_text = findViewById(R.id.forward_text);

        View speedLayout = findViewById(R.id.speedLayout);

        setMargin(back, 0, 20); // 7

        setMargin(tool_title, 0, 25); // 5
        setMargin(tool_title, 1, 35); // 10
        setConstraint(tool_title, ConstraintSet.END, audio, ConstraintSet.START);

        audio.setVisibility(View.VISIBLE);
        subtitle.setVisibility(View.VISIBLE);

        setMargin(more, 1, 23); // 8

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

        setSize(rewind_layout, 0, 230);
        setSize(rewind_layout, 1, ViewGroup.LayoutParams.MATCH_PARENT);
        setMargin(rewind_image, 3, -2);
        rewind_layout.setScaleY(1.2f);
        rewind_image.setScaleY(1.0f / 1.2f);
        rewind_text.setScaleY(1.0f / 1.2f);

        setSize(forward_layout, 0, 230);
        setSize(forward_layout, 1, ViewGroup.LayoutParams.MATCH_PARENT);
        setMargin(forward_image, 3, -2);
        forward_layout.setScaleY(1.2f);
        forward_image.setScaleY(1.0f / 1.2f);
        forward_text.setScaleY(1.0f / 1.2f);

        setSize(speedLayout, 0, 500);
    }

    private void onPortrait(){
        Log.d(TAG, "onPortrait: YES");
        View back = findViewById(R.id.back_button);
        View tool_title = findViewById(R.id.toolbar_title);
        View audio = findViewById(R.id.audio_tracks_button);
        View subtitle = findViewById(R.id.sub_tracks_button);
        View decoder = findViewById(R.id.decoder_button);
        View more = findViewById(R.id.expandB);
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

        View rewind_layout = findViewById(R.id.rewind_layout);
        View rewind_image = findViewById(R.id.rewind_image);
        View rewind_text = findViewById(R.id.rewind_text);

        View forward_layout = findViewById(R.id.forward_layout);
        View forward_image = findViewById(R.id.forward_image);
        View forward_text = findViewById(R.id.forward_text);

        View speedLayout = findViewById(R.id.speedLayout);

        setMargin(back, 0, 13);

        setMargin(tool_title, 0, 20);
        setMargin(tool_title, 1, 25);
        setConstraint(tool_title, ConstraintSet.END, decoder, ConstraintSet.START);

        audio.setVisibility(View.GONE);
        subtitle.setVisibility(View.GONE);

        setMargin(more, 1, 15);

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

        setSize(rewind_layout, 0, 130);
        setSize(rewind_layout, 1, 380);
        setMargin(rewind_image, 3, -3);
        rewind_layout.setScaleY(1.2f);
        rewind_image.setScaleY(1.0f / 1.2f);
        rewind_text.setScaleY(1.0f / 1.2f);

        setSize(forward_layout, 0, 130);
        setSize(forward_layout, 1, 380);
        setMargin(forward_image, 3, -3);
        forward_layout.setScaleY(1.2f);
        forward_image.setScaleY(1.0f / 1.2f);
        forward_text.setScaleY(1.0f / 1.2f);

        setSize(speedLayout, 0, -1);
    }

    private void setSize(View view, int side, int dp){
        int px;

        if (dp == ViewGroup.LayoutParams.MATCH_PARENT || dp == ViewGroup.LayoutParams.WRAP_CONTENT) {
            px = dp; // Keep same for these sizes
        }
        else {
            px = DpToPixel(dp, this);  // Convert dp to pixels
        }

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
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

        if (layoutParams instanceof ConstraintLayout.LayoutParams) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) layoutParams;

            if(side == 0) {
                params.setMarginStart(px);
            }
            else if(side == 1) {
                params.setMarginEnd(px);
            }
            else if (side == 2) {
                params.setMargins(params.leftMargin, px, params.rightMargin, params.bottomMargin);
            }
            else if (side == 3) {
                params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, px);
            }
            view.setLayoutParams(params);
        }

        else if (layoutParams instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layoutParams;

            if(side == 0) {
                params.setMarginStart(px);
            }
            else if(side == 1) {
                params.setMarginEnd(px);
            }
            else if (side == 2) {
                params.setMargins(params.leftMargin, px, params.rightMargin, params.bottomMargin);
            }
            else if (side == 3) {
                params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, px);
            }
            view.setLayoutParams(params);
        }
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

            playerPrefsEditor.putFloat("lastTime : " + mediaItem.requestMetadata.mediaUri, ((float) player.getCurrentPosition()));
            playerPrefsEditor.apply();

            if (player.isPlaying() && !isBackgroundPlay) {
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

            if (isPlay && !isBackgroundPlay) {
                playMedia();
                isPlay = false;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        handler.removeCallbacks(updateSeekBar);

        if (isBound) {
            unbindService(connection);
            isBound = false;
        }

        if (isFinishing() && !isBackgroundPlay) {
            if (player != null) {
                player.stop();
                Log.d("finish", "onFinish: YES");
            }
        }
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateScreenDimension();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d("Orientation", "Changed to Landscape");
            onLandscape();
        }
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d("Orientation", "Changed to Portrait");
            onPortrait();
        }

        Log.d(TAG, "onConfigurationChanged: " + getResources().getDisplayMetrics().heightPixels + " " + getResources().getDisplayMetrics().widthPixels);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            float maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int incrementValue = (int) ((5.0 / 150) * maxVolume);
            incrementValue = Math.max(1, incrementValue);

            int direction = keyCode == KeyEvent.KEYCODE_VOLUME_UP ? incrementValue : -incrementValue;
            adjustVolume(direction);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private long videoDuration;
    private float scrollSensitivity = 50f;
    private long totalScrollDistance = 0;

    private void performSeekByTouch(int incrementValue, boolean fromUser) {
        int seekValue = (int) player.getCurrentPosition() + incrementValue;
        player.seekTo(seekValue);
        seekbar.setProgress(seekValue);

        seekCurrentTime = (int) player.getCurrentPosition();
        seekCurrentTimeText.setText(MillisToTime(seekCurrentTime));

        seekTime += incrementValue > 0 ? 1 : -1;

        String sign = seekTime >= 0 ? "+" : "-";
        seekTimeText.setText(sign + SecToMin(Math.abs(seekTime)));
    }


    private void adjustVolume(int incrementValue) {
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume + incrementValue, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

        volumeText.setText(String.valueOf(getCurrentVolumeLevelText()));
        hideLayouts(brightnessLayout);
        showCustomToast(volumeLayout, 800);
    }

    private void adjustBrightness(float incrementValue) {
        // Get current window attributes
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();

        // Calculating new brightness, ensuring it's between 0 and 1
        layoutParams.screenBrightness = Math.max(0, Math.min(layoutParams.screenBrightness + incrementValue, 1));

        // Applying new brightness
        getWindow().setAttributes(layoutParams);

        brightnessText.setText(String.valueOf(getCurrentBrightnessLevelText()));
        hideLayouts(volumeLayout);
        showCustomToast(brightnessLayout, 800);
    }

    private void hideLayouts(View... layouts) {
        for (View layout : layouts) {
            if (layout.getVisibility() == View.VISIBLE) {
                layout.setVisibility(View.GONE);
            }
        }
    }

    private int getCurrentVolumeLevelText() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        return (int) ((currentVolume / (float) maxVolume) * 150);
    }

    private int getCurrentBrightnessLevelText() {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        return (int) (layoutParams.screenBrightness * 30);
    }


    private void playMedia() {
        requestAudioFocus();
        player.play();
        wasPlaying_Focus = false;
    }

    private void requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !hasAudioFocus) {
            audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setOnAudioFocusChangeListener(focusChange -> {
                        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                            // pause playback, if other app starts playing
                            if (player.isPlaying()) {
                                player.pause();
                            }
                            hasAudioFocus = false;
                            wasPlaying_Focus = false;
                        }
                        else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                            if (player.isPlaying()){
                                player.pause();
                                wasPlaying_Focus = true;
                            }

                        }
                        else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
//                            player.setVolume(1.0f);
                            if (wasPlaying_Focus) {
                                player.play();
                                wasPlaying_Focus = false;
                                hasAudioFocus = true;
                            }
                        }
                    })
                    .build();

            int result = audioManager.requestAudioFocus(focusRequest);
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Log.d("AudioFocus", "Focus not granted, do not start media");
            }
            else {
                hasAudioFocus = true;
            }
        }
    }


    private Runnable toastRunnable;

    private void showCustomToast(View toastView, long duration) {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        if (toastView.getVisibility() == View.GONE) {
            toastView.setVisibility(View.VISIBLE);
            toastView.startAnimation(fadeIn);
        }

        // Removing any existing callbacks to prevent the past callbacks interrupting present callbacks.
        if (toastRunnable != null) {
            handler.removeCallbacks(toastRunnable);
        }

        toastRunnable = () -> {
            toastView.startAnimation(fadeOut);

            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    toastView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        };

        handler.postDelayed(toastRunnable, duration);
    }


    private Runnable rewindTextRunnable, rewindFadeOutRunnable, forwardTextRunnable, forwardFadeOutRunnable;
    private int tapCount = 0;

    private void showRewindLayout(View layout, TextView timeText) {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        tapCount++;

        String time = String.valueOf((doubleTapSeekTime / 1000) * tapCount);
        timeText.setText(time + " sec");

        layout.setPressed(true);
        layout.postDelayed(() -> layout.setPressed(false), 300);

        if (layout.getVisibility() == View.GONE) {
            layout.setVisibility(View.VISIBLE);
            layout.startAnimation(fadeIn);
        }

        // Resetting the timer of handler
        if (rewindTextRunnable != null) {
            handler.removeCallbacks(rewindTextRunnable);
        }


        rewindTextRunnable = () -> {
            tapCount = 0;
        };

        handler.postDelayed(rewindTextRunnable, 700);


        // Resetting the timer of handler
        if (rewindFadeOutRunnable != null) {
            handler.removeCallbacks(rewindFadeOutRunnable);
        }

        rewindFadeOutRunnable = () -> {
            layout.startAnimation(fadeOut);

            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    layout.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

        };

        handler.postDelayed(rewindFadeOutRunnable, 700);
    }

    private void showForwardLayout(View layout, TextView timeText) {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        tapCount++;

        String time = String.valueOf((doubleTapSeekTime / 1000) * tapCount);
        timeText.setText(time + " sec");

        layout.setPressed(true);
        layout.postDelayed(() -> layout.setPressed(false), 300);

        if (layout.getVisibility() == View.GONE) {
            layout.setVisibility(View.VISIBLE);
            layout.startAnimation(fadeIn);
        }

        // Resetting the timer of handler
        if (forwardTextRunnable != null) {
            handler.removeCallbacks(forwardTextRunnable);
        }


        forwardTextRunnable = () -> {
            tapCount = 0;
        };

        handler.postDelayed(forwardTextRunnable, 700);


        // Resetting the timer of handler
        if (forwardFadeOutRunnable != null) {
            handler.removeCallbacks(forwardFadeOutRunnable);
        }

        forwardFadeOutRunnable = () -> {
            layout.startAnimation(fadeOut);

            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    layout.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

        };

        handler.postDelayed(forwardFadeOutRunnable, 700);
    }

    private void Fullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(lp);
        }

    getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    );

        // Setting status bar color same as toolbar
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.toolbar_background_player_transparent));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.toolbar_background_player_transparent));
    }

    private void notFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(lp);
        }

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        // Setting status bar color same as toolbar
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.toolbar_background_player_black));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.toolbar_background_player_black));
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @OptIn(markerClass = UnstableApi.class)
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            PlayerService playerService = binder.getService();
            player = PlayerService.getPlayer();
            Log.d("service", "onServiceConnected: YES: " + player);

            if (player != null) {
                manager = isVideoFile ? MediaRepository.getInstance().getVideoPlaylistManager() : MediaRepository.getInstance().getAudioPlaylistManager();
                manager.setCurrentIndex(currentIndex);
                initializePlayer(MediaItem.fromUri(media_path), true);
                Log.d("service", "isPlayer: YES");
            }
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    private void updateScreenDimension() {
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        halfWidth = screenWidth / 2;
        Q3_Width = screenWidth / 3;
        Q4_Width = screenWidth / 4;

        Log.d(TAG, "updateScreenDimension: " + screenWidth);
    }

    public String MillisToTime(long millis) {
        long hours = (millis / (1000 * 60 * 60)) % 24;
        long minutes = (millis / (1000 * 60)) % 60;
        long seconds = (millis / 1000) % 60;

        if (hours >= 1) {
            return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
    }

    public String SecToMin(int totalSeconds) {
        int min = totalSeconds / 60;
        int sec = totalSeconds % 60;

        return String.format(Locale.ROOT, "%02d:%02d", min, sec);
    }

    public int DpToPixel(float dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}