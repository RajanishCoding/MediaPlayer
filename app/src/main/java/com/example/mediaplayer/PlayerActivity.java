package com.example.mediaplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
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
import androidx.media3.common.text.CueGroup;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.TrackGroupArray;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.CaptionStyleCompat;
import androidx.media3.ui.PlayerView;
import androidx.media3.ui.SubtitleView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.mediaplayer.Extra.AnimationLibs;
import com.example.mediaplayer.Extra.MediaRepository;
import com.example.mediaplayer.Extra.PlaylistManager;
import com.example.mediaplayer.Tracks.AudioTracks;
import com.example.mediaplayer.Tracks.AudioTracksAdapter;
import com.example.mediaplayer.Tracks.SubTracks;
import com.example.mediaplayer.Tracks.SubTracksAdapter;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@UnstableApi
public class PlayerActivity extends AppCompatActivity {
    private static final String TAG = "tag";
    private ExoPlayer player;  // Make the player instance static

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

    private View playerContainer;
    private View zoomW;
    private PlayerView playerView;
    private SubtitleView subtitleView;

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
    private boolean isOrientationChangedOnReady;
    private boolean isBackgroundPlay;

    private SharedPreferences playerPrefs;
    private SharedPreferences.Editor playerPrefsEditor;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;

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
    private float ScrollX_Speed = 1;
    private float ScrollY1_Speed = 1;
    private float ScrollY2_Speed = 1;
    private float currentScale = 1f;
    private float scaleStepAccumulator = 0f;
    private final float MIN_SCALE = .8f;
    private final float MAX_SCALE = 2f;

    private float mScaleFactor = 1.0f;
    private float mTranslationX = 0f;
    private float mTranslationY = 0f;
    private float mMinScale = 1.0f;
    private float mMaxScale = 5.0f; // Adjust max zoom level as needed
    private float mZoomSpeedMultiplier = 0.05f; // Adjust this value to control zoom speed (0.1f to 1.0f)


    private int videoWidth = 0;
    private int videoHeight = 0;
    private int originalWidth = 0;
    private int originalHeight = 0;

    private View volumeLayout;
    private TextView volumeText;
    private View brightnessLayout;
    private TextView brightnessText;
    private View zoomLayout;
    private TextView zoomText;

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
    private boolean isLongPressed_Speed;
    private boolean isLongPressed_Lock;
    private boolean isScrolling;
    private boolean isScalling;
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

    //    Colors for Expand Layout Views
    private ColorStateList expandButtonsTint1;
    private ColorStateList expandButtonsTint0;
    private Drawable expandButtonsDrawable1;
    private Drawable expandButtonsDrawable0;

    // Expand Views Layouts ---->

    private LinearLayout muteExpandB;
    private ImageButton muteExpandImg;
    private boolean isMuted;

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
    private boolean isSpeedLayoutShowing;

    private LinearLayout sleepExpandB;

    private LinearLayout loopExpandB;
    private ImageButton loopExpandImg;
    private int loopValue = 0;

    private LinearLayout shuffleExpandB;
    private ImageButton shuffleExpandImg;
    private boolean isShuffled;

    private LinearLayout infoExpandB;

    private LinearLayout audioExpandB;
    private ImageButton audioExpandImg;
    private boolean isAudioPlay;

    private LinearLayout lockExpandB;

    private LinearLayout rotateExpandB;

    private LinearLayout popupExpandB;


    private enum GestureDirection {NONE, HORIZONTAL, VERTICAL}

    private GestureDirection gestureDirection = GestureDirection.NONE;

    private boolean isPlaying;
    private boolean wasPlaying;
    private boolean isMediaReady;

    private boolean isFirstTimeForCurrentMedia;
    private boolean isBuffuringForCurrentMedia;
    private boolean isOnStateReadyCalled;

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
    private MediaItem oldMediaItem;

    private long lastPlayedTime;
    private float lastPlayedFile;

    private AnimationLibs animationLibs;

    private Player.Listener listener;
    private Player.Listener listener_size;


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

        animationLibs = new AnimationLibs();

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

        playerContainer = findViewById(R.id.playerContainer);
        zoomW = findViewById(R.id.zoomW);
        playerView = findViewById(R.id.player_view);
        subtitleView = findViewById(R.id.customSubtitleView);

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
        zoomLayout = findViewById(R.id.zoom_layout);
        volumeText = findViewById(R.id.volume_text);
        brightnessText = findViewById(R.id.brightness_text);
        zoomText = findViewById(R.id.zoom_text);

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

        // Colors for Expand Layout Views
        expandButtonsTint1 = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.sidebarDark));
        expandButtonsTint0 = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white));
        expandButtonsDrawable1 = ContextCompat.getDrawable(this, R.drawable.shape_expand_icons);
        expandButtonsDrawable0 = ContextCompat.getDrawable(this, R.drawable.shape_transparent);

        // Expand Views Layouts ---->

        speedText_Expand = findViewById(R.id.speed_expand_text);
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

        muteExpandB = findViewById(R.id.mute_expand);
        muteExpandImg = findViewById(R.id.mute_expand_image);

        loopExpandB = findViewById(R.id.loop_expand);
        loopExpandImg = findViewById(R.id.loop_expand_image);

        shuffleExpandB = findViewById(R.id.shuffle_expand);
        shuffleExpandImg = findViewById(R.id.shuffle_expand_image);

        infoExpandB = findViewById(R.id.info_expand);

        audioExpandB = findViewById(R.id.playAudio_expand);
        audioExpandImg = findViewById(R.id.playAudio_expand_image);

        lockExpandB = findViewById(R.id.lock_expand);

        rotateExpandB = findViewById(R.id.rotate_expand);

        popupExpandB = findViewById(R.id.pip_expand);

        videoList = new ArrayList<>();

        audioTracksList = new ArrayList<>();
        subTracksList = new ArrayList<>();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);


        playerPrefs = getSharedPreferences("PlayerPrefs", Context.MODE_PRIVATE);
        playerPrefsEditor = playerPrefs.edit();

        isTime1ButtonClicked = playerPrefs.getBoolean("isTime1Clicked", false);
        isTime2ButtonClicked = playerPrefs.getBoolean("isTime2Clicked", false);

        loopValue = playerPrefs.getInt("loopValue", 0);
        isShuffled = playerPrefs.getBoolean("isShuffle", false);


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


        playerControls_Listeners();

        tracksContainers_Listeners();


        // Expand Views Buttons - Listener ----->
        expandViewsListeners();

        zoomW.post(() -> {
            originalWidth = playerView.getWidth();
            originalHeight = playerView.getHeight();
        });


        zoomW.setOnTouchListener((v, event) -> {
            if (!isScreenLocked) {
                int pointerCount = event.getPointerCount();

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_POINTER_DOWN:
                        isScalling = true; // Block scroll as soon as second finger touches
                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                        isScalling = false;
                        break;
                }

                if (!isAudioTracksShowing && !isSubTracksShowing && !isExpandViewsShowing && !isSpeedLayoutShowing) {
                    if (pointerCount == 1 && !isScalling)
                        gestureDetector.onTouchEvent(event);
                    if (pointerCount == 2 && !isScrolling && !isLongPressed) {
                        scaleGestureDetector.onTouchEvent(event);
                    }
                }

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

                    if (isSpeedLayoutShowing) {
                        hideSpeedLayout();
                        isSpeedLayoutShowing = false;
                    }

                    if (isLongPressed) isLongPressed = false;
                    
                    if (isLongPressed_Speed) {
                        isLongPressed_Speed = false;
                        player.setPlaybackSpeed(PlaybackSpeed);
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

                    if (isVolumeChanging || isBrightnessChanging) {
                        hideCustomToast(isVolumeChanging ? volumeLayout : brightnessLayout);
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
            public boolean onDown(@NonNull MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
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
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                if (!isLongPressed_Speed && !isScrolling) {
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
            public void onLongPress(@NonNull MotionEvent e) {
                if (isPlaying && !isScrolling && !isScalling) {
                    isLongPressed = true;
                    hideLayouts(volumeLayout, brightnessLayout, zoomLayout);

                    if (e.getX() < halfWidth) {
                        isLongPressed_Lock = true;
                        if (!isScreenLocked) {
                            isScreenLocked = true;
                            hideControls();
                            removeControlsRunnable();
                            showLockButton();
                            lockScreenToast();
                        }
                        else {
                            isScreenLocked = false;
                            hideLockButton();
                            removeLockScreenRunnable();
                        }
                    }
                    else {
                        isLongPressed_Speed = true;
                        showCustomToast(speedToastLayout);
                        player.setPlaybackSpeed(2);
                    }
                }
            }

            @Override
            public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
                Log.d("Scrolling", "onScroll: "+ gestureDirection.name() + "  " + isSeeking + " - " + isScalling);

                if (gestureDirection == GestureDirection.NONE && !isScalling && !isLongPressed_Speed) {
                    isScrolling = true;

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

                        // Locking as horizontal gesture (seeking)
                        gestureDirection = GestureDirection.HORIZONTAL;
                    }
                    else {
                        // Locking as vertical gesture (volume/brightness)
                        gestureDirection = GestureDirection.VERTICAL;
                    }
                }

                if (gestureDirection == GestureDirection.HORIZONTAL) {
                    isSeeking = true;
                    totalScrollX += distanceX;

                    if (Math.abs(totalScrollX) > 20) {
                        if (totalScrollX > 0) {
                            performSeekByTouch(-500, false);
                        } else {
                            performSeekByTouch(500, false);
                        }
                        totalScrollX = 0;
                    }
                }
                else if (gestureDirection == GestureDirection.VERTICAL) {
                    if (e1.getX() < halfWidth) {
                        isBrightnessChanging = true;
                        totalScrollY1 += distanceY;

                        if (Math.abs(totalScrollY1) > 30) {
                            if (totalScrollY1 > 0) {
                                // Scrolled on left, increase volume
                                adjustBrightness(0.02f);
                            } else {
                                // Scrolled on right, decrease volume
                                adjustBrightness(-0.02f);
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

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                if (!isScalling) return false;

                float rawScaleFactor = detector.getScaleFactor();
                float scaleFactor = (float) Math.pow(rawScaleFactor, .5);

                float newScale = currentScale * scaleFactor;
                currentScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, newScale));

                zoomW.setScaleX(currentScale);
                zoomW.setScaleY(currentScale);

                int zoomPercent = (int) (currentScale * 100);
                zoomText.setText(zoomPercent + "%");

                Log.d("scaleD", "rawScaleFactor: " + rawScaleFactor + ", scaleFactor: " + scaleFactor + ", newScale: " + newScale + ", currentScale: " + currentScale);

                return super.onScale(detector);
            }

            @Override
            public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
                isScalling = true;
                showCustomToast(zoomLayout);
                if (zoomW.getWidth() > 0 && zoomW.getHeight() > 0) {
                    zoomW.setPivotX(zoomW.getWidth() / 2f);
                    zoomW.setPivotY(zoomW.getHeight() / 2f);
                }
                return true;
            }

            @Override
            public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
                isScalling = false;
//                zoomLayout.setVisibility(View.GONE);
                zoomLayout.animate()
                        .alpha(0f)
                        .setDuration(150)
                        .withEndAction(() -> zoomLayout.setVisibility(View.GONE))
                        .start();
            }
        });
    }


    private float applyCropMode() {
        int viewWidth = zoomW.getWidth();
        int viewHeight = zoomW.getHeight();

        float viewAspectRatio = (float) viewWidth / viewHeight;
        float videoAspectRatio = (float) videoWidth / videoHeight;

        float scale = 1f;

        if (videoAspectRatio > viewAspectRatio) {
            // Video is wider than view → crop sides
            scale = videoAspectRatio / viewAspectRatio;
        }
        else {
            // Video is taller than view → crop top/bottom
            scale = viewAspectRatio / videoAspectRatio;
        }

        zoomW.setPivotX(viewWidth / 2f);
        zoomW.setPivotY(viewHeight / 2f);

        zoomW.setScaleX(scale);
        zoomW.setScaleY(scale);

        return scale;
    }

    private void applyResizeFitMode() {
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT); // Always FIT

        int containerW = playerView.getWidth();
        int containerH = playerView.getHeight();

        Format format = player.getVideoFormat();
        if (format == null || containerW == 0 || containerH == 0) return;

        float videoW = format.width;
        float videoH = format.height;

        float videoAspect = videoW / videoH;
        float containerAspect = (float) containerW / containerH;

        int newWidth, newHeight;

        if (videoAspect > containerAspect) {
            // Fit width → letterbox top/bottom
            newWidth = containerW;
            newHeight = (int) (containerW / videoAspect);
        } else {
            // Fit height → letterbox sides
            newHeight = containerH;
            newWidth = (int) (containerH * videoAspect);
        }

        ViewGroup.LayoutParams params = playerView.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        playerView.setLayoutParams(params);
    }

    private void onStateReady() {
        Log.d("heibeibvi", "isfirst: " + isFirstTimeForCurrentMedia);

        if (isFirstTimeForCurrentMedia) {
            lastPlayedTime = playerPrefs.getLong("lastTime : " + mediaItem.requestMetadata.mediaUri, 0);
            player.seekTo(lastPlayedTime);

            buffer_view.setVisibility(View.GONE);
            isBuffuringForCurrentMedia = false;

            seekbar.setMax((int) player.getDuration());
            seekbar.setProgress((int) player.getCurrentPosition());
            time1.setText(isTime1ButtonClicked ? "-" + MillisToTime(player.getDuration() - player.getCurrentPosition()) : MillisToTime(player.getCurrentPosition()));
            time2.setText(isTime2ButtonClicked ? "-" + MillisToTime(player.getDuration() - player.getCurrentPosition()) : MillisToTime(player.getDuration()));
            isFirstTimeForCurrentMedia = false;
            controlsToast();
        }
    }

    private void updatePlayerUI(boolean isNamePresent) {
        String name = isNamePresent ? media_name : String.valueOf(mediaItem.mediaMetadata.title);
        ToolbarText.setText(name);
        playButton.setImageResource(R.drawable.baseline_play_circle_outline_24);
        buffer_view.setVisibility(View.VISIBLE);
        isBuffuringForCurrentMedia = true;
        isFirstTimeForCurrentMedia = true;

        Log.d("mediaitem56", "initializePlayer: " + player.getMediaItemCount() + "  " + player.getCurrentMediaItemIndex());
        Log.d("heibeibvi", "updatePlayerUI: ");

        if (isNamePresent) {
            playMedia();
        }

        updateExpandViews();

        isBackgroundPlay = !isVideoFile;

        Intent intent = new Intent("BG_PLAY_STATUS");
        intent.putExtra("isBGPlay", isBackgroundPlay);
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
    }

    private void updateExpandViews() {
        if (loopValue == 0) {
            loopExpandImg.setImageResource(R.drawable.round_repeat);
            setExpandViewColors(loopExpandImg, expandButtonsTint0, expandButtonsDrawable0);
        }
        else if (loopValue == 1) {
            loopExpandImg.setImageResource(R.drawable.round_repeat_one);
            setExpandViewColors(loopExpandImg, expandButtonsTint1, expandButtonsDrawable1);
        }
        else {
            loopExpandImg.setImageResource(R.drawable.round_repeat);
            setExpandViewColors(loopExpandImg, expandButtonsTint1, expandButtonsDrawable1);
        }

        if (isShuffled) {
            setExpandViewColors(shuffleExpandImg, expandButtonsTint1, expandButtonsDrawable1);
        }
        else {
            setExpandViewColors(shuffleExpandImg, expandButtonsTint0, expandButtonsDrawable0);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initializePlayer() {
        Log.d("TAG", "initializePlayer: " + player);

        List<MediaItem> mediaItemList = isVideoFile ? MediaRepository.getInstance().getVideoPlaylist() : MediaRepository.getInstance().getAudioPlaylist();
        mediaItem = mediaItemList.get(currentIndex);
        player.setMediaItems(mediaItemList, currentIndex, 0);
        player.prepare();

        player.setRepeatMode(loopValue);
        player.setShuffleModeEnabled(isShuffled);

        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        playerView.getSubtitleView().setVisibility(View.GONE);
        setSubtitleViewStyles();

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = playerPrefs.getFloat("Brightness", 0f);
        getWindow().setAttributes(layoutParams);

        Log.d("inittt", "init: " + playerPrefs.getFloat("Brightness", 0f));
    }


    private final ServiceConnection connection = new ServiceConnection() {
        @OptIn(markerClass = UnstableApi.class)
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            PlayerService playerService = binder.getService();
            player = playerService.getPlayer();
            Log.d("service", "onServiceConnected: YES: " + player);

            if (player != null) {
                player_Listeners();
                initializePlayer();
                updatePlayerUI(true);
                Log.d("service", "isPlayer: YES");
            }
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    private void player_Listeners() {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    Log.d("statePlay", " Ready ");
                    onStateReady();
                }

                if (playbackState == Player.STATE_ENDED) {
                    Log.d("statePlay", " Ended " + playerPrefs.getLong("lastTime : " + mediaItem.requestMetadata.mediaUri, 0));                }

                if (playbackState == Player.STATE_BUFFERING) {
                    if (isBuffuringForCurrentMedia) {
                        buffer_view.setVisibility(View.VISIBLE);
                    }
                    Log.d("statePlay", " Buffering ");
                    Log.d("brightness", "initializePlayer: " + playerPrefs.getFloat("Brightness", 0f));
                }

                if (playbackState == Player.STATE_IDLE) {
                    Log.d("statePlay", " Idle ");
                }
            }

            @Override
            public void onMediaItemTransition(@Nullable MediaItem newMediaItem, int reason) {
                Log.d("heibeibvi", "onMediaItemTransition: ");
                mediaItem = newMediaItem;
                isOrientationChangedOnReady = false;
                updatePlayerUI(false);
                if (player.getPlaybackState() == Player.STATE_READY && isFirstTimeForCurrentMedia) {
                    onStateReady();
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlayingOn) {
                Log.d("statePlay", "isPlaying: " + isPlayingOn);
                if (isPlayingOn) {
                    isPlaying = true;
                    playButton.setImageResource(R.drawable.baseline_pause_circle_outline_24);
                } else {
                    isPlaying = false;
                    playButton.setImageResource(R.drawable.baseline_play_circle_outline_24);
                }
            }

            @Override
            public void onPositionDiscontinuity(@NonNull Player.PositionInfo oldPosition, @NonNull Player.PositionInfo newPosition, int reason) {
                if (reason == Player.DISCONTINUITY_REASON_SEEK) seekbar.setProgress((int) player.getCurrentPosition());

                if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
                    MediaItem oldMediaItem = oldPosition.mediaItem;
                    if (oldMediaItem != null){
                        Log.d("onPosDIs", "onPositionDiscontinuity: " + oldMediaItem.mediaMetadata.title);
                        playerPrefsEditor.putLong("lastTime : " + oldMediaItem.requestMetadata.mediaUri, 0);
                        playerPrefsEditor.apply();
                    }
                }
            }

            @Override
            public void onCues(@NonNull CueGroup cueGroup) {
                subtitleView.setCues(cueGroup.cues);
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Log.d("playbackError", "onPlayerError: " + error);
            }
        });

        player.addListener(new Player.Listener() {
            @Override
            public void onVideoSizeChanged(@NonNull VideoSize size) {
                int width = size.width;
                int height = size.height;

                videoWidth = width;
                videoHeight = height;

                if (!isOrientationChangedOnReady && width != 0 && height != 0) {

                    Log.d("VideoDimensions", "Width: " + width + ", Height: " + height);

                    Log.d("isVideoFile", "listener1: " + isVideoFile);

                    // Rotating logic according to dimensions
                    boolean isLandscapeVideo = height < width;

                    if (isLandscapeVideo) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                    else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

                    Log.d("isVideoFile", "listener2: " + isLandscapeVideo);
                    isOrientationChangedOnReady = true;
                }
            }
        });

//        player.addAnalyticsListener(new AnalyticsListener() {
//            @Override
//            public void onVideoDecoderInitialized(EventTime eventTime, String decoderName, long initializedTimestampMs, long initializationDurationMs) {
//                if (decoderName.contains("hardware")) {
//                    decoderButton.setImageResource(R.drawable.sw); // Software decoder is being used
//                } else {
//                    decoderButton.setImageResource(R.drawable.hw); // Hardware decoder is being used
//                }
//                Log.d("DecoderInfo", "Decoder Name: " + decoderName);
//            }
//        });
    }

    private void playerControls_Listeners() {
        playButton.setOnClickListener(v -> {
            if (player != null) {
                if (!player.isPlaying()) {
                    playMedia();
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
        
        playButton.setOnLongClickListener(v -> {
            playButton.setSelected(!playButton.isSelected());
            return true;
        });

        prevButton.setOnClickListener(v -> {
            playerPrefsEditor.putLong("lastTime : " + mediaItem.requestMetadata.mediaUri, player.getCurrentPosition());
            playerPrefsEditor.apply();
            player.seekToPreviousMediaItem();
        });

        nextButton.setOnClickListener(v -> {
            playerPrefsEditor.putLong("lastTime : " + mediaItem.requestMetadata.mediaUri, player.getCurrentPosition());
            playerPrefsEditor.apply();
            player.seekToNextMediaItem();
        });


        backButton.setOnClickListener(v -> finish());

        expandB.setOnClickListener(v -> {
            Log.d("dgiuhors", "onCreate: jbissg");
            showExpandViewsLayout(75, 300);
            isExpandViewsShowing = true;
            hideControls();
            removeControlsRunnable();
        });

        rotateButton.setOnClickListener(v -> {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                Log.d(TAG, "onRotate: " + "Landscape");
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                Log.d(TAG, "onRotate: " + "Portrait");
            }
            isOrientation = true;
        });

        fitcropButton.setOnClickListener(new View.OnClickListener() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onClick(View v) {
//                playerView.setScaleX(1);
//                playerView.setScaleY(1);

                if (!isFitScreen) {
                    fitcropButton.setImageResource(R.drawable.baseline_fit_screen_24);
//                    playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                    if (zoomW.getWidth() > 0 && zoomW.getHeight() > 0) {
                        zoomW.setPivotX(zoomW.getWidth() / 2f);
                        zoomW.setPivotY(zoomW.getHeight() / 2f);
                    }
                    zoomW.setScaleX(1);
                    zoomW.setScaleY(1);
                    currentScale = 1;

//                    applyResizeFitMode();
                    isFitScreen = true;
                } else {
                    fitcropButton.setImageResource(R.drawable.baseline_crop_din_24);
//                    playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                    currentScale = applyCropMode();
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
            playerPrefsEditor.putBoolean("isTime1Clicked", isTime1ButtonClicked);
            playerPrefsEditor.apply();
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
            playerPrefsEditor.putBoolean("isTime2Clicked", isTime2ButtonClicked);
            playerPrefsEditor.apply();
            Log.d(TAG, "onClickTime2: " + isTime2ButtonClicked);
        });

    }

    private void tracksContainers_Listeners() {
        audioTrackButton.setOnClickListener(v -> showAudioTracks());

        audioTracks_BackButton.setOnClickListener(v -> hideAudioTracks());

        subTrackButton.setOnClickListener(v -> showSubTracks());

        subTracks_BackButton.setOnClickListener(v -> hideSubTracks());
    }


    private void expandViewsListeners() {
        speedExpand_Listeners();
        muteExpand_Listeners();
        loopExpand_Listeners();
        shuffleExpand_Listeners();
        audioExpand_Listeners();
        lockExpand_Listeners();
        rotateExpand_Listeners();
    }

    private void speedExpand_Listeners() {
        speedExpandB.setOnClickListener(v -> {
            showSpeedLayout();
            hideExpandViewsLayout();
            isSpeedLayoutShowing = true;
        });

        sliderSpeed.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                PlaybackSpeed = value;
                if (player != null) player.setPlaybackSpeed(PlaybackSpeed);
                speedText.setText(String.format("%sX", PlaybackSpeed));
            }
        });

        incrSpeed.setOnClickListener(v -> {
            if (PlaybackSpeed < 4) {
                PlaybackSpeed += 0.05F;
                if (player != null) player.setPlaybackSpeed(PlaybackSpeed);
                sliderSpeed.setValue(PlaybackSpeed);
                speedText.setText(String.format(Locale.ROOT, "%.2fX", PlaybackSpeed));
            }
        });

        decrSpeed.setOnClickListener(v -> {
            if (PlaybackSpeed > 0.25) {
                PlaybackSpeed -= 0.05F;
                if (player != null) player.setPlaybackSpeed(PlaybackSpeed);
                sliderSpeed.setValue(PlaybackSpeed);
                speedText.setText(String.format(Locale.ROOT, "%.2fX", PlaybackSpeed));
            }
        });

        Button[] speedButtons = {speed75, speed100, speed125, speed150, speed175, speed200};
        for (Button btn : speedButtons) {
            btn.setOnClickListener(v -> {
                String speedStr = ((Button) v).getText().toString();
                PlaybackSpeed = Float.parseFloat(speedStr);
                if (player != null) player.setPlaybackSpeed(PlaybackSpeed);
                sliderSpeed.setValue(PlaybackSpeed);
                speedText.setText(String.format(Locale.ROOT, "%.2fX", PlaybackSpeed));
            });
        }
    }

    private void muteExpand_Listeners() {
        muteExpandB.setOnClickListener(v -> {
            if (isMuted) {
                setExpandViewColors(muteExpandImg, expandButtonsTint0, expandButtonsDrawable0);
            }
            else {
                setExpandViewColors(muteExpandImg, expandButtonsTint1, expandButtonsDrawable1);
            }
            isMuted = !isMuted;
        });
    }

    private void loopExpand_Listeners() {
        loopExpandB.setOnClickListener(v -> {
            if (loopValue == 0) {
                setExpandViewColors(loopExpandImg, expandButtonsTint1, expandButtonsDrawable1);
                loopValue = 2;
            }
            else if (loopValue == 2) {
                loopExpandImg.setImageResource(R.drawable.round_repeat_one);
                setExpandViewColors(loopExpandImg, expandButtonsTint1, expandButtonsDrawable1);
                loopValue = 1;
            }
            else {
                loopExpandImg.setImageResource(R.drawable.round_repeat);
                setExpandViewColors(loopExpandImg, expandButtonsTint0, expandButtonsDrawable0);
                loopValue = 0;
            }
            player.setRepeatMode(loopValue);
        });
    }

    private void shuffleExpand_Listeners() {
        shuffleExpandB.setOnClickListener(v -> {
            if (isShuffled) {
                setExpandViewColors(shuffleExpandImg, expandButtonsTint0, expandButtonsDrawable0);
            }
            else {
                setExpandViewColors(shuffleExpandImg, expandButtonsTint1, expandButtonsDrawable1);
            }
            isShuffled = !isShuffled;
            player.setShuffleModeEnabled(isShuffled);
        });
    }

    private void audioExpand_Listeners() {
        audioExpandB.setOnClickListener(v -> {
            if (isAudioPlay) {
                setExpandViewColors(audioExpandImg, expandButtonsTint0, expandButtonsDrawable0);
            }
            else {
                setExpandViewColors(audioExpandImg, expandButtonsTint1, expandButtonsDrawable1);
            }
            isAudioPlay = !isAudioPlay;
        });
    }

    private void lockExpand_Listeners() {
        lockExpandB.setOnClickListener(v -> {
            isScreenLocked = true;
            hideControls();
            removeControlsRunnable();
            showLockButton();
            lockScreenToast();
        });
    }

    private void rotateExpand_Listeners() {
        rotateExpandB.setOnClickListener(v -> {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                Log.d(TAG, "onRotate: " + "Landscape");
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                Log.d(TAG, "onRotate: " + "Portrait");
            }
            isOrientation = true;
        });
    }

    private void setExpandViewColors(ImageButton img, ColorStateList clr, Drawable drw) {
        img.setImageTintList(clr);
        img.setBackground(drw);
    }


    private void setSubtitleViewStyles() {
        subtitleView.setApplyEmbeddedStyles(true);           // Ignore subtitle file's styles
        subtitleView.setApplyEmbeddedFontSizes(false);       // Ignore font sizes in subtitle file

        subtitleView.setStyle(
                new CaptionStyleCompat(
                        Color.WHITE,                                // foreground (text color)
                        Color.TRANSPARENT,                          // background
                        Color.TRANSPARENT,                          // window background
                        CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW,   // edge type
                        Color.BLACK,                                // edge color
                        null                                        // typeface
                )
        );

        // Adjust text size (0.05 = 5% of screen height)
        subtitleView.setFractionalTextSize(0.055f);  // 5.5% of screen height
    }

    @OptIn(markerClass = UnstableApi.class)
    private void loadAudioTracks() {
        // Get current tracks and check for selection
        Tracks currentTracks = player.getCurrentTracks();

        new Thread(() -> {
            audioTracksList.add(new AudioTracks(null, -1, null, null, -1, false));

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
            subTracksList.add(new SubTracks(null, -1, null, null, false));

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

    private void showExpandViewsLayout(long delayBetween, long duration) {
        expandScrollView.animate().cancel();
        animationLibs.fadeSlideInRight(expandScrollView, 200, null, null);

        int count = expandView.getChildCount();
        expandView.setVisibility(View.VISIBLE);

        for (int i = 0; i < count; i++) {
            final View view = expandView.getChildAt(i);
            view.animate().cancel();
            view.setAlpha(0f);
            view.setVisibility(View.VISIBLE);

            int j = i;
            view.post(() -> {
                view.animate()
                        .alpha(1f)
                        .setDuration(duration)
                        .setStartDelay(j * delayBetween)
                        .start();
                });
        }
    }

    private void hideExpandViewsLayout() {
        expandScrollView.animate().cancel();
        animationLibs.fadeSlideOutRight(expandScrollView, 200, null, () -> expandScrollView.setVisibility(View.GONE));
    }

    private void showSpeedLayout() {
        animationLibs.fadeSlideInBottom(speedLayout, 300, null, null);
    }

    private void hideSpeedLayout() {
        speedText_Expand.setText(String.format(Locale.ROOT, "%.2fX", PlaybackSpeed));
        animationLibs.fadeSlideOutBottom(speedLayout, 200, null, null);
    }


    private void showControls() {
        isControlsHidden = false;
        isControlsShowing = true;

        animationLibs.slideInBottom(PlaybackControls_Container, 150,
                () -> {
                    isInAnimation = true;
                    surface_click_frag = false;
                    notFullscreen();
                },
                () -> {
                    isInAnimation = false;
                    controlsToast();
                });

        animationLibs.slideInTop(toolbar, 150, null, null);
    }

    private void hideControls() {
        isControlsShowing = false;
        isControlsHidden = true;

        animationLibs.slideOutBottom(PlaybackControls_Container, 150,
                () -> {
                    isOutAnimation = true;
                    Fullscreen();
                },
                () -> {
                    isOutAnimation = false;
                    toolbar.setVisibility(View.GONE);
                    PlaybackControls_Container.setVisibility(View.GONE);
                    surface_click_frag = true;
                });

        animationLibs.slideOutTop(toolbar, 150, null, null);
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
        hideControls();
        removeControlsRunnable();
        animationLibs.slideInRight(audioTracksContainer, 250, () -> isAudioTracksShowing = true, null);
    }

    private void hideAudioTracks() {
        animationLibs.slideOutRight(audioTracksContainer, 250, null,
                () -> {
                    isAudioTracksShowing = false;
                    audioTracksContainer.setVisibility(View.INVISIBLE);
                });
    }

    private void showSubTracks() {
        hideControls();
        removeControlsRunnable();
        animationLibs.slideInRight(subTracksContainer, 250, () -> isSubTracksShowing = true, null);
    }

    private void hideSubTracks() {
        animationLibs.slideOutRight(subTracksContainer, 250, null,
                () -> {
                    isSubTracksShowing = false;
                    subTracksContainer.setVisibility(View.INVISIBLE);
                });
    }



    private void seekbarUpdater() {
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (player != null) {
                    if (player.isPlaying() && !f) {
                        seekbar.setProgress((int) player.getCurrentPosition());
                    }
                }
                long dur = player != null ? player.getDuration() : 0;
                handler.postDelayed(this, dur < 60000 ? 500 : 1000);
            }
        };
        handler.post(updateSeekBar);
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

        View tool_title = findViewById(R.id.toolbar_title);
        View audio = findViewById(R.id.audio_tracks_button);
        View subtitle = findViewById(R.id.sub_tracks_button);
        View text1 = findViewById(R.id.time1);
        View text2 = findViewById(R.id.time2);
        View lock = findViewById(R.id.lock);
        View rotate = findViewById(R.id.rotate);
        View prev = findViewById(R.id.prev);
        View play = findViewById(R.id.play);
        View next = findViewById(R.id.next);
        View crop = findViewById(R.id.fit_crop);
        View pip = findViewById(R.id.pip);
        View toolbarLayout = findViewById(R.id.toolbarLayout);
        View full_container = findViewById(R.id.full_container);

        View rewind_layout = findViewById(R.id.rewind_layout);
        View rewind_image = findViewById(R.id.rewind_image);
        View rewind_text = findViewById(R.id.rewind_text);

        View forward_layout = findViewById(R.id.forward_layout);
        View forward_image = findViewById(R.id.forward_image);
        View forward_text = findViewById(R.id.forward_text);

        View speedLayout = findViewById(R.id.speedLayout);

//        setMargin(back, 0, 20); // 7

        setMargin(tool_title, 0, 25); // 5
        setMargin(tool_title, 1, 35); // 10
        setConstraint(tool_title, ConstraintSet.END, audio, ConstraintSet.START);

        audio.setVisibility(View.VISIBLE);
        subtitle.setVisibility(View.VISIBLE);

//        setMargin(more, 1, 23); // 8

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

        setPadding(toolbarLayout, 0, 30);
        setPadding(toolbarLayout, 1, 30);

        setPadding(full_container, 0, 25);
        setPadding(full_container, 1, 25);

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

        View tool_title = findViewById(R.id.toolbar_title);
        View audio = findViewById(R.id.audio_tracks_button);
        View subtitle = findViewById(R.id.sub_tracks_button);
        View decoder = findViewById(R.id.decoder_button);
        View text1 = findViewById(R.id.time1);
        View text2 = findViewById(R.id.time2);
        View lock = findViewById(R.id.lock);
        View rotate = findViewById(R.id.rotate);
        View prev = findViewById(R.id.prev);
        View play = findViewById(R.id.play);
        View next = findViewById(R.id.next);
        View crop = findViewById(R.id.fit_crop);
        View pip = findViewById(R.id.pip);
        View toolbarLayout = findViewById(R.id.toolbarLayout);
        View full_container = findViewById(R.id.full_container);

        View rewind_layout = findViewById(R.id.rewind_layout);
        View rewind_image = findViewById(R.id.rewind_image);
        View rewind_text = findViewById(R.id.rewind_text);

        View forward_layout = findViewById(R.id.forward_layout);
        View forward_image = findViewById(R.id.forward_image);
        View forward_text = findViewById(R.id.forward_text);

        View speedLayout = findViewById(R.id.speedLayout);

//        setMargin(back, 0, 13);

        setMargin(tool_title, 0, 20);
        setMargin(tool_title, 1, 25);
        setConstraint(tool_title, ConstraintSet.END, decoder, ConstraintSet.START);

        audio.setVisibility(View.GONE);
        subtitle.setVisibility(View.GONE);

//        setMargin(more, 1, 15);

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

        setPadding(toolbarLayout, 0, 15);
        setPadding(toolbarLayout, 1, 15);

        setPadding(full_container, 0, 10);
        setPadding(full_container, 1, 10);

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

            playerPrefsEditor.putLong("lastTime : " + mediaItem.requestMetadata.mediaUri, player.getCurrentPosition());
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

        playerPrefsEditor.putInt("loopValue", loopValue);
        playerPrefsEditor.putBoolean("isShuffle", isShuffled);
        playerPrefsEditor.apply();

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
            hideCustomToast(volumeLayout);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


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
        showCustomToast(volumeLayout);
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
        showCustomToast(brightnessLayout);
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
        return (int) (layoutParams.screenBrightness * 50);
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
    private void removeCustomToastRunnable() {
        if (toastRunnable != null) {
            handler.removeCallbacks(toastRunnable);
        }
    }

    private void showCustomToast(View toastView) {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        toastView.clearAnimation();
        toastView.setVisibility(View.VISIBLE);
        toastView.animate()
                .alpha(1f)
                .setDuration(150)
                .start();
    }

    private void hideCustomToast(View toastView) {
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        removeCustomToastRunnable();

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

        handler.postDelayed(toastRunnable, 400);
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