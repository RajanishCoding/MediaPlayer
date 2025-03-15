package com.example.mediaplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

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
import android.content.SharedPreferences;


public class AudioFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private AudioAdapter adapter;
    private List<Audio> mediaList;

    private TextView foundText;

    private ArrayList<String> FilesName;
    private static ArrayList<String> FilesPath;
    private static ArrayList<String> AudioFilesPath;
    private ArrayList<String> FilesDateAdded;

    private String TAG = "AudioTag";
    private boolean isInsert;

    private ExecutorService executorService;

    private List<Audio> storedMediaList;
    private boolean isFilesStored;

    private View menu_Container;
    private ImageButton menu_Button;
    private ImageButton search_Button;
    private Switch mode_Switch;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

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

        foundText = view.findViewById(R.id.found_text_audio);

        mediaList = new ArrayList<>();

        FilesName = new ArrayList<>();
        FilesPath = new ArrayList<>();
        AudioFilesPath = new ArrayList<>();
        FilesDateAdded = new ArrayList<>();

        menu_Container = view.findViewById(R.id.MenuContainer);
        menu_Button = view.findViewById(R.id.menu_button);
        search_Button = view.findViewById(R.id.search_button);

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

        adapter = new AudioAdapter(requireContext(), mediaList);
        recyclerView.setAdapter(adapter);

        sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        menu_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("showMenuLayout", "showMenuLayout: YES");
                showMenuLayout();
            }
        });
    }


    private void showMenuLayout() {
        Log.d("showMenuLayout", "showMenuLayout: YES");
        Animation slideInRight = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_right);
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
                mediaList.clear();
                mediaList.addAll(queryMediaFiles(false));
            }
            else {
                isFilesStored = true;
            }

            requireActivity().runOnUiThread(() -> {
                if (isAdded()) {
                    if (!isFilesStored) {
                        if (mediaList.isEmpty())
                            foundText.setVisibility(View.VISIBLE);
                        else
                            foundText.setVisibility(View.GONE);

                        adapter.notifyDataSetChanged();
                        saveMediaListToPreferences(mediaList);
                    }

                    else {
                        foundText.setVisibility(View.GONE);
                        mediaList.clear();
                        mediaList.addAll(storedMediaList);
                        adapter.notifyDataSetChanged();

                        Check_And_Update_Files();
                    }

                    Log.d("Hello", "UIThread-Stored: " + storedMediaList);
                    Log.d("Hello", "UIThread: " + mediaList);

//                    if (!refresh) {
//                        adapter = new VideoAdapter(requireContext(), mediaList);
//                        recyclerView.setAdapter(adapter);
//                        Log.d(TAG, "Refresh: Yes");
//                    }
//                    else if (isInsert) {
//                        saveMediaListToPreferences(mediaList);
//                        adapter = new VideoAdapter(requireContext(), mediaList);
//                        recyclerView.setAdapter(adapter);
//                        isInsert = false;
//                        Log.d(TAG, "isInsert: " + isInsert);
//                    }
                }
            });
        });
    }

    private List<Audio> queryMediaFiles(boolean refresh) {
        List<Audio> mediaList = new ArrayList<>();
        
        String[] audioProjection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATE_ADDED
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
            mediaList.addAll(processCursor(audioCursor, parentFolders));
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

        return mediaList;
    }

    private List<Audio> processCursor(Cursor cursor, Set<String> parentFolders) {
        List<Audio> mediaList = new ArrayList<>();

        int filePathInd = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        int displayNameInd = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
        int dateAddedInd = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);

        if (filePathInd != -1 && displayNameInd != -1 && dateAddedInd != -1) {
            Log.d(TAG, "processCursor1: " + cursor.getCount());
            while (cursor.moveToNext()) {
                Log.d(TAG, "processCursor2: " + cursor.getCount());

                Log.d(TAG, "Indices: " + filePathInd + ", " + displayNameInd + ", " + dateAddedInd);

                String filePath = cursor.getString(filePathInd);
                String displayName = cursor.getString(displayNameInd);
                String date = cursor.getString(dateAddedInd);

                // Add parent folder path to the set
                String parentFolder = new File(filePath).getParent();
                parentFolders.add(parentFolder);

                FilesName.add(displayName);
                FilesPath.add(filePath);
                FilesDateAdded.add(date);

//                storedMediaList = loadMediaListFromPreferences();

                Audio media = new Audio(displayName, filePath, date, null, false);
                mediaList.add(media);

//                if (storedMediaList != null) {
//                    if (!isInsert) {
//                        isInsert = isInsertFiles(storedMediaList, media);
////                        Log.d(TAG, "processCursor: " + isInsert + " : " + media.getName());
//                    }
//                }

                Log.d("wowo", displayName);
                Log.d("wowo", date);
            }
        }
        return mediaList;
    }


    private void Check_And_Update_Files() {
        new Thread(() -> {
            // For Deletion
            List<Audio> removingMediaList = new ArrayList<>();
            List<Audio> newMediaList = new ArrayList<>(queryMediaFiles(false));
            Set<String> newListNames = new HashSet<>();

            for (Audio m : newMediaList) {
                newListNames.add(m.getName());
            }

            for (Audio m : storedMediaList) {
                if (!newListNames.contains(m.getName())) {
                    removingMediaList.add(m);
                }
            }


            // For Insertion
            List<Audio> addingMediaList = new ArrayList<>();
            Set<String> storedListNames = new HashSet<>();

            for (Audio m : storedMediaList) {
                storedListNames.add(m.getName());
            }

            for (Audio m : newMediaList) {
                if (!storedListNames.contains(m.getName())) {
                    addingMediaList.add(m);
                }
            }

//            for (int i = 0; i < newMediaList.size(); i++) {
//                boolean found = false;
//                for (int j = 0; j < storedMediaList.size(); j++) {
//                    if (newMediaList.get(i).getName().equals(storedMediaList.get(j).getName())) {
//                        found = true;
//                        break;
//                    }
//                    else {
//                        found = false;
//                    }
//                }
//
//                if (!found) {
//                    newMediaList.add(newMediaList.get(i));
//                    adapter.notifyItemInserted(newMediaList.size() - 1);
//                }
//            }

            requireActivity().runOnUiThread(() -> {
//                mediaList.removeAll(removingMediaList);

                for (Audio m : removingMediaList) {
                    int index = mediaList.indexOf(m);
                    if (index >= 0) {
                        mediaList.remove(index);
                        adapter.notifyItemRemoved(index);
                    }
                }

                mediaList.addAll(addingMediaList);


                adapter.notifyDataSetChanged();
                saveMediaListToPreferences(mediaList);
            });

        }).start();
    }


    public boolean isInsertFiles(List<Audio> storedList, Audio media) {
        boolean f = false;
        for (Audio m: storedList) {
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


    private void saveMediaListToPreferences(List<Audio> mediaList) {
        new Thread(() -> {
            try {
                SharedPreferences prefs = requireContext().getSharedPreferences("MediaPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                Gson gson = new Gson();
                String json = gson.toJson(mediaList);
                editor.putString("audioList", json);
                editor.apply();
            } catch (Exception e) {
                Log.e(TAG, "saveMediaListToPreferences: ", e);
            }
        }).start();
    }


    public List<Audio> loadMediaListFromPreferences() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MediaPrefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("audioList", null);
        Type type = new TypeToken<ArrayList<Audio>>() {}.getType();
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