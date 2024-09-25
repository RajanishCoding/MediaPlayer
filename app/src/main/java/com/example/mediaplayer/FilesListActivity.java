package com.example.mediaplayer;
import android.Manifest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
//import androidx.media3.exoplayer.Player;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FilesListActivity extends AppCompatActivity {

    private ArrayList<String> FilesName;
    private static ArrayList<String> FilesPath;
    private static ArrayList<String> AudioFilesPath;
    private ArrayList<String> FilesDuration;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private MediaAdapter adapter;
    private List<Media> mediaList;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files_list);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerViewVideo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bottomNavigationView = findViewById(R.id.bottomNavBar);

        FilesName = new ArrayList<>();
        FilesPath = new ArrayList<>();
        AudioFilesPath = new ArrayList<>();
        FilesDuration = new ArrayList<>();

        StorageRationaleLayout = findViewById(R.id.StorageAccessLayout);
        Rationale_AllowAccess_Button = findViewById(R.id.allowAccess);


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                queryMediaFiles(true); // Example: Refresh media list
                swipeRefreshLayout.setRefreshing(false);
            }
        });

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
//        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainer);

        if (isAdd)
            fragmentTransaction.add(R.id.fragmentContainer, fragment);
        else
            fragmentTransaction.replace(R.id.fragmentContainer, fragment);

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


    List<Media> storedMediaList;

    private void queryMediaFiles(boolean refresh) {

        String[] audioProjection = new String[] {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION
        };

        String[] videoProjection = new String[] {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION
        };

        // Query audio files
        Cursor audioCursor = getApplicationContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                audioProjection,
                null,
                null,
                null
        );

        // Query video files
        Cursor videoCursor = getApplicationContext().getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                videoProjection,
                null,
                null,
                null
        );

        // Set to keep track of unique parent folders
        Set<String> parentFolders = new HashSet<>();
        mediaList = new ArrayList<>();

        // Process audio files
        if (audioCursor != null) {
            processCursor(audioCursor, parentFolders, true);
            audioCursor.close();
        }

        // Process video files
        if (videoCursor != null) {
            processCursor(videoCursor, parentFolders, false);
            videoCursor.close();

            
        }

        // Retrieve and display files in each parent folder
        for (String folderPath : parentFolders) {
            Log.d(TAG, "queryMediaFiles: " + folderPath);
        }

        Log.d("queryMediaFiles", String.valueOf(FilesPath.size()));
        Log.d("queryMediaFiles", String.valueOf(FilesPath));

        storedMediaList = loadMediaListFromPreferences();

        Log.d("TAG", "Media list: " + mediaList);
        Log.d(TAG, "Media list: - Stored: " + storedMediaList);



        if (!refresh) {
            saveMediaListToPreferences(mediaList);
            adapter = new MediaAdapter(this, mediaList);
            recyclerView.setAdapter(adapter);
            Log.d(TAG, "Refresh: Yes");
        }

        else if (isInsert){
            saveMediaListToPreferences(mediaList);
            adapter = new MediaAdapter(this, mediaList);
            recyclerView.setAdapter(adapter);
            isInsert = false;
            Log.d(TAG, "isInsert: " + isInsert);
        }

    }

//    private final ExecutorService executorService = Executors.newFixedThreadPool(10); // Create a thread pool

    private void processCursor(Cursor cursor, Set<String> parentFolders, boolean isAudio) {
        int filePathInd = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        int displayNameInd = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
        int durationInd = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

        if (filePathInd != -1 && displayNameInd != -1 && durationInd != -1) {
            while (cursor.moveToNext()) {
                String filePath = cursor.getString(filePathInd);
                String displayName = cursor.getString(displayNameInd);
                long duration = cursor.getLong(durationInd);
                String formattedDuration = MillisToTime(duration);

                // Add parent folder path to the set
                String parentFolder = new File(filePath).getParent();
                parentFolders.add(parentFolder);

                FilesName.add(displayName);
                FilesPath.add(filePath);
                FilesDuration.add(formattedDuration);

                if (isAudio) {
                    AudioFilesPath.add(filePath);
                }

//                storedMediaList = loadMediaListFromPreferences();

                Media media = new Media(displayName, filePath, formattedDuration, BitmapFactory.decodeResource(getResources(), R.drawable.icon));
                mediaList.add(media);


                if (storedMediaList != null) {
                    if (!isInsert) {
                        isInsert = isInsertFiles(storedMediaList, media);
//                        Log.d(TAG, "processCursor: " + isInsert + " : " + media.getName());
                    }
                }

//                else {
//                    mediaList.add(media);
//                }



//                if (cachedMediaList != null) {
//                    for (Media media : cachedMediaList) {
//                        cachedMediaMap.put(media.getPath(), media);
//                    }
//                }

//              Offload thumbnail generation to background thread
//                executorService.execute(() -> {
//                    Bitmap thumbnail = getThumbnail(filePath);
//                    media.setThumbnail(thumbnail);
//
//                    // Notify the RecyclerView to update the item if it's currently visible
//                    runOnUiThread(() -> {
//                        int index = mediaList.indexOf(media);
//                        if (index >= 0) {
//                            adapter.notifyItemChanged(index); // Assuming `adapter` is your RecyclerView.Adapter
//                        }
//                    });
//                });

//                Log.d("tag", filePath);
                Log.d("tag", displayName);
                Log.d("tag", formattedDuration);
            }
        }
    }

    public boolean isInsertFiles(List<Media> storedList, Media media) {
        boolean f = false;
        for (Media m: storedList) {
            if (Objects.equals(m.getName(), media.getName())) {
                f = true;
                break;
            }
        }
        if (!f) {
            Log.d(TAG, "isInsertFiles: YES");
            return true;
        }
        Log.d(TAG, "isInsertFiles: NO");
        return false;
    }

    public void saveMediaListToPreferences(List<Media> mediaList) {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("MediaPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mediaList);
        editor.putString("mediaList", json);
        editor.apply();
    }

    public List<Media> loadMediaListFromPreferences() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("MediaPrefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("mediaList", null);
        Type type = new TypeToken<ArrayList<Media>>() {}.getType();
        return gson.fromJson(json, type);
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