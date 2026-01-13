package com.example.mediaplayer.Audio;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
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

import com.example.mediaplayer.Extra.AnimationLibs;
import com.example.mediaplayer.Extra.ConsentDialog;
import com.example.mediaplayer.Extra.FFmpegMetadataRetriever;
import com.example.mediaplayer.Extra.MediaRepository;
import com.example.mediaplayer.Extra.MyBottomSheet;
import com.example.mediaplayer.Extra.MyMediaItem;
import com.example.mediaplayer.PlayerActivity;
import com.example.mediaplayer.R;
import com.example.mediaplayer.Room.AudioDao;
import com.example.mediaplayer.Room.RoomDB;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AudioFragment extends Fragment {

    private Context context;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private AudioAdapter adapter;

    private Toolbar toolbar_main;
    private Toolbar toolbar_selection;
    private View bottomBar_selection;

    private TextView foundText;

    private List<Audio> mediaList;

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
    private boolean switch_frag;
    private boolean switch_isEnd = true;

    private MaterialButton ByName_sort;
    private MaterialButton BySize_sort;
    private MaterialButton ByDate_sort;
    private MaterialButton ByLength_sort;
    private MaterialButtonToggleGroup sortOrderToggle;
    private MaterialButton Asc_sort;
    private MaterialButton Desc_sort;

    private MaterialButton path_Details;
    private MaterialButton date_Details;
    private MaterialButton size_Details;
    private MaterialButton dur_Details;
    private CheckBox durThumbnail_Details;

    private MaterialButtonToggleGroup layoutToggle;
    private MaterialButton layout_List;
    private MaterialButton layout_Grid;

    private boolean isPath_Visible;
    private boolean isDate_Visible;
    private boolean isSize_Visible;
    private boolean isDur_Visible;

    private Button apply_Button;
    private Button cancel_Button;

    private String sortBy = "";
    private boolean isAscending;
    private boolean isList;
    private boolean isDur_OnThumbnail;

    private ImageButton lastPlay_Button;
    private int lastPlayedPosition;

    private ImageButton toolbarSel_back;
    private ImageButton toolbarSel_play;
    private ImageButton toolbarSel_sel;
    private TextView toolbarSel_text;
    private boolean isAllSelected;

    private ImageButton bottomSel_ren;
    private ImageButton bottomSel_del;
    private ImageButton bottomSel_share;

    private AnimationLibs animationLibs;

    private SharedPreferences settingsPrefs;
    private SharedPreferences.Editor settingsPrefsEditor;

    private SharedPreferences playerPrefs;
    private SharedPreferences.Editor playerPrefsEditor;

    private SharedPreferences listPrefs;
    private SharedPreferences.Editor listPrefsEditor;

    private AudioDao audioDao;

    public AudioFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_audio, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView = view.findViewById(R.id.recyclerViewAudio);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        toolbar_main = view.findViewById(R.id.toolbar);
        toolbar_selection = view.findViewById(R.id.toolbar_sel);
        bottomBar_selection = view.findViewById(R.id.bottomBar_sel);

        foundText = view.findViewById(R.id.found_text_audio);

        mediaList = new ArrayList<>();

        FilesName = new ArrayList<>();
        FilesPath = new ArrayList<>();
        AudioFilesPath = new ArrayList<>();
        FilesDateAdded = new ArrayList<>();

        menu_Container = view.findViewById(R.id.MenuContainer_Audio);
        menu_Button = view.findViewById(R.id.menu_button);
        search_Button = view.findViewById(R.id.search_button);

        lastPlay_Button = view.findViewById(R.id.Play_Last);

        ByName_sort = view.findViewById(R.id.sort_byName);
        ByDate_sort = view.findViewById(R.id.sort_byDate);
        ByLength_sort = view.findViewById(R.id.sort_byLength);
        BySize_sort = view.findViewById(R.id.sort_bySize);

        sortOrderToggle = view.findViewById(R.id.sortOrderToggle);
        Asc_sort = view.findViewById(R.id.sort_Asc);
        Desc_sort = view.findViewById(R.id.sort_Desc);

        path_Details = view.findViewById(R.id.details_Path);
        date_Details = view.findViewById(R.id.details_Date);
        size_Details = view.findViewById(R.id.details_Size);
        dur_Details = view.findViewById(R.id.details_Dur);
        durThumbnail_Details = view.findViewById(R.id.more_dur);

        layoutToggle = view.findViewById(R.id.layoutViewToggle);
        layout_List = view.findViewById(R.id.layout_list);
        layout_Grid = view.findViewById(R.id.layout_grid);

        apply_Button = view.findViewById(R.id.buttonApply);
        cancel_Button = view.findViewById(R.id.buttonCancel);

        toolbarSel_back = view.findViewById(R.id.toolbarSel_backB);
        toolbarSel_play = view.findViewById(R.id.toolbarSel_playB);
        toolbarSel_sel = view.findViewById(R.id.toolbarSel_selB);
        toolbarSel_text = view.findViewById(R.id.toolbarSel_title);

        bottomSel_ren = view.findViewById(R.id.renameB_sel);
        bottomSel_del = view.findViewById(R.id.deleteB_sel);
        bottomSel_share = view.findViewById(R.id.shareB_sel);

        context = requireContext();
        executorService = Executors.newFixedThreadPool(1);

        settingsPrefs = requireContext().getSharedPreferences("AudioListSettings", Context.MODE_PRIVATE);
        settingsPrefsEditor = settingsPrefs.edit();

        listPrefs = requireContext().getSharedPreferences("ListSetting", Context.MODE_PRIVATE);
        listPrefsEditor = listPrefs.edit();

        playerPrefs = requireContext().getSharedPreferences("PlayerSetting", Context.MODE_PRIVATE);
        playerPrefsEditor = playerPrefs.edit();

        audioDao = RoomDB.getDatabase(context).audioDao();

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

        animationLibs = new AnimationLibs();

        adapter = new AudioAdapter(requireContext(), mediaList, getChildFragmentManager());
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);

        boolean isLastPlayButtonEnabled = listPrefs.getBoolean("isFloatingResumeEnabled", true);
        lastPlay_Button.setVisibility(isLastPlayButtonEnabled ? View.VISIBLE : View.GONE);

//      Load at Start
        sortBy = settingsPrefs.getString("sortBy", "Name");
        isAscending = settingsPrefs.getBoolean("isAscending", true);
        isList = settingsPrefs.getBoolean("isList", true);

//      Setting at start
        setListObserver();
        Load_Or_Query_MediaList();

//      Loading at Start
        loadDetailsButtonVisibility();

//      Notify Adapter at Start
        setDetailsVisibility();

//      Setting BG at Start
        setBackground_LayoutButtons(isList);
        setBackground_SortButtons(sortBy);
        setBackground_AscDesc_Buttons(isAscending);
        setBackground_DetailsButtons();

        adapter.addSelectionListener(new AudioAdapter.SelectionListener() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onItemClicked(int position, Audio audio) {
//                new Thread(() -> audioDao.markAsPlayed(audio.getPath())).start();
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("Name", audio.getName());
                intent.putExtra("Path", audio.getPath());
                intent.putExtra("isVideo", audio.isVideo());
                intent.putExtra("currentIndex", position);
                Log.d("isVideoFile", "onClick: " + audio.isVideo());
                context.startActivity(intent);
            }

            @Override
            public void onSelectionStarts() {
                animationLibs.fadeIn(toolbar_selection, 350, null, null);
                animationLibs.fadeIn(bottomBar_selection, 350, null, null);
            }

            @Override
            public void onSelectionEnds() {
                animationLibs.fadeOut(toolbar_selection, 350, null, () -> toolbar_selection.setVisibility(View.GONE));
                animationLibs.fadeOut(bottomBar_selection, 350, null, () -> toolbar_selection.setVisibility(View.GONE));
            }

            @Override
            public void onCountChanged(int counts) {
                toolbarSel_text.setText(String.format(Locale.getDefault(), "%d/%d selected", counts, mediaList.size()));
                bottomSel_ren.setEnabled(counts == 1);
            }

            @Override
            public void onOptionButtonClicked(int p, String name) {
                MyBottomSheet sheet = new MyBottomSheet(p, name);
                sheet.show(getChildFragmentManager(), sheet.getTag());

                sheet.addBottomSheetDialogListeners(new MyBottomSheet.BottomSheetDialogListeners() {
                    @Override
                    public void onPlayCLickListener(int position) {

                    }

                    @Override
                    public void onInfoClickListener(int position) {

                    }

                    @Override
                    public void onRenameClickListener(int p) {
                        List<Uri> uris = new ArrayList<>();
                        uris.add(Uri.parse(mediaList.get(p).getUri()));
                        ConsentDialog consent = new ConsentDialog(2, uris, mediaList.get(p).getName());
                        consent.show(getChildFragmentManager(), sheet.getTag());
                    }

                    @Override
                    public void onDeleteClickListener(int p) {
                        List<Uri> uris = new ArrayList<>();
                        uris.add(Uri.parse(mediaList.get(p).getUri()));
                        ConsentDialog consent = new ConsentDialog(1, uris, mediaList.get(p).getName());
                        consent.show(getChildFragmentManager(), sheet.getTag());
                    }

                    @Override
                    public void onShareClickListener(int position) {

                    }
                });
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

        menu_Button.setOnClickListener(v -> {
            if (switch_isEnd) {
                if (switch_frag) {
                    hideMenuLayout();
                    resetMoreContainerViews();
                } else {
                    showMenuLayout();
                }
            }
        });

        layoutB_Listeners();
        sortB_Listeners();
        detailsB_Listeners();

        apply_Button.setOnClickListener(v -> {
            settingsPrefsEditor.putString("sortBy", sortBy);
            settingsPrefsEditor.putBoolean("isAscending", isAscending);
            settingsPrefsEditor.apply();

            setDetailsVisibility();
            setListObserver();
            hideMenuLayout();
        });

        cancel_Button.setOnClickListener(v -> {
            hideMenuLayout();
            resetMoreContainerViews();
        });

        lastPlay_Button.setOnClickListener(new View.OnClickListener() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onClick(View v) {
                String name = listPrefs.getString("lastPlayedAudioName", "");
                String path = listPrefs.getString("lastPlayedAudioPath", "");
                Boolean isVideo = listPrefs.getBoolean("lastPlayedFile_isVideo", false);

                new Thread(() -> {
                    if (audioDao.isPathExists(path)) {
                        requireActivity().runOnUiThread(() -> {
                            Intent intent = new Intent(requireActivity(), PlayerActivity.class);
                            intent.putExtra("Name", name);
                            intent.putExtra("Path", path);
                            intent.putExtra("isVideo", isVideo);
                            intent.putExtra("currentIndex", lastPlayedPosition);
                            startActivity(intent);
                        });
                    }
                }).start();
            }
        });

        requireContext().getContentResolver().registerContentObserver(
                MediaStore.Audio.Media.getContentUri("external"), true,
                new ContentObserver(new Handler(Looper.getMainLooper())) {
                    @Override
                    public void onChange(boolean selfChange, @Nullable Uri uri) {
                        super.onChange(selfChange, uri);
                        Log.d("MediaChange", "Change detected: " + uri.getPath());
                        Load_Or_Query_MediaList();
                    }
                }
        );

        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (adapter.isSelectionMode) {
                    clearSelection();
                }
                else {
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }


    private void selectAllItems() {
        for (Audio v : mediaList) {
            v.isSelected = true;
        }
        adapter.notifyDataSetChanged();
        adapter.selectionCounts = mediaList.size();
        toolbarSel_text.setText(String.format(Locale.getDefault(), "%d/%d selected", adapter.selectionCounts, adapter.selectionCounts));
//        adapter.listener.onCountChanged(mediaList.size());
    }

    private void deselectAllItems() {
        for (Audio v : mediaList) {
            v.isSelected = false;
        }
        adapter.notifyDataSetChanged();
        adapter.selectionCounts = 0;
        toolbarSel_text.setText(String.format(Locale.getDefault(), "%d/%d selected", 0, mediaList.size()));
//        adapter.listener.onCountChanged(0);
    }

    private void clearSelection() {
        for (Audio v : mediaList) {
            v.isSelected = false;
        }
        adapter.isSelectionMode = false;
        adapter.notifyDataSetChanged();
        adapter.listener.onSelectionEnds();
    }


    private void layoutB_Listeners() {
        layoutToggle.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (checkedId == R.id.layout_list) {
                    isList = isChecked;
                }
                else {
                    isList = !isChecked;
                }
            }
        });
    }

    private void sortB_Listeners() {
        ByName_sort.setOnClickListener(v -> setBackground_SortButtons("Name"));
        ByDate_sort.setOnClickListener(v -> setBackground_SortButtons("Date"));
        ByLength_sort.setOnClickListener(v -> setBackground_SortButtons("Length"));
        BySize_sort.setOnClickListener(v -> setBackground_SortButtons("Size"));

        sortOrderToggle.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (checkedId == R.id.sort_Asc) {
                    isAscending = isChecked;
                }
                else {
                    isAscending = !isChecked;
                }
            }
        });
    }

    private void detailsB_Listeners() {
        path_Details.setOnClickListener(v -> {
            isPath_Visible = !isPath_Visible;
            path_Details.setChecked(isPath_Visible);
        });

        date_Details.setOnClickListener(v -> {
            isDate_Visible = !isDate_Visible;
            date_Details.setChecked(isDate_Visible);
        });

        size_Details.setOnClickListener(v -> {
            isSize_Visible = !isSize_Visible;
            size_Details.setChecked(isSize_Visible);
        });

        dur_Details.setOnClickListener(v -> {
            isDur_Visible = !isDur_Visible;
            dur_Details.setChecked(isDur_Visible);
        });

        durThumbnail_Details.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isDur_OnThumbnail = isChecked;
        });
    }


    private void setListObserver() {
        LiveData<List<Audio>> livedataList = getSortedListLiveData();

        if (livedataList != null) {
            livedataList.removeObservers(getViewLifecycleOwner());
        }

        getSortedListLiveData().observe(getViewLifecycleOwner(), new Observer<List<Audio>>() {
            @Override
            public void onChanged(List<Audio> audios) {
                if (audios == null || audios.isEmpty()) foundText.setVisibility(View.VISIBLE);
                else foundText.setVisibility(View.INVISIBLE);

                if (audios != null) {
                    String lastPlayedPath = listPrefs.getString("lastPlayedAudioPath", "");
                    lastPlayedPosition = 0;

                    for (int i = 0; i < audios.size(); i++) {
                        Audio v = audios.get(i);
                        boolean isMatch = v.getPath().equals(lastPlayedPath);
                        v.setLastPlayed(isMatch);

                        if (isMatch) {
                            lastPlayedPosition = i; // Position for Last Play Floating Button
                        }
                    }
                }

                adapter.submitList(audios);
                storedMediaList = new ArrayList<>(audios);
                setAudioPlaylist();
            }
        });
    }

    private LiveData<List<Audio>> getSortedListLiveData() {
        return switch (sortBy) {
            case "Date" -> isAscending ? audioDao.getListByDate() : audioDao.getListByDateDesc();
            case "Size" -> isAscending ? audioDao.getListBySize() : audioDao.getListBySizeDesc();
            case "Length" -> isAscending ? audioDao.getListByDuration() : audioDao.getListByDurationDesc();
            default -> isAscending ? audioDao.getListByName() : audioDao.getListByNameDesc();
        };
    }

    private void setDetailsVisibility() {
        adapter.setDetailsVisibility(isPath_Visible, isSize_Visible, isDate_Visible, isDur_Visible, isDur_OnThumbnail);
        saveDetailsButtonVisibility();
    }


    private void setBackground(Button button, boolean b) {
        if (!b)
            button.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.toggle_notselected));
        else
            button.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.togglebtn_selected));
    }

    private void setBackground_LayoutButtons(boolean isList) {
        if (isList) layout_List.setChecked(true);
        else layout_Grid.setChecked(true);
        this.isList = isList;
    }

    private void setBackground_SortButtons(String sortBy_Button) {
        List<MaterialButton> sortButtons = Arrays.asList(ByName_sort, BySize_sort, ByLength_sort, ByDate_sort);

        // Set all buttons to false first
        for (MaterialButton button : sortButtons) {
            button.setChecked(false);
        }

        // Set the selected button to true
        switch (sortBy_Button) {
            case "Name":   ByName_sort.setChecked(true);   break;
            case "Size":   BySize_sort.setChecked(true);   break;
            case "Length": ByLength_sort.setChecked(true); break;
            case "Date":   ByDate_sort.setChecked(true);   break;
        }

        sortBy = sortBy_Button;
    }

    private void setBackground_AscDesc_Buttons(boolean isAscending) {
        if (isAscending) Asc_sort.setChecked(true);
        else Desc_sort.setChecked(true);
        this.isAscending = isAscending;
    }

    private void setBackground_DetailsButtons() {
        path_Details.setChecked(isPath_Visible);
        size_Details.setChecked(isSize_Visible);
        date_Details.setChecked(isDate_Visible);
        dur_Details.setChecked(isDur_Visible);
        durThumbnail_Details.setChecked(isDur_OnThumbnail);
    }

    private void loadDetailsButtonVisibility() {
        isPath_Visible = settingsPrefs.getBoolean("path", false);
        isDate_Visible = settingsPrefs.getBoolean("date", true);
        isSize_Visible = settingsPrefs.getBoolean("size", true);
        isDur_Visible = settingsPrefs.getBoolean("dur", true);
        isDur_OnThumbnail = settingsPrefs.getBoolean("isDur_Thumbnail", true);
    }

    private void saveDetailsButtonVisibility() {
        settingsPrefsEditor.putBoolean("path", isPath_Visible);
        settingsPrefsEditor.putBoolean("date", isDate_Visible);
        settingsPrefsEditor.putBoolean("size", isSize_Visible);
        settingsPrefsEditor.putBoolean("dur", isDur_Visible);
        settingsPrefsEditor.putBoolean("isDur_Thumbnail", isDur_OnThumbnail);
        settingsPrefsEditor.apply();
    }


    private void showMenuLayout() {
        Log.d("showMenuLayout", "showMenuLayout: YES");
        animationLibs.slideInRight(menu_Container, 250,
                () -> {
                    switch_frag = true;
                    switch_isEnd = false;
                },
                () -> switch_isEnd = true);
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
            }

            @Override
            public void onAnimationRepeat (Animation animation){}
        });
    }

    private void resetMoreContainerViews() {
        loadDetailsButtonVisibility();
        sortBy = settingsPrefs.getString("sortBy", "Name");
        isAscending = settingsPrefs.getBoolean("isAscending", true);
        setBackground_SortButtons(sortBy);
        setBackground_AscDesc_Buttons(isAscending);
        setBackground_DetailsButtons();
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
            mediaList = new ArrayList<>(queryMediaFiles());

            if (storedMediaList == null || storedMediaList.isEmpty()) {
                isFilesStored = false;

                // If no media files in preferences, query media files
                audioDao.insertAll(mediaList);
            }
            else {
                isFilesStored = true;
                Check_And_Update_Files();
            }

            if (storedMediaList != null) {
                for (Audio v : storedMediaList) {
                    getMediaDetails(v);
                }
            }
        });
    }

    private List<Audio> queryMediaFiles() {
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

        if (audioCursor != null) {
            mediaList.addAll(processCursor(audioCursor));
            Log.d(TAG, "Audio list Count: " + audioCursor.getCount());
            audioCursor.close();
        }

//        Log.d(TAG, "Audio list: " + mediaList);

        return mediaList;
    }

    private List<Audio> processCursor(Cursor cursor) {
        List<Audio> mediaList = new ArrayList<>();

        int idInd = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
        int filePathInd = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        int displayNameInd = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
        int dateAddedInd = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);

        if (idInd != -1 && filePathInd != -1 && displayNameInd != -1 && dateAddedInd != -1) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(idInd);
                String filePath = cursor.getString(filePathInd);
                String displayName = cursor.getString(displayNameInd);
                String date = cursor.getString(dateAddedInd);

                String audioUri = String.valueOf(ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id));

                FilesName.add(displayName);
                FilesPath.add(filePath);
                FilesDateAdded.add(date);

//                storedMediaList = loadMediaListFromPreferences();

                Audio media = new Audio(audioUri, displayName, filePath, date,false);
                mediaList.add(media);
            }
        }

        return mediaList;
    }


    private void Check_And_Update_Files() {
        // For Deletion
        List<String> newListUris = new ArrayList<>();

        for (Audio m : mediaList) {
            newListUris.add(m.getUri());
        }

        audioDao.deleteAllByUris(newListUris);

        // For Insertion
        for (Audio audio : mediaList) {
            Audio existing = audioDao.getByUri(audio.getUri());

            if (existing == null) {
                // New file → insert
                audioDao.insertAll(Collections.singletonList(audio));
            }
            else {
//                // File exists → check if changed
                if (!existing.getPath().equals(audio.getPath()) ||
                        !existing.getName().equals(audio.getName())) {
                    audioDao.update(audio); // rename/update case
                }
            }
        }
    }

    private void getMediaDetails(Audio media) {
        if (media.getDuration() == null || media.getSize() == null) {
            new FFmpegMetadataRetriever(requireContext(), media.getPath(), new FFmpegMetadataRetriever.MetadataCallback() {
                @Override
                public void onMetadataReady(FFmpegMetadataRetriever retriever) {
                    try {
                        Long sizeInBytes = retriever.getFileSize();
                        Long duration = retriever.getDuration();

                        // Setting all Values
                        media.setSize(sizeInBytes);
                        media.setDuration(duration);

                        Log.d("MediaExtractor", "File Size: " + getFormattedFileSize(media.getSize()) + " MB, Duration: " + MillisToTime(duration));

                        executorService.execute(() -> audioDao.update(media));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void setAudioPlaylist() {
        if (storedMediaList != null) {
            List<MediaItem> mediaItemList = new ArrayList<>();

            for (Audio v : storedMediaList) {
                MyMediaItem mediaItem = new MyMediaItem(v.getName(), v.getPath());
                mediaItemList.add(mediaItem.toExoPlayerMediaItem());
            }
            MediaRepository.getInstance().setAudioPlaylist(mediaItemList);
        }
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