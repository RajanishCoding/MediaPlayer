package com.example.mediaplayer;
import android.Manifest;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
//import androidx.media3.exoplayer.Player;
import androidx.media3.common.util.UnstableApi;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FilesListActivity extends AppCompatActivity {

    private ArrayList<String> FilesName;
    private static ArrayList<String> FilesPath;
    private static ArrayList<String> AudioFilesPath;
    private ArrayList<String> FilesDuration;

    private List<Video> mediaList;

    private ConstraintLayout StorageRationaleLayout;
    private Button Rationale_AllowAccess_Button;
    private boolean NotificationEnabled;

    private boolean readStorage;
    private boolean writeStorage;
    private boolean isStorageAccessed;
    private boolean isSetAdapterNeeded;
    private String TAG = "TAG";
    public boolean isInsert;

    private BottomNavigationView bottomNavigationView;

    private ImageButton lastPlay_Button;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private View menu_Container;
    private ImageButton menu_Button;
    private ImageButton search_Button;
    private Switch mode_Switch;

    public static boolean isThemeChanged = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files_list);

        bottomNavigationView = findViewById(R.id.bottomNavBar);

        FilesName = new ArrayList<>();
        FilesPath = new ArrayList<>();
        AudioFilesPath = new ArrayList<>();
        FilesDuration = new ArrayList<>();

        StorageRationaleLayout = findViewById(R.id.StorageAccessLayout);
        Rationale_AllowAccess_Button = findViewById(R.id.allowAccess);

//        lastPlay_Button = findViewById(R.id.Play_Last);

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        setThemeMode();

//        menu_Container = findViewById(R.id.MenuContainer);
        menu_Button = findViewById(R.id.moremenu_button);
        search_Button = findViewById(R.id.search_button);

//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
////                queryMediaFiles(true); // Example: Refresh media list
//                swipeRefreshLayout.setRefreshing(false);
//            }
//        });

        CheckAndAsk_StorageAccess();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_video) {
                    if (!isStorageAccessed) {
                        return false;
                    }

                    loadFragment(new VideoFragment(), false);
                    return true;
                }

                else if (item.getItemId() == R.id.nav_audio) {
                    if (!isStorageAccessed) {
                        return false;
                    }
                    loadFragment(new AudioFragment(), false);
                    return true;
                }

                else {
                    loadFragment(new SettingsFragment(), false);
                    return true;
                }
            }
        });

        if (isThemeChanged) {  // If recreated due to theme change
            bottomNavigationView.post(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    bottomNavigationView.setSelectedItemId(R.id.nav_video);
                    Log.d("ThemeChange", "onCreate: ");
                }
            });
            Log.d("ThemeChange", "onCreate: Theme changed, selecting Video in NavBar " + isThemeChanged);
            isThemeChanged = false; // Reset flag
        }



//        new Handler(Looper.getMainLooper()).postDelayed(() -> {
//            bottomNavigationView.setSelectedItemId(R.id.nav_video);
//            Log.d("Theme NOT", "onCreate: ");
//        }, 200);


//        lastPlay_Button.setOnClickListener(new View.OnClickListener() {
//            @OptIn(markerClass = UnstableApi.class)
//            @Override
//            public void onClick(View v) {
//                String name = sharedPreferences.getString("lastPlayedFileName", null);
//                String path = sharedPreferences.getString("lastPlayedFilePath", null);
//                Boolean isVideo = sharedPreferences.getBoolean("lastPlayedFile_isVideo", false);
//
//                if (name != null) {
//                    Intent intent = new Intent(FilesListActivity.this, PlayerActivity.class);
//                    intent.putExtra("Name", name);
//                    intent.putExtra("Path", path);
//                    intent.putExtra("isVideo", isVideo);
//                    startActivity(intent);
//                }
//            }
//        });

        Log.d("DEBUG", "menu_Button: " + (menu_Button != null ? "Exists" : "NULL"));
        menu_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("showMenuLayout", "showMenuLayout: YES");
                showMenuLayout();
            }
        });
    }

    private void setThemeMode() {
        SharedPreferences sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        int savedMode = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        int currentMode = AppCompatDelegate.getDefaultNightMode();

        if (savedMode != currentMode) {  // Only set if its different
            AppCompatDelegate.setDefaultNightMode(savedMode);
            Log.d("niced", "onViewCreated: YES " + savedMode + currentMode);
        }
    }

    private void showMenuLayout() {
        Log.d("showMenuLayout", "showMenuLayout: YES");
        Animation slideInRight = AnimationUtils.loadAnimation(FilesListActivity.this, R.anim.slide_in_right);
        menu_Container.startAnimation(slideInRight);

        slideInRight.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart (Animation animation){
                menu_Container.setVisibility(View.VISIBLE);
//                isMenuContainerShowing = true;
            }

            @Override
            public void onAnimationEnd (Animation animation){
            }

            @Override
            public void onAnimationRepeat (Animation animation){}
        });
    }


    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    ActivityResultLauncher<String> requestPermissionLauncher_Notification =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    NotificationEnabled = isGranted;
                }
            });

    ActivityResultLauncher<String[]> requestPermissionsLauncher_StorageA10 =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> permissions) {
                    Boolean readPermission = permissions.get(Manifest.permission.READ_EXTERNAL_STORAGE);
                    Boolean writePermission = permissions.get(Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    if (readPermission != null && readPermission && writePermission != null && writePermission) {
                        // Both permissions are granted
                        isStorageAccessed = true;
                        StorageRationaleLayout.setVisibility(View.GONE);
                        loadFragment(new VideoFragment(), true);
                    }
                    else {
                        isStorageAccessed = false;
                        StorageRationaleLayout.setVisibility(View.VISIBLE);
                        Rationale_AllowAccess_Button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", FilesListActivity.this.getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        });
                    }
                }
            });

    // Only check if permission is Allowed or not
    private boolean checkStorageAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    // Check and Decide for, which permission is needed
    private void CheckAndAsk_StorageAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            CheckAndAsk_StorageAccessA11();
        else
            CheckAndAsk_StorageAccessA10();
    }

    // Check and Asks the permission for android 11 and above
    private void CheckAndAsk_StorageAccessA10() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//            NotificationEnabled = true;
            isStorageAccessed = true;
            loadFragment(new VideoFragment(), true);
        }

        else {
            isStorageAccessed = false;
            requestPermissionsLauncher_StorageA10.launch(REQUIRED_PERMISSIONS);
        }
    }

    // Check and Asks the permission for android 10 and below
    private void CheckAndAsk_StorageAccessA11() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                isStorageAccessed = true;
                loadFragment(new VideoFragment(), true);
//                queryMediaFiles(false);
                Log.d(TAG, "onCreate: YES");
            }
            else {
                isStorageAccessed = false;
                CheckAndAsk_NotificationAccess();
                StorageRationaleLayout.setVisibility(View.VISIBLE);
                Rationale_AllowAccess_Button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPermissionDialog_StorageAccessA11();
                    }
                });
            }
        }


    }

    // Check and Asks the permission for Notification for Android 13 and above
    private void CheckAndAsk_NotificationAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check if the permission is granted
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                NotificationEnabled = true;
            }

            else if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.POST_NOTIFICATIONS)) {
                NotificationEnabled = false;
                showRationaleDialog_Notification();
            }

            else {
                NotificationEnabled = false;
                requestPermissionLauncher_Notification.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    // Permission Confirmation Dialog for android 11 and above
    private void showPermissionDialog_StorageAccessA11() {
        new AlertDialog.Builder(this)
                .setTitle("Storage Permission")
                .setMessage(R.string.storage_access_text)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.R)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open permission
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        Uri uri = Uri.fromParts("package", FilesListActivity.this.getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    // Permission Rationale Dialog
    private void showRationaleDialog_Notification(){
        new AlertDialog.Builder(this)
                .setTitle("Notification Permission")
                .setMessage("Notification permission is required to control the media playback from notification while background play is enabled.\n\n" +
                        "Please enable it in the permissions of the app settings.")
                .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open app settings
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", FilesListActivity.this.getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }


    private void loadFragment(Fragment fragment, boolean isAdd) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainer);

        if (!fragment.getClass().isInstance(currentFragment)) {
            if (isAdd)
                fragmentTransaction.add(R.id.fragmentContainer, fragment);
            else
                fragmentTransaction.replace(R.id.fragmentContainer, fragment);
        }

        fragmentTransaction.commit();
    }

    private boolean isFragmentAlreadyActive() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        return currentFragment != null;
    }


    private void unloadFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainer);

        if (currentFragment != null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(currentFragment);
            fragmentTransaction.commit();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Check if the permission is granted after returning to app
        isStorageAccessed = checkStorageAccess();

        if (isStorageAccessed) {
            StorageRationaleLayout.setVisibility(View.GONE);

            if (!isFragmentAlreadyActive()) {
                loadFragment(new VideoFragment(), true);
            }
//            queryMediaFiles(true);
        }
        else {
            unloadFragment();
            CheckAndAsk_StorageAccess();
        }
    }


    public static ArrayList<String> getSongList() {
        if (AudioFilesPath != null) {
            return AudioFilesPath;
        }
        return null;
    }

//    if (ContextCompat.checkSelfPermission(
//            this, Manifest.permission.READ_MEDIA_AUDIO) ==
//    PackageManager.PERMISSION_GRANTED) {
//        // Permission is already granted. Proceed with the action.
//        queryMediaFiles(); // Example: method to query and display media files
//        Toast.makeText(this, "Yes Allowed", Toast.LENGTH_SHORT).show();
//    }
//        else if (ActivityCompat.shouldShowRequestPermissionRationale(
//                this, Manifest.permission.READ_MEDIA_AUDIO)) {
//        // Permission hasn't been granted yet and the user hasn't declined it previously.
//        // You can explain to the user why the permission is needed.
//        showRationaleDialog();
//        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO);
//    }
//        else {
//        // Permission hasn't been granted yet. Request the permission.
//        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO);
//    }

    //    ActivityResultLauncher<String> requestPermissionLauncher =
//            registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
//                @Override
//                public void onActivityResult(Boolean isGranted) {
//                    if (isGranted) {
//                        // Permission is granted. Continue the action or workflow in your
//                        // app.
//                        queryMediaFiles();
//                        Toast.makeText(FilesListActivity.this, "Yes Allowed", Toast.LENGTH_SHORT).show();
//                    }
//                    else {
//                        Toast.makeText(FilesListActivity.this, "Not Allowed", Toast.LENGTH_SHORT).show();
//
//                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
//                                FilesListActivity.this, Manifest.permission.READ_MEDIA_AUDIO)) {
//                            showSettingsRedirectDialog();
//                        } else {
//                            showPermissionDeniedMessage();
//                        }
//                    }
//                }
//            });
}