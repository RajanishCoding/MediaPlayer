package com.example.mediaplayer;
import android.Manifest;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
//import androidx.media3.exoplayer.Player;

import com.example.mediaplayer.Audio.Audio;
import com.example.mediaplayer.Audio.AudioFragment;
import com.example.mediaplayer.Extra.MediaRepository;
import com.example.mediaplayer.Extra.MyMediaItem;
import com.example.mediaplayer.Settings.SettingsFragment;
import com.example.mediaplayer.Video.Video;
import com.example.mediaplayer.Video.VideoFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FilesListActivity extends AppCompatActivity {

    private ArrayList<String> FilesName;
    private static ArrayList<String> FilesPath;
    private static ArrayList<String> AudioFilesPath;
    private static ArrayList<String> VideoFilesPath;
    private ArrayList<String> FilesDuration;

    private List<Audio> audioList = new ArrayList<>();
    private List<Video> videoList = new ArrayList<>();

    private ConstraintLayout StorageRationaleLayout;
    private MaterialButton storageRationale_AccessButton;
    private TextView storageRationaleText;
    private boolean NotificationEnabled;

    private MaterialButton pickFileButton;

    private boolean readStorage;
    private boolean writeStorage;
    private boolean isStorageAccessed;
    private boolean isAudioStorageAccessed;
    private boolean isStoragePermanentlyDenied;
    private boolean isAudioStoragePermanentlyDenied;
    private boolean isPermissionsAsked;
    private boolean isAudioPermissionsAsked;
    private boolean isSetAdapterNeeded;
    private String TAG = "TAG";
    public boolean isInsert;

    private boolean isNotificationPermissionAsked;

    private boolean isStart = true;

    private Fragment activeFragment;
    private BottomNavigationView bottomNavigationView;

    private ImageButton lastPlay_Button;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private SharedPreferences listPrefs;
    private SharedPreferences.Editor listPrefsEditor;

    private SharedPreferences prefs;
    private SharedPreferences.Editor prefsEditor;

    private View menu_Container;
    private ImageButton menu_Button;
    private ImageButton search_Button;
    private Switch mode_Switch;

    private String mediaName;
    private String mediaPath;

    private int id;

    public static boolean isThemeChanged = false;

    private static final String[] REQUIRED_PERMISSIONS_A10B = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final String[] REQUIRED_PERMISSIONS_A1112 = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private static final String[] REQUIRED_PERMISSIONS_A13A = {
            Manifest.permission.READ_MEDIA_VIDEO
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files_list);

        bottomNavigationView = findViewById(R.id.bottomNavBar);

        FilesName = new ArrayList<>();
        FilesPath = new ArrayList<>();
        AudioFilesPath = new ArrayList<>();
        VideoFilesPath = new ArrayList<>();
        FilesDuration = new ArrayList<>();

        StorageRationaleLayout = findViewById(R.id.StorageAccessLayout);
        storageRationaleText = findViewById(R.id.access_content);
        storageRationale_AccessButton = findViewById(R.id.allowAccess);

        pickFileButton = findViewById(R.id.pickFile);

        menu_Button = findViewById(R.id.moremenu_button);
        search_Button = findViewById(R.id.search_button);

        sharedPreferences = getSharedPreferences("PlayerPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        listPrefs = getSharedPreferences("ListSetting", Context.MODE_PRIVATE);

        prefs = getSharedPreferences("PermissionPrefs", MODE_PRIVATE);
        prefsEditor = prefs.edit();

        isPermissionsAsked = prefs.getBoolean("isPermissionsAsked", false);
        isAudioPermissionsAsked = prefs.getBoolean("isAudioPermissionsAsked", false);
        isNotificationPermissionAsked = prefs.getBoolean("isNotificationPermissionAsked", false);

        setThemeMode();

        pickFileButton.setOnClickListener(v -> openMediaPicker());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_video) {
                id = item.getItemId();
                Log.d(TAG, "onStart: " + "yy");

                if (!isStorageAccessed) {
                    unloadFragment(new VideoFragment()); unloadFragment(new SettingsFragment());
                    updateRationaleLayout(isStoragePermanentlyDenied, false);
                    return true;
                }

                StorageRationaleLayout.setVisibility(View.GONE);
                loadFragment(new VideoFragment());
                CheckAndAsk_NotificationAccess(); // For Notification Access Android 13 and above
                return true;
            }

            else if (item.getItemId() == R.id.nav_audio) {
                id = item.getItemId();

                if (!checkVersion13A()) isAudioStorageAccessed = isStorageAccessed;

                if (!isAudioStorageAccessed) {
                    unloadFragment(new VideoFragment()); unloadFragment(new SettingsFragment());
                    updateRationaleLayout(isAudioStoragePermanentlyDenied, true);
                    return true;
                }

                StorageRationaleLayout.setVisibility(View.GONE);
                loadFragment(new AudioFragment());
                CheckAndAsk_NotificationAccess(); // For Notification Access Android 13 and above
                return true;
            }

            else {
                id = item.getItemId();
                StorageRationaleLayout.setVisibility(View.GONE);
                loadFragment(new SettingsFragment());
                return true;
            }
        });

        if (isThemeChanged) {  // If recreated due to theme change
            bottomNavigationView.post(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    bottomNavigationView.setSelectedItemId(R.id.nav_settings);
                    Log.d("ThemeChange", "onCreate: ");
                }
            });
            Log.d("ThemeChange", "onCreate: Theme changed, selecting Video in NavBar " + isThemeChanged);
            isThemeChanged = false; // Reset flag
        }
    }

    private void setThemeMode() {
        SharedPreferences sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        int savedMode = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        int currentMode = AppCompatDelegate.getDefaultNightMode();

        if (savedMode != currentMode) {  // Only set if its different
            AppCompatDelegate.setDefaultNightMode(savedMode);
            Log.d("niced", "onViewCreated: YES " + savedMode + " , " + currentMode + " , " + AppCompatDelegate.getDefaultNightMode());
        }

//       -100 - unspecified
//        -1 - follow
//        1 - Light
//        2 - Night
    }


    ActivityResultLauncher<Intent> mediaPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @OptIn(markerClass = UnstableApi.class)
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
//                        getPersistentAccess(uri);

                            Audio audio = getAudioFromUri(uri);
                            audioList.add(audio);
                            setAudioPlaylist();

                            Intent intent = new Intent(FilesListActivity.this, PlayerActivity.class);
                            intent.putExtra("Name", audio.getName());
                            intent.putExtra("Path", audio.getPath());
                            intent.putExtra("isVideo", audio.isVideo());
                            intent.putExtra("currentIndex", 0);
                            intent.putExtra("fromPickFile", true);
                            Log.d("isVideoFile", "onClick: " + audio.isVideo());
                            FilesListActivity.this.startActivity(intent);
                        }
                    }
                }
            });

    ActivityResultLauncher<PickVisualMediaRequest> videoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(),
            uri -> {
                if (uri != null) {
//                    getPersistentAccess(uri);
                }
            }
    );

    ActivityResultLauncher<String> requestPermissionLauncher_Notification =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    isNotificationPermissionAsked = NotificationEnabled = isGranted;
                }
            });

    ActivityResultLauncher<String[]> requestPermissionsLauncher_Storage =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> permissions) {
                    isStorageAccessed = checkStorageAccess(false);

                    boolean isPermission = isPermissionsAsked;
                    isPermissionsAsked = true;
                    prefsEditor.putBoolean("isPermissionsAsked", true).apply();

                    if (isStorageAccessed) {
                        StorageRationaleLayout.setVisibility(View.GONE);
                        loadFragment(id == R.id.nav_audio ? new AudioFragment() : new VideoFragment());
                        CheckAndAsk_NotificationAccess(); // For Notification Access Android 13 and above
                    }
                    else {
                        isStoragePermanentlyDenied = isPermission && checkPermanentlyDenied(permissions.keySet().toArray(new String[0]));
                        updateRationaleLayout(isStoragePermanentlyDenied, false);
                    }
                }
            });

    ActivityResultLauncher<String> requestPermissionLauncher_AudioStorage =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    isAudioStorageAccessed = isGranted;

                    boolean isPermission = isAudioPermissionsAsked;
                    isAudioPermissionsAsked = true;
                    prefsEditor.putBoolean("isAudioPermissionsAsked", true).apply();

                    if (isAudioStorageAccessed) {
                        StorageRationaleLayout.setVisibility(View.GONE);
                        loadFragment(new AudioFragment());
                        CheckAndAsk_NotificationAccess(); // For Notification Access Android 13 and above
                    }
                    else {
                        isAudioStoragePermanentlyDenied = isPermission && checkPermanentlyDenied(new String[]{Manifest.permission.READ_MEDIA_AUDIO});
                        updateRationaleLayout(isAudioStoragePermanentlyDenied, true);
                    }
                }
            });

    private boolean checkPermanentlyDenied(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    return true;
                }
            }
        }
        return false;
    }


    // Check if permission is Allowed or not
    private boolean checkStorageAccess(boolean isAudio) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        else {
            return isAudio ?
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED :
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            return REQUIRED_PERMISSIONS_A10B;
        }
        else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            return REQUIRED_PERMISSIONS_A1112;
        }
        else {
            return REQUIRED_PERMISSIONS_A13A;
        }
    }

    // Check and Decide for, which permission is needed
    private void CheckAndAsk_StorageAccess(boolean isAudio) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
            CheckAndAsk_StorageAccessA10B();
        else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
            CheckAndAsk_StorageAccessA1112();
        else {
            if (isAudio) CheckAndAsk_AudioStorageAccessA13A();
            else CheckAndAsk_StorageAccessA13A();
        }
    }

    // Check and Asks the permission for android 10 and below
    private void CheckAndAsk_StorageAccessA10B() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            isStorageAccessed = true;
            loadFragment(new VideoFragment());
        }

        else {
            isStorageAccessed = false;
            requestPermissionsLauncher_Storage.launch(REQUIRED_PERMISSIONS_A10B);
        }
    }

    // Check and Asks the permission for android 11 & 12
    private void CheckAndAsk_StorageAccessA1112() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            isStorageAccessed = true;
            loadFragment(new VideoFragment());
        }

        else {
            isStorageAccessed = false;
            requestPermissionsLauncher_Storage.launch(REQUIRED_PERMISSIONS_A1112);
        }
    }

    // Check and Asks the permission for android 13 and above
    private void CheckAndAsk_StorageAccessA13A() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED) {
            isStorageAccessed = true;
            loadFragment(new VideoFragment());
        }

        else {
            isStorageAccessed = false;
            requestPermissionsLauncher_Storage.launch(REQUIRED_PERMISSIONS_A13A);
        }
    }

    // Check and Asks the Audio permission for android 13 and above
    private void CheckAndAsk_AudioStorageAccessA13A() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            isAudioStorageAccessed = true;
            loadFragment(new AudioFragment());
        }

        else {
            isAudioStorageAccessed = false;
            requestPermissionLauncher_AudioStorage.launch(Manifest.permission.READ_MEDIA_AUDIO);
        }
    }

    // Check and Asks the permission for Notification for Android 13 and above
    private void CheckAndAsk_NotificationAccess() {
        if (!isStart) return;
        isStart = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationEnabled = true;
            }

            else if (!isNotificationPermissionAsked && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                NotificationEnabled = false;
                showRationaleDialog_Notification();
            }

            else {
                Log.d(TAG, "CheckAndAsk_NotificationAccess: " + NotificationEnabled);
                NotificationEnabled = false;
                requestPermissionLauncher_Notification.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    // Permission Rationale Dialog
    private void showRationaleDialog_Notification(){
        new AlertDialog.Builder(this)
                .setTitle("Notification Permission")
                .setMessage("Notification permission is required to control the media playback from notification while background play is enabled.\n\n" +
                        "Please enable it in the permissions of the app settings.")
                .setPositiveButton("Allow Access", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // For Notification Access Android 13 and above
                        requestPermissionLauncher_Notification.launch(Manifest.permission.POST_NOTIFICATIONS);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isNotificationPermissionAsked = true;
                        prefsEditor.putBoolean("isNotificationPermissionAsked", true).apply();
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Check if the permission is granted after returning to app
        isStorageAccessed = checkStorageAccess(false);
        isStoragePermanentlyDenied = isPermissionsAsked && checkPermanentlyDenied(getRequiredPermissions());

        if (checkVersion13A()) {
            isAudioStorageAccessed = checkStorageAccess(true);
            isAudioStoragePermanentlyDenied = isAudioPermissionsAsked && checkPermanentlyDenied(new String[]{Manifest.permission.READ_MEDIA_AUDIO});
        }
        else {
            isAudioStorageAccessed = isStorageAccessed;
            isAudioStoragePermanentlyDenied = isStoragePermanentlyDenied;
        }

        bottomNavigationView.post(() -> {
            if (id == 0 || id == R.id.nav_video) {
                bottomNavigationView.setSelectedItemId(R.id.nav_video);
            }
            else if (id == R.id.nav_audio) {
                bottomNavigationView.setSelectedItemId(R.id.nav_audio);
            }
        });
    }

    private void updateRationaleLayout(boolean isStoragePermanentlyDenied, boolean isAudio) {
        StorageRationaleLayout.setVisibility(View.VISIBLE);

        StringBuilder builder = new StringBuilder();

        if (checkVersion13A()) {
            builder.append(getString(isAudio ? R.string.audio_access: R.string.video_access));
        } 
        else {
            builder.append(getString(R.string.storage_access));
        }

        builder.append("\n\n");

        if (isStoragePermanentlyDenied) {
            if (checkVersion13A()) {
                builder.append(getString(isAudio ? R.string.action_settings_audio : R.string.action_settings_video));
            } else {
                builder.append(getString(R.string.action_settings_storage));
            }

            storageRationale_AccessButton.setText("Open Settings");
            storageRationale_AccessButton.setOnClickListener(v -> openSettings());
        }
        else {
            builder.append(getString(R.string.action_allow));
            storageRationale_AccessButton.setText("Allow Access");
            storageRationale_AccessButton.setOnClickListener(v -> CheckAndAsk_StorageAccess(isAudio));
        }

        storageRationaleText.setText(builder.toString());
    }


    private void loadFragment(Fragment fragment) {
        if (fragment == null) return;
        String tag = fragment.getClass().getSimpleName(); // Uses the class name as the tag
        FragmentManager fragmentManager = getSupportFragmentManager();

        // Checking if fragment exists already
        Fragment existingFragment = fragmentManager.findFragmentByTag(tag);

        if (existingFragment != null && existingFragment == activeFragment) return;

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        if (activeFragment != null && activeFragment != existingFragment) {
            transaction.hide(activeFragment);
        }

        if (existingFragment == null) {
            existingFragment = fragment;
            transaction.add(R.id.fragmentContainer, existingFragment, tag);
        }
        else {
            transaction.show(existingFragment);
        }

        activeFragment = existingFragment;
        transaction.commit();
    }

    private void unloadFragment(Fragment fragment) {
        if (fragment == null) return;

        if (!checkVersion13A()) {
            unloadAllFragments();
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        String tag = fragment.getClass().getSimpleName();
        Fragment existingFragment = fragmentManager.findFragmentByTag(tag);

        if (existingFragment != null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(existingFragment);
            transaction.commit();

            if (activeFragment == existingFragment) {
                activeFragment = null;
            }
        }
    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", FilesListActivity.this.getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private boolean checkVersion13A() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
    }

    private void unloadAllFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        List<Fragment> fragments = fragmentManager.getFragments();

        for (Fragment fragment : fragments) {
            if (fragment != null) {
                transaction.remove(fragment);
            }
        }

        transaction.commit();
        activeFragment = null;
    }


    public void openMediaPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {"audio/*", "video/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        mediaPickerLauncher.launch(intent);

//        videoPickerLauncher.launch(new PickVisualMediaRequest.Builder()
//                .setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE)
//                .build());
    }

    private Audio getAudioFromUri(Uri uri) {
        mediaPath = uri.toString();
        String date = String.valueOf(System.currentTimeMillis());

        // Query the specific URI provided by the picker
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                // Get Display Name
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    mediaName = cursor.getString(nameIndex);
                }

                // Actual media path (may return null on Android 10+)
                int dataIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                if (dataIndex != -1) {
                    String realPath = cursor.getString(dataIndex);
                    if (realPath != null) mediaPath = realPath;
                }
            }
        }

        return new Audio(uri.toString(), mediaName, mediaPath, date, false);
    }

    private Video getVideoFromPickedUri(Uri uri) {
        mediaPath = uri.toString();
        String date = String.valueOf(System.currentTimeMillis());

        // Query the specific URI provided by the picker
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                // Get Display Name
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    mediaName = cursor.getString(nameIndex);
                }

                // Actual media path (may return null on Android 10+)
                int dataIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                if (dataIndex != -1) {
                    String realPath = cursor.getString(dataIndex);
                    if (realPath != null) mediaPath = realPath;
                }
            }
        }

        return new Video(uri.toString(), mediaName, mediaPath, date, true);
    }

    public void setAudioPlaylist() {
        if (audioList != null) {
            List<MediaItem> mediaItemList = new ArrayList<>();

            for (Audio v : audioList) {
                MyMediaItem mediaItem = new MyMediaItem(v.getName(), v.getPath());
                mediaItemList.add(mediaItem.toExoPlayerMediaItem());
            }
            MediaRepository.getInstance().setAudioPlaylist(mediaItemList);
        }
    }

    public void setVideoPlaylist() {
        if(videoList != null) {
            List<MediaItem> mediaItemList = new ArrayList<>();

            for (Video v : videoList) {
                MyMediaItem mediaItem = new MyMediaItem(v.getName(), v.getPath());
                mediaItemList.add(mediaItem.toExoPlayerMediaItem());
            }
            MediaRepository.getInstance().setVideoPlaylist(mediaItemList);
        }
    }

    private void getPersistentAccess(Uri uri) {
        int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
        try {
            getContentResolver().takePersistableUriPermission(uri, takeFlags);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

}