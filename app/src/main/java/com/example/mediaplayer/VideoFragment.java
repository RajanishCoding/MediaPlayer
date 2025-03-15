package com.example.mediaplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;


import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class VideoFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private VideoAdapter adapter;

    private List<Video> mediaList;

    private TextView foundText;

    private ArrayList<String> FilesName;
    private static ArrayList<String> FilesPath;
    private static ArrayList<String> AudioFilesPath;
    private ArrayList<String> FilesDateAdded;

    private String TAG = "VideoTag";
    private boolean isInsert;

    private ExecutorService executorService;

    private List<Video> storedMediaList;
    private boolean isFilesStored;

    private VideoAdapter mediaAdapter;

    private Thread thread;

    private View menu_Container;
    private ImageButton menu_Button;
    private ImageButton search_Button;

    private Switch mode_Switch;
    private boolean switch_frag;
    private boolean switch_isEnd = true;

    private Button ByName_sort;
    private Button BySize_sort;
    private Button ByDate_sort;
    private Button ByLength_sort;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    public VideoFragment() {
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
        View view = inflater.inflate(R.layout.fragment_video, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView = view.findViewById(R.id.recyclerViewVideo);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        mediaList = new ArrayList<>();

        foundText = view.findViewById(R.id.found_text_video);

        FilesName = new ArrayList<>();
        FilesPath = new ArrayList<>();
        AudioFilesPath = new ArrayList<>();
        FilesDateAdded = new ArrayList<>();

        menu_Container = view.findViewById(R.id.MenuContainer);
        menu_Button = view.findViewById(R.id.menu_button);
        search_Button = view.findViewById(R.id.search_button);

        ByName_sort = view.findViewById(R.id.sort_byName);
        ByDate_sort = view.findViewById(R.id.sort_byDate);
        ByLength_sort = view.findViewById(R.id.sort_byLength);
        BySize_sort = view.findViewById(R.id.sort_bySize);

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

//        mediaList.add(new Video("Name", "Path", "Size", null));
        adapter = new VideoAdapter(requireContext(), mediaList);
        recyclerView.setAdapter(adapter);

        menu_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switch_isEnd) {
                    if (switch_frag) {
                        switch_frag = false;
                        hideMenuLayout();
                    } else {
                        switch_frag = true;
                        showMenuLayout();
                    }
                }
            }
        });

        ByName_sort.setOnClickListener(v -> {
            setBackground(ByName_sort, true);
            setBackground(ByDate_sort, false);
            setBackground(ByLength_sort, false);
            setBackground(BySize_sort, false);
        });

        ByDate_sort.setOnClickListener(v -> {
            setBackground(ByName_sort, false);
            setBackground(ByDate_sort, true);
            setBackground(ByLength_sort, false);
            setBackground(BySize_sort, false);
        });

        ByLength_sort.setOnClickListener(v -> {
            setBackground(ByName_sort, false);
            setBackground(ByDate_sort, false);
            setBackground(ByLength_sort, true);
            setBackground(BySize_sort, false);
        });

        BySize_sort.setOnClickListener(v -> {
            setBackground(ByName_sort, false);
            setBackground(ByDate_sort, false);
            setBackground(ByLength_sort, false);
            setBackground(BySize_sort, true);
        });
    }

    private void setBackground(Button button, boolean b) {
        if (!b)
            button.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.toggle_notselected));
        else
            button.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.toggle_selected));
    }

    private void showMenuLayout() {
        Log.d("showMenuLayout", "showMenuLayout: YES");
        Animation slideInRight = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_right);
        menu_Container.startAnimation(slideInRight);

        slideInRight.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart (Animation animation){
                menu_Container.setVisibility(View.VISIBLE);
                switch_isEnd = false;
//                isMenuContainerShowing = true;
            }

            @Override
            public void onAnimationEnd (Animation animation){
                switch_isEnd = true;
            }

            @Override
            public void onAnimationRepeat (Animation animation){}
        });
    }

    private void hideMenuLayout() {
        Log.d("showMenuLayout", "showMenuLayout: NO");
        Animation slideOutRight = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_right);
        menu_Container.startAnimation(slideOutRight);

        slideOutRight.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart (Animation animation){
                switch_isEnd = false;
//                isMenuContainerShowing = true;
            }

            @Override
            public void onAnimationEnd (Animation animation){
                menu_Container.setVisibility(View.GONE);
                switch_isEnd = true;
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


    private void sortMediaList() {
        ;
    }


    private void Load_Or_Query_MediaList() {
        executorService.submit(() -> {
            storedMediaList = loadMediaListFromPreferences();
            Log.d("Hello", "Stored: " + storedMediaList);

            if (storedMediaList == null || storedMediaList.isEmpty()) {
                isFilesStored = false;

                Log.d("video", "Load_Or_Query_MediaList: ");

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
                        mediaList.sort((m1, m2) -> CharSequence.compare(m1.getDuration(), m2.getDuration()));
                        adapter.notifyDataSetChanged();

                        Check_And_Update_Files();
                    }
                    mediaList.sort((m1, m2) -> CharSequence.compare(m1.getDuration(), m2.getDuration()));
                    adapter.notifyDataSetChanged();
                    Log.d("Hello", "UIThread: " + storedMediaList);
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

    private List<Video> queryMediaFiles(boolean refresh) {
        List<Video> mediaList = new ArrayList<>();

        String[] videoProjection = new String[]{
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATE_ADDED
        };

        String sortOrder = MediaStore.Video.Media.DISPLAY_NAME + " ASC";

        Cursor videoCursor = requireContext().getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                videoProjection,
                null,
                null,
                sortOrder
        );

        Set<String> parentFolders = new HashSet<>();

        // Process video files
        if (videoCursor != null) {
            mediaList.addAll(processCursor(videoCursor, parentFolders));
            Log.d(TAG, "Video list Count: " + videoCursor.getColumnCount());
            videoCursor.close();
        }

        // Retrieve and display files in each parent folder
        for (String folderPath : parentFolders) {
            Log.d(TAG, "queryMediaFiles: " + folderPath);
        }

        Log.d("queryMediaFiles", String.valueOf(FilesPath.size()));
        Log.d("queryMediaFiles", String.valueOf(FilesPath));

        Log.d(TAG, "Video list: " + mediaList);

        return mediaList;
    }

    private List<Video> processCursor(Cursor cursor, Set<String> parentFolders) {
        List<Video> mediaList = new ArrayList<>();

        int filePathInd = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
        int displayNameInd = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
        int dateAddedInd = cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED);

        if (filePathInd != -1 && displayNameInd != -1 && dateAddedInd != -1) {
            while (cursor.moveToNext()) {
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

                Video media = new Video(displayName, filePath, date, null, true);
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
            List<Video> removingMediaList = new ArrayList<>();
            List<Video> newMediaList = new ArrayList<>(queryMediaFiles(false));
            Set<String> newListNames = new HashSet<>();

            for (Video m : newMediaList) {
                newListNames.add(m.getName());
            }

            for (Video m : storedMediaList) {
                if (!newListNames.contains(m.getName())) {
                    removingMediaList.add(m);
                }
            }


            // For Insertion
            List<Video> addingMediaList = new ArrayList<>();
            Set<String> storedListNames = new HashSet<>();

            for (Video m : storedMediaList) {
                storedListNames.add(m.getName());
            }

            for (Video m : newMediaList) {
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

                for (Video m : removingMediaList) {
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


    public boolean isInsertFiles(List<Video> storedList, Video media) {
        boolean f = false;
        for (Video m: storedList) {
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


    private void saveMediaListToPreferences(List<Video> mediaList) {
        new Thread(() -> {
            try {
                SharedPreferences prefs = requireContext().getSharedPreferences("MediaPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                Gson gson = new Gson();
                String json = gson.toJson(mediaList);
                editor.putString("videoList", json);
                editor.apply();
            } catch (Exception e) {
                Log.e(TAG, "saveMediaListToPreferences: ", e);
            }
        }).start();
    }

    private List<Video> loadMediaListFromPreferences() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MediaPrefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("videoList", null);
        Type type = new TypeToken<ArrayList<Video>>() {}.getType();
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