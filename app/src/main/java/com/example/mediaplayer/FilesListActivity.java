package com.example.mediaplayer;
import android.Manifest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.media3.exoplayer.ExoPlayer;
//import androidx.media3.exoplayer.Player;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mediaplayer.Media;

import java.util.ArrayList;
import java.util.List;

public class FilesListActivity extends AppCompatActivity {

    private PlayerView playerView;
    private ExoPlayer player;
    private ArrayList<String> FilesName;
    private ArrayList<String> FilesPath;
    private ArrayList<String> FilesDuration;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private MediaAdapter adapter;
    private List<Media> mediaList;

    private ConstraintLayout RationaleLayout;
    private boolean NotificationEnabled;
    private boolean Enabled;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files_list);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FilesName = new ArrayList<>();
        FilesPath = new ArrayList<>();
        FilesDuration = new ArrayList<>();


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryMediaFiles(); // Example: Refresh media list
                swipeRefreshLayout.setRefreshing(false);
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                // Permission is granted
                queryMediaFiles();
            } else {
                checkNotificationAccess();
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        }
        else {
            checkStorageAccess();
            checkNotificationAccess();
        }
    }

    private void checkNotificationAccess() {
        // Check if the permission is granted
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted. Proceed with the action.
            NotificationEnabled = true;
        }
        else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.POST_NOTIFICATIONS)) {
            // Permission hasn't been granted yet and the user hasn't declined it previously.
            // You can explain to the user why the permission is needed.
            showRationaleDialogNotification();
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            // Permission hasn't been granted yet. Request the permission.
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }


    private void checkStorageAccess() {
        // Check if the permission is granted
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_MEDIA_AUDIO) ==
                PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted. Proceed with the action.
            queryMediaFiles(); // Example: method to query and display media files
            Toast.makeText(this, "Yes Allowed", Toast.LENGTH_SHORT).show();
        }
        else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.READ_MEDIA_AUDIO)) {
            // Permission hasn't been granted yet and the user hasn't declined it previously.
            // You can explain to the user why the permission is needed.
            showRationaleDialog();
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO);
        }
        else {
            // Permission hasn't been granted yet. Request the permission.
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO);
        }
    }

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog.
    ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    if (isGranted) {
                        // Permission is granted. Continue the action or workflow in your
                        // app.
                        queryMediaFiles();
                        Toast.makeText(FilesListActivity.this, "Yes Allowed", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(FilesListActivity.this, "Not Allowed", Toast.LENGTH_SHORT).show();

                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                FilesListActivity.this, Manifest.permission.READ_MEDIA_AUDIO)) {
                            showSettingsRedirectDialog();
                        } else {
                            showPermissionDeniedMessage();
                        }
                    }
                }
            });

    private void showRationaleDialogNotification(){
        new AlertDialog.Builder(this)
                .setTitle("Notification Permission")
                .setMessage("Notification permission is required to control the media playback from notification while background play is enabled. Please enable it in the permissions of the app settings.")
                .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open app settings
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", FilesListActivity.this.getPackageName(), null);
                        intent.setData(uri);
                        FilesListActivity.this.startActivity(intent);
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


    private void showSettingsRedirectDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Needed")
                .setMessage("Storage permission is required to access media files. Please enable it in the permissions of the app settings.")
                .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open app settings
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", FilesListActivity.this.getPackageName(), null);
                        intent.setData(uri);
                        FilesListActivity.this.startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void showPermissionDeniedMessage() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Denied")
                .setMessage("Storage permission is required to access media files. Please enable it in settings.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open app settings
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", FilesListActivity.this.getPackageName(), null);
                        intent.setData(uri);
                        FilesListActivity.this.startActivity(intent);
                    }
                })
                .create()
                .show();


    }

    private void showRationaleDialog() {
        Toast.makeText(this, "Allow it", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if the permission is granted after returning from settings
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_MEDIA_AUDIO) ==
                PackageManager.PERMISSION_GRANTED) {
            // Permission is granted. Proceed with the action.
            queryMediaFiles(); // Example: method to query and display media files
        }
    }


    private void queryMediaFiles() {
        String[] projection = new String[] {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION
        };

//        String selection = sql-where-clause-with-placeholder-variables;
//        String[] selectionArgs = new String[] {
//                values-of-placeholder-variables
//        };
//        String sortOrder = sql-order-by-clause;

        Cursor cursor = getApplicationContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        mediaList = new ArrayList<>();

        Log.d("Cursor", String.valueOf(cursor.getCount()));

        if (cursor != null) {
            // Retrieve data like file path, display name, duration, etc.
            int filePath_ind = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int displayName_ind = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
            int duration_ind = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            Log.d("queryMediaFiles", "yes");
            if (filePath_ind != -1 && displayName_ind != -1 && duration_ind != -1) {
                while (cursor.moveToNext()) {
                    String filePath = cursor.getString(filePath_ind);
                    String displayName = cursor.getString(displayName_ind);
                    long dur = cursor.getLong(duration_ind);
                    String duration = MillisToTime(dur);

                    FilesName.add(displayName);
                    FilesPath.add(filePath);
                    FilesDuration.add(duration);

                    mediaList.add(new Media(displayName, filePath, duration, ""));

                    Log.d("queryMediaFiles", filePath);
                    Log.d("queryMediaFiles", displayName);
                    Log.d("queryMediaFiles", duration);
                }
            }
            cursor.close();
        }
        Log.d("queryMediaFiles", String.valueOf(FilesPath.size()));
        Log.d("queryMediaFiles", String.valueOf(FilesPath));

        Log.d("TAG", "media list: " + mediaList);
        adapter = new MediaAdapter(this, mediaList);
        recyclerView.setAdapter(adapter);
    }

    public String MillisToTime(long millis) {
//        int hours = (millis / (1000 * 60 * 60)) % 24;
        long minutes = (millis / (1000 * 60)) % 60;
        long seconds = (millis / 1000) % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }
}
