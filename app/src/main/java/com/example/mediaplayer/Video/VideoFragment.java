package com.example.mediaplayer.Video;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;


import com.example.mediaplayer.Extra.MediaRepository;
import com.example.mediaplayer.Extra.MyMediaItem;
import com.example.mediaplayer.FilesListActivity;
import com.example.mediaplayer.PlayerActivity;
import com.example.mediaplayer.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
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

    private Toolbar toolbar_main;
    private Toolbar toolbar_selection;
    private View bottomBar_selection;

    private TextView foundText;

    private List<Video> mediaList;

    private ArrayList<String> FilesName;
    private static ArrayList<String> FilesPath;
    private static ArrayList<String> VideoFilesPath;
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
    private Button ByResol_sort;
    private Button ByFps_sort;
    private Button Asc_sort;
    private Button Desc_sort;

    private Button path_Details;
    private Button date_Details;
    private Button resol_Details;
    private Button fps_Details;
    private Button size_Details;
    private Button dur_Details;
    private CheckBox durThumbnail_Details;

    private Button layout_List;
    private Button layout_Grid;

    private boolean isPath_Visible;
    private boolean isDate_Visible;
    private boolean isResol_Visible;
    private boolean isFps_Visible;
    private boolean isSize_Visible;
    private boolean isDur_Visible;

    private Button apply_Button;
    private Button cancel_Button;

    private String sortBy = "";
    private boolean isAscending;
    private boolean isList;
    private boolean isDur_OnThumbnail;

    private ImageButton lastPlay_Button;

    private View bottomSheet;
    private BottomSheetBehavior<View> behavior;

    private ImageButton toolbarSel_back;
    private ImageButton toolbarSel_play;
    private ImageButton toolbarSel_sel;
    private TextView toolbarSel_text;
    private boolean isAllSelected;

    private SharedPreferences settingsPrefs;
    private SharedPreferences.Editor settingsPrefsEditor;

    private SharedPreferences playerPrefs;
    private SharedPreferences.Editor playerPrefsEditor;



    public VideoFragment() {}

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

        toolbar_main = view.findViewById(R.id.toolbar);
        toolbar_selection = view.findViewById(R.id.toolbar_sel);
        bottomBar_selection = view.findViewById(R.id.bottomBar_sel);

        foundText = view.findViewById(R.id.found_text_video);

        mediaList = new ArrayList<>();

        FilesName = new ArrayList<>();
        FilesPath = new ArrayList<>();
        VideoFilesPath = new ArrayList<>();
        FilesDateAdded = new ArrayList<>();

        menu_Container = view.findViewById(R.id.MenuContainer);
        menu_Button = view.findViewById(R.id.menu_button);
        search_Button = view.findViewById(R.id.search_button);

        lastPlay_Button = view.findViewById(R.id.Play_Last);

        ByName_sort = view.findViewById(R.id.sort_byName);
        ByDate_sort = view.findViewById(R.id.sort_byDate);
        ByLength_sort = view.findViewById(R.id.sort_byLength);
        BySize_sort = view.findViewById(R.id.sort_bySize);
        ByResol_sort = view.findViewById(R.id.sort_byResol);
        ByFps_sort = view.findViewById(R.id.sort_byFps);

        Asc_sort = view.findViewById(R.id.sort_Asc);
        Desc_sort = view.findViewById(R.id.sort_Desc);

        path_Details = view.findViewById(R.id.details_Path);
        date_Details = view.findViewById(R.id.details_Date);
        resol_Details = view.findViewById(R.id.details_Resol);
        fps_Details = view.findViewById(R.id.details_Fps);
        size_Details = view.findViewById(R.id.details_Size);
        dur_Details = view.findViewById(R.id.details_Dur);
        durThumbnail_Details = view.findViewById(R.id.more_dur);

        layout_List = view.findViewById(R.id.layout_list);
        layout_Grid = view.findViewById(R.id.layout_grid);

        apply_Button = view.findViewById(R.id.buttonApply);
        cancel_Button = view.findViewById(R.id.buttonCancel);

        toolbarSel_back = view.findViewById(R.id.toolbarSel_backB);
        toolbarSel_play = view.findViewById(R.id.toolbarSel_playB);
        toolbarSel_sel = view.findViewById(R.id.toolbarSel_selB);
        toolbarSel_text = view.findViewById(R.id.toolbarSel_title);

        bottomSheet = view.findViewById(R.id.bottomsheet);
        behavior = BottomSheetBehavior.from(bottomSheet);

        executorService = Executors.newSingleThreadExecutor();

        settingsPrefs = requireContext().getSharedPreferences("VideoSettings", Context.MODE_PRIVATE);
        settingsPrefsEditor = settingsPrefs.edit();

        playerPrefs = requireContext().getSharedPreferences("PlayerPrefs", Context.MODE_PRIVATE);
        playerPrefsEditor = playerPrefs.edit();


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
//            File[] files = new File("/storage/emulated/0").listFiles();
//            for (File file : files) {
//                MediaScannerConnection.scanFile(
//                        getContext(),
//                        new String[]{file.getPath()},
//                        new String[]{"video/*"},
//                        (path, uri) -> {
//
//                        }
//                );
//            }
            Load_Or_Query_MediaList();
            swipeRefreshLayout.setRefreshing(false);
        });


//        mediaList.add(new Video("Name", "Path", "Size", null));
        adapter = new VideoAdapter(requireContext(), mediaList, getChildFragmentManager());
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);

        menu_Button.setOnClickListener(v -> {
            if (switch_isEnd) {
                if (switch_frag) {
                    hideMenuLayout();
                } else {
                    showMenuLayout();
                }
            }
        });

//        Load at Start
        sortBy = settingsPrefs.getString("sortBy", "Name");
        isAscending = settingsPrefs.getBoolean("isAscending", true);
        isList = settingsPrefs.getBoolean("isList", true);

//        Loading at Start
        loadDetailsButtonVisibility();

//        Notifying Adapter at Start
        setDetailsVisibility();

//        Setting BG at Start
        setBackground_LayoutButtons(isList);
        setBackground_SortButtons(sortBy);
        setBackground_AscDesc_Buttons(isAscending);
        setBackground_DetailsButtons();

        adapter.addSelectionListener(new VideoAdapter.SelectionListener() {
            @Override
            public void onSelectionStarts() {
                toolbar_selection.setAlpha(0f);
                toolbar_selection.setVisibility(View.VISIBLE);
                toolbar_selection.animate()
                    .alpha(1f)
                    .setDuration(350)
                    .start();

                bottomBar_selection.setAlpha(0f);
                bottomBar_selection.setVisibility(View.VISIBLE);
                bottomBar_selection.animate()
                        .alpha(1f)
                        .setDuration(350)
                        .start();
            }

            @Override
            public void onSelectionEnds() {
                toolbar_selection.animate()
                        .alpha(0f)
                        .setDuration(350)
                        .withEndAction(() -> toolbar_selection.setVisibility(View.GONE))
                        .start();

                bottomBar_selection.animate()
                        .alpha(0f)
                        .setDuration(350)
                        .withEndAction(() -> bottomBar_selection.setVisibility(View.GONE))
                        .start();
            }

            @Override
            public void onCountChanged(int counts) {
                toolbarSel_text.setText(String.format(Locale.getDefault(), "%d/%d selected", counts, mediaList.size()));
            }
        });

        toolbarSel_back.setOnClickListener(v -> {
            clearSelection();
        });

        toolbarSel_sel.setOnClickListener(v -> {
            if (!adapter.isAllSelected()) {
                selectAllItems();
            }
            else {
                deselectAllItems();
            }
            isAllSelected = !isAllSelected;
        });

        layoutB_Listeners();
        sortB_Listeners();
        detailsB_Listeners();

        apply_Button.setOnClickListener(v -> {
            setDetailsVisibility();
            sortMediaList(isAscending);
            hideMenuLayout();
            Log.d("DUR_onThumb", "onViewCreated: " + isDur_OnThumbnail);
        });

        cancel_Button.setOnClickListener(v -> hideMenuLayout());

        lastPlay_Button.setOnClickListener(new View.OnClickListener() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onClick(View v) {
                String name = playerPrefs.getString("lastPlayedFileName", null);
                String path = playerPrefs.getString("lastPlayedFilePath", null);
                Boolean isVideo = playerPrefs.getBoolean("lastPlayedFile_isVideo", false);

                if (name != null) {
                    Intent intent = new Intent(requireActivity(), PlayerActivity.class);
                    intent.putExtra("Name", name);
                    intent.putExtra("Path", path);
                    intent.putExtra("isVideo", isVideo);
                    startActivity(intent);
                }
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (adapter.isSelectionMode) {
                    clearSelection();
                }
                else{
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }


    private void selectAllItems() {
        for (Video v : mediaList) {
            v.isSelected = true;
        }
        adapter.notifyDataSetChanged();
        adapter.selectionCounts = mediaList.size();
        adapter.listener.onCountChanged(mediaList.size());
    }

    private void deselectAllItems() {
        for (Video v : mediaList) {
            v.isSelected = false;
        }
        adapter.notifyDataSetChanged();
        adapter.selectionCounts = 0;
        adapter.listener.onCountChanged(0);
    }

    private void clearSelection() {
        for (Video v : mediaList) {
            v.isSelected = false;
        }
        adapter.isSelectionMode = false;
        adapter.notifyDataSetChanged();
        adapter.listener.onSelectionEnds();
    }


    private void layoutB_Listeners() {
        layout_List.setOnClickListener(v -> setBackground_LayoutButtons(true));
        layout_Grid.setOnClickListener(v -> setBackground_LayoutButtons(false));
    }

    private void sortB_Listeners() {
        ByName_sort.setOnClickListener(v -> setBackground_SortButtons("Name"));
        ByDate_sort.setOnClickListener(v -> setBackground_SortButtons("Date"));
        ByLength_sort.setOnClickListener(v -> setBackground_SortButtons("Length"));
        BySize_sort.setOnClickListener(v -> setBackground_SortButtons("Size"));
        ByResol_sort.setOnClickListener(v -> setBackground_SortButtons("Resol"));
        ByFps_sort.setOnClickListener(v -> setBackground_SortButtons("Fps"));

        Asc_sort.setOnClickListener(v -> setBackground_AscDesc_Buttons(true));
        Desc_sort.setOnClickListener(v -> setBackground_AscDesc_Buttons(false));
    }

    private void detailsB_Listeners() {
        path_Details.setOnClickListener(v -> {
            isPath_Visible = !isPath_Visible;
            setBackground(path_Details, isPath_Visible);
        });

        resol_Details.setOnClickListener(v -> {
            isResol_Visible = !isResol_Visible;
            setBackground(resol_Details, isResol_Visible);
        });

        fps_Details.setOnClickListener(v -> {
            isFps_Visible = !isFps_Visible;
            setBackground(fps_Details, isFps_Visible);
        });

        date_Details.setOnClickListener(v -> {
            isDate_Visible = !isDate_Visible;
            setBackground(date_Details, isDate_Visible);
        });

        size_Details.setOnClickListener(v -> {
            isSize_Visible = !isSize_Visible;
            setBackground(size_Details, isSize_Visible);
        });

        dur_Details.setOnClickListener(v -> {
            isDur_Visible = !isDur_Visible;
            setBackground(dur_Details, isDur_Visible);
        });

        durThumbnail_Details.setOnCheckedChangeListener((buttonView, isChecked) -> isDur_OnThumbnail = isChecked);
    }


    private void sortMediaList(boolean isAsc) {
        boolean isSorted = true;

        Comparator<Video> comparator = null;

        switch (sortBy) {
            case "Name":
                comparator = Comparator.comparing(Video::getName, String.CASE_INSENSITIVE_ORDER);
                break;

            case "Size":
                comparator = Comparator.comparingLong(m -> Long.parseLong(m.getSize()));
                break;

            case "Length":
                comparator = Comparator.comparingLong(m -> Long.parseLong(m.getDuration()));
                break;

            case "Date":
                comparator = Comparator.comparingLong(m -> Long.parseLong(m.getDateAdded()));
                break;

            case "Resol":
                comparator = Comparator.comparingLong(m -> Long.parseLong(m.getResolution()));
                break;

            case "Fps":
                comparator = Comparator.comparingDouble(m -> Double.parseDouble(m.getFrameRate()));
                break;

            default:
                isSorted = false;
                break;
        }

        if (comparator != null) {
            if (!isAsc) {
                comparator = comparator.reversed(); // Reverse order for descending
            }
            mediaList.sort(comparator);
        }

        if (isSorted) {
            adapter.notifyDataSetChanged();
            settingsPrefsEditor.putString("sortBy", sortBy);
            settingsPrefsEditor.putBoolean("isAscending", isAsc);
            settingsPrefsEditor.apply();
            saveMediaListToPreferences(mediaList);
        }
    }

    private void setDetailsVisibility() {
        adapter.setDetailsVisibility(isPath_Visible, isResol_Visible, isFps_Visible, isSize_Visible, isDate_Visible, isDur_Visible, isDur_OnThumbnail);
        saveDetailsButtonVisibility();
    }


    private void setBackground(Button button, boolean b) {
        if (!b)
            button.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.toggle_notselected));
        else
            button.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.toggle_selected));
    }

    private void setBackground_LayoutButtons(boolean isList) {
        setBackground(layout_List, false);
        setBackground(layout_Grid, false);
        setBackground(isList ? layout_List : layout_Grid, true);
        this.isList = isList;
    }

    private void setBackground_SortButtons(String sortBy_Button) {
        List<Button> sortButtons = Arrays.asList(ByName_sort, BySize_sort, ByLength_sort, ByDate_sort, ByResol_sort, ByFps_sort);

        // Set all buttons to false first
        for (Button button : sortButtons) {
            setBackground(button, false);
        }

        // Set the selected button to true
        switch (sortBy_Button) {
            case "Name":   setBackground(ByName_sort, true);  break;
            case "Size":   setBackground(BySize_sort, true);  break;
            case "Length": setBackground(ByLength_sort, true); break;
            case "Date":   setBackground(ByDate_sort, true);  break;
            case "Resol":  setBackground(ByResol_sort, true); break;
            case "Fps":    setBackground(ByFps_sort, true);   break;
        }

        sortBy = sortBy_Button;
    }

    private void setBackground_AscDesc_Buttons(boolean isAscending) {
        setBackground(Asc_sort, false);
        setBackground(Desc_sort, false);
        setBackground(isAscending ? Asc_sort : Desc_sort, true);
        this.isAscending = isAscending;
    }

    private void setBackground_DetailsButtons() {
        setBackground(path_Details, isPath_Visible);
        setBackground(resol_Details, isResol_Visible);
        setBackground(fps_Details, isFps_Visible);
        setBackground(size_Details, isSize_Visible);
        setBackground(date_Details, isDate_Visible);
        setBackground(dur_Details, isDur_Visible);
        durThumbnail_Details.setChecked(isDur_OnThumbnail);
    }

    private void loadDetailsButtonVisibility() {
        isDate_Visible = settingsPrefs.getBoolean("date", true);
        isSize_Visible = settingsPrefs.getBoolean("size", true);
        isDur_Visible = settingsPrefs.getBoolean("dur", true);
        isPath_Visible = settingsPrefs.getBoolean("path", false);
        isResol_Visible = settingsPrefs.getBoolean("resol", false);
        isFps_Visible = settingsPrefs.getBoolean("fps", false);
        isDur_OnThumbnail = settingsPrefs.getBoolean("isDur_Thumbnail", true);
    }

    private void saveDetailsButtonVisibility() {
        settingsPrefsEditor.putBoolean("path", isPath_Visible);
        settingsPrefsEditor.putBoolean("date", isDate_Visible);
        settingsPrefsEditor.putBoolean("size", isSize_Visible);
        settingsPrefsEditor.putBoolean("resol", isResol_Visible);
        settingsPrefsEditor.putBoolean("fps", isFps_Visible);
        settingsPrefsEditor.putBoolean("dur", isDur_Visible);
        settingsPrefsEditor.putBoolean("isDur_Thumbnail", isDur_OnThumbnail);
        settingsPrefsEditor.apply();
    }



    private void showMenuLayout() {
        Log.d("showMenuLayout", "showMenuLayout: YES");
        Animation slideInRight = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_right);
        menu_Container.startAnimation(slideInRight);

        slideInRight.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart (Animation animation){
                menu_Container.setVisibility(View.VISIBLE);
                switch_frag = true;
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
                switch_frag = false;
//                isMenuContainerShowing = true;
            }

            @Override
            public void onAnimationEnd (Animation animation){
                menu_Container.setVisibility(View.GONE);
                switch_isEnd = true;

                sortBy = settingsPrefs.getString("sortBy", "Name");
                isAscending = settingsPrefs.getBoolean("isAscending", true);
                loadDetailsButtonVisibility();
                setBackground_SortButtons(sortBy);
                setBackground_AscDesc_Buttons(isAscending);
                setBackground_DetailsButtons();
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

                Log.d("video", "Load_Or_Query_MediaList: ");

                // If no media files in preferences, query media files
                mediaList.clear();
                mediaList.addAll(queryMediaFiles(false));
            }
            else {
                isFilesStored = true;
            }

            setVideoPlaylist();

            requireActivity().runOnUiThread(() -> {
                if (isAdded()) {
                    if (!isFilesStored) {
                        if (mediaList.isEmpty())
                            foundText.setVisibility(View.VISIBLE);
                        else
                            foundText.setVisibility(View.GONE);

                        sortMediaList(isAscending);
//                        adapter.notifyDataSetChanged();
//                        saveMediaListToPreferences(mediaList);
                    }

                    else {
                        foundText.setVisibility(View.GONE);
                        mediaList.clear();
                        mediaList.addAll(storedMediaList);
                        adapter.notifyDataSetChanged();

                        Check_And_Update_Files();


                    }



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

                Log.d(TAG, "processCursor: ");

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

                setVideoPlaylist();

                sortMediaList(isAscending);

//                adapter.notifyDataSetChanged();
//                saveMediaListToPreferences(mediaList);
            });

        }).start();
    }

    public void setVideoPlaylist() {
        if (mediaList != null) {
            List<MediaItem> mediaItemList = new ArrayList<>();

            for (Video v : mediaList) {
                MyMediaItem mediaItem = new MyMediaItem(v.getName(), v.getPath());
                mediaItemList.add(mediaItem.toExoPlayerMediaItem());
            }
            MediaRepository.getInstance().setVideoPlaylist(mediaItemList);
//            PlaylistManager manager = new PlaylistManager(mediaItemList);
//            MediaRepository.getInstance().setVideoPlaylistManager(manager);
        }
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