package com.example.mediaplayer;
import android.Manifest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private ConstraintLayout StorageRationaleLayout;
    private Button Rationale_AllowAccess_Button;
    private boolean NotificationEnabled;

    private boolean readStorage = false;
    private boolean writeStorage = false;
    private String TAG = "TAG";


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

        StorageRationaleLayout = findViewById(R.id.StorageAccessLayout);
        Rationale_AllowAccess_Button = findViewById(R.id.allowAccess);


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
                Log.d(TAG, "onCreate: YES");
            }
            else {
                checkNotificationAccess();
                StorageRationaleLayout.setVisibility(View.VISIBLE);
                Rationale_AllowAccess_Button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        StorageAccessA11_PermissionDialog();
                    }
                });
            }
        }
        else {
            checkStorageAccessA10();
        }
    }

    private void checkNotificationAccess() {
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
                showRationaleDialogNotification();
            }

            else {
                NotificationEnabled = false;
                requestPermissionLauncher_Notification.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    // system permissions dialog.
    ActivityResultLauncher<String> requestPermissionLauncher_Notification =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    NotificationEnabled = isGranted;
                }
            });


    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private void checkStorageAccessA10() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//            NotificationEnabled = true;
            queryMediaFiles();
        }

        else {
//            NotificationEnabled = false;
            requestPermissionsLauncher_StorageA10.launch(REQUIRED_PERMISSIONS);
        }
    }

    ActivityResultLauncher<String[]> requestPermissionsLauncher_StorageA10 =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                        @Override
                        public void onActivityResult(Map<String, Boolean> permissions) {
                            Boolean readPermission = permissions.get(Manifest.permission.READ_EXTERNAL_STORAGE);
                            Boolean writePermission = permissions.get(Manifest.permission.WRITE_EXTERNAL_STORAGE);

                            if (readPermission != null && readPermission && writePermission != null && writePermission) {
                                // Both permissions are granted
                                queryMediaFiles();
                            }
                            else {
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


    private void StorageAccessA11_PermissionDialog() {
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

    private void showRationaleDialogNotification(){
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


    @Override
    protected void onResume() {
        super.onResume();
        // Check if the permission is granted after returning to app.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()){
                StorageRationaleLayout.setVisibility(View.GONE);
                queryMediaFiles();
            }
        }
        else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                StorageRationaleLayout.setVisibility(View.GONE);
                queryMediaFiles();
            }
        }
    }


    private void queryMediaFiles() {
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
            processCursor(audioCursor, parentFolders);
            audioCursor.close();
        }

        // Process video files
        if (videoCursor != null) {
            processCursor(videoCursor, parentFolders);
            videoCursor.close();
        }

        // Retrieve and display files in each parent folder
        for (String folderPath : parentFolders) {
            Log.d(TAG, "queryMediaFiles: " + folderPath);
        }

        Log.d("queryMediaFiles", String.valueOf(FilesPath.size()));
        Log.d("queryMediaFiles", String.valueOf(FilesPath));

        Log.d("TAG", "media list: " + mediaList);
        adapter = new MediaAdapter(this, mediaList);
        recyclerView.setAdapter(adapter);
    }

    private ExecutorService executorService = Executors.newFixedThreadPool(4); // Create a thread pool

    private void processCursor(Cursor cursor, Set<String> parentFolders) {
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

                Media media = new Media(displayName, filePath, formattedDuration, BitmapFactory.decodeResource(getResources(), R.drawable.icon));
                mediaList.add(media);

                // Offload thumbnail generation to background thread
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

                Log.d("queryMediaFiles", filePath);
                Log.d("queryMediaFiles", displayName);
                Log.d("queryMediaFiles", formattedDuration);
            }
        }
    }

    private Bitmap getThumbnail(String filePath) {
//        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(filePath, 1);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        Bitmap thumbnail = null;

        try {
            retriever.setDataSource(filePath);
            Bitmap frame = retriever.getFrameAtTime(1000000); // Get frame at 30 second (1000000 microseconds)
            byte[] art = retriever.getEmbeddedPicture();

            if (art != null) {
                thumbnail = BitmapFactory.decodeByteArray(art, 0, art.length);
            }
            else {
                if (frame != null) {
                    thumbnail = frame;
                }
                else {
                    thumbnail = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            // Handle cases where the file is invalid or data source is unsupported
        }
        finally {
            try {
                retriever.release();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return thumbnail;
    }

    public String MillisToTime(long millis) {
    //        int hours = (millis / (1000 * 60 * 60)) % 24;
        long minutes = (millis / (1000 * 60)) % 60;
        long seconds = (millis / 1000) % 60;

        return String.format("%02d:%02d", minutes, seconds);
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
