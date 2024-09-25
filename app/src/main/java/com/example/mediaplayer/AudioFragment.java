package com.example.mediaplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.nfc.Tag;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AudioFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private MediaAdapter adapter;
    private List<Media> mediaList;

    private ArrayList<String> FilesName;
    private static ArrayList<String> FilesPath;
    private static ArrayList<String> AudioFilesPath;
    private ArrayList<String> FilesDuration;

    private String TAG = "AudioTag";
    private boolean isInsert;

    private ExecutorService executorService;

    private List<Media> storedMediaList;
    private boolean isFilesStored;


    public AudioFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_audio, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView = view.findViewById(R.id.recyclerViewAudio);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        mediaList = new ArrayList<>();

        FilesName = new ArrayList<>();
        FilesPath = new ArrayList<>();
        AudioFilesPath = new ArrayList<>();
        FilesDuration = new ArrayList<>();

        executorService = Executors.newSingleThreadExecutor();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                Load_Or_Query_MediaList();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        Log.d(TAG, "onViewCreated: YES");

        adapter = new MediaAdapter(requireContext(), mediaList);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onStart() {
        super.onStart();

        Log.d("Hello", "Before Load: Yes");
        Load_Or_Query_MediaList();
        Log.d("Hello", "After Load: Yes");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        executorService.shutdown();
    }


    private void Load_Or_Query_MediaList() {
        executorService.submit(() -> {
            storedMediaList = loadMediaListFromPreferences();
            Log.d("Hello", "Stored: " + storedMediaList);

            if (storedMediaList == null || storedMediaList.isEmpty()) {
                isFilesStored = false;

                Log.d("audio", "Load_Or_Query_MediaList: ");

                // If no media files in preferences, query media files
                queryMediaFiles(false);

                // Saving the media files to preferences
                saveMediaListToPreferences(mediaList);
            }
            else {
                isFilesStored = true;
                queryMediaFiles(false);
            }

            requireActivity().runOnUiThread(() -> {
                if (isAdded()) {
                    if (!isFilesStored) {
                        recyclerView.setAdapter(adapter);
                    }

                    else {
                        mediaList.clear();
                        mediaList.addAll(storedMediaList);
                        adapter.notifyDataSetChanged();

//                        Check_And_Insert_Files(mediaList);
                    }

                    Log.d("Hello", "UIThread-Stored: " + storedMediaList);
                    Log.d("Hello", "UIThread: " + mediaList);

//                    if (!refresh) {
//                        adapter = new MediaAdapter(requireContext(), mediaList);
//                        recyclerView.setAdapter(adapter);
//                        Log.d(TAG, "Refresh: Yes");
//                    }
//                    else if (isInsert) {
//                        saveMediaListToPreferences(mediaList);
//                        adapter = new MediaAdapter(requireContext(), mediaList);
//                        recyclerView.setAdapter(adapter);
//                        isInsert = false;
//                        Log.d(TAG, "isInsert: " + isInsert);
//                    }
                }
            });
        });
    }

    private void queryMediaFiles(boolean refresh) {
        String[] audioProjection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.SIZE
        };

        String sortOrder = MediaStore.Audio.Media.DISPLAY_NAME + " ASC";

        Cursor audioCursor = requireContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                audioProjection,
                null,
                null,
                sortOrder
        );

        Set<String> parentFolders = new HashSet<>();

        // Process video files
        if (audioCursor != null) {
            processCursor(audioCursor, parentFolders);
            Log.d(TAG, "Audio list Count: " + audioCursor.getCount());
            audioCursor.close();
        }

        // Retrieve and display files in each parent folder
        for (String folderPath : parentFolders) {
            Log.d(TAG, "queryMediaFiles: " + folderPath);
        }

        Log.d("queryMediaFiles", String.valueOf(FilesPath.size()));
        Log.d("queryMediaFiles", String.valueOf(FilesPath));

        Log.d(TAG, "Audio list: " + mediaList);
    }

    private void processCursor(Cursor cursor, Set<String> parentFolders) {
        int filePathInd = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        int displayNameInd = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
        int sizeInd = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE);

        if (filePathInd != -1 && displayNameInd != -1 && sizeInd != -1) {
            while (cursor.moveToNext()) {
                String filePath = cursor.getString(filePathInd);
                String displayName = cursor.getString(displayNameInd);
                long size = cursor.getLong(sizeInd);
                String formattedSize = getFormattedFileSize(size);

                // Add parent folder path to the set
                String parentFolder = new File(filePath).getParent();
                parentFolders.add(parentFolder);

                FilesName.add(displayName);
                FilesPath.add(filePath);
                FilesDuration.add(formattedSize);

//                storedMediaList = loadMediaListFromPreferences();

                Media media = new Media(displayName, filePath, formattedSize, null);
                mediaList.add(media);

//                if (storedMediaList != null) {
//                    if (!isInsert) {
//                        isInsert = isInsertFiles(storedMediaList, media);
////                        Log.d(TAG, "processCursor: " + isInsert + " : " + media.getName());
//                    }
//                }

                Log.d(TAG, displayName);
                Log.d(TAG, formattedSize);
            }
        }
    }

    private void Check_And_Insert_Files(List<Media> mediaList) {
        for (int i = 0; i < mediaList.size(); i++) {
            boolean f = true;
            for (int j = 0; j < storedMediaList.size(); j++) {
                if (!mediaList.get(i).getName().equals(storedMediaList.get(j).getName())) {
                    f = true;
                }
                else {
                    f = false;
                }
            }
            if (f) {
                MediaAdapter mediaAdapter = new MediaAdapter(requireContext(), storedMediaList);
                mediaAdapter.addMediaItem(mediaList.get(i));

//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        saveMediaListToPreferences(media);
//                    }
//                })
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
        try {
            SharedPreferences prefs = requireContext().getSharedPreferences("MediaPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            Gson gson = new Gson();
            String json = gson.toJson(mediaList);
            editor.putString("audioList", json);
            editor.apply();
        }
        catch (Exception e) {
            Log.e(TAG, "saveMediaListToPreferences: ", e);
        }
    }

    public List<Media> loadMediaListFromPreferences() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MediaPrefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("audioList", null);
        Type type = new TypeToken<ArrayList<Media>>() {}.getType();
        return gson.fromJson(json, type);
    }


    public static String getFormattedFileSize(long sizeInBytes) {
        // All sizes in bytes
        final int KILOBYTE = 1024;
        final int MEGABYTE = KILOBYTE * 1024;
        final int GIGABYTE = MEGABYTE * 1024;

        // Converting based on size
        if (sizeInBytes < KILOBYTE) {
            return sizeInBytes + " B"; // Bytes
        }
        else if (sizeInBytes < MEGABYTE) {
            return (sizeInBytes / KILOBYTE) + " KB"; // Kilobytes
        }
        else if (sizeInBytes < GIGABYTE) {
            return (sizeInBytes / MEGABYTE) + " MB"; // Megabytes
        }
        else {
            double sizeInGB = (double) sizeInBytes / GIGABYTE;
            return String.format(Locale.ROOT,"%.1f GB", sizeInGB); // Gigabytes
        }
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

}