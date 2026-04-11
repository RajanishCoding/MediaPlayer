package com.example.mediaplayer.Video;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.ContentObserver;
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
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.example.mediaplayer.Extra.AnimationLibs;
import com.example.mediaplayer.Extra.FFmpegMetadataRetriever;
import com.example.mediaplayer.Extra.InfoDialog;
import com.example.mediaplayer.Extra.MediaRepository;
import com.example.mediaplayer.Extra.MyBottomSheet;
import com.example.mediaplayer.Extra.MyMediaItem;
import com.example.mediaplayer.Extra.ConsentDialog;
import com.example.mediaplayer.PlayerActivity;
import com.example.mediaplayer.R;
import com.example.mediaplayer.Room.RoomDB;
import com.example.mediaplayer.Room.VideoDao;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class VideoFragment extends Fragment {

    private Context context;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private VideoAdapter adapter;

    private Toolbar toolbar_main;
    private Toolbar toolbar_selection;
    private Toolbar toolbar_search;
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
    private List<Video> temporaryMediaList;

    private Thread thread;

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
    private MaterialButton ByResol_sort;
    private MaterialButton ByFps_sort;
    private MaterialButtonToggleGroup sortOrderToggle;
    private MaterialButton Asc_sort;
    private MaterialButton Desc_sort;

    private MaterialButton path_Details;
    private MaterialButton date_Details;
    private MaterialButton resol_Details;
    private MaterialButton fps_Details;
    private MaterialButton size_Details;
    private MaterialButton dur_Details;
    private MaterialSwitch durThumbnail_Details;

    private MaterialButtonToggleGroup layoutToggle;
    private MaterialButton layout_List;
    private MaterialButton layout_Grid;

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
    private int lastPlayedPosition;

    private View bottomSheet;
    private BottomSheetBehavior<View> behavior;

    private ImageButton toolbarSel_back;
    private ImageButton toolbarSel_play;
    private ImageButton toolbarSel_sel;
    private TextView toolbarSel_text;
    private boolean isAllSelected;

    private MaterialButton bottomSel_ren;
    private MaterialButton bottomSel_del;
    private MaterialButton bottomSel_share;

    private boolean isSearchToolbarShowing;
    private ImageButton toolbarS_backB;
    private ImageButton toolbarS_clearB;
    private TextInputLayout toolbarS_inputLayout;
    private TextInputEditText toolbarS_inputText;

    private List<Uri> selectedListUris;
    private int position;
    
    private AnimationLibs animationLibs;

    private SharedPreferences settingsPrefs;
    private SharedPreferences.Editor settingsPrefsEditor;

    private SharedPreferences listPrefs;
    private SharedPreferences.Editor listPrefsEditor;

    private SharedPreferences playerPrefs;
    private SharedPreferences.Editor playerPrefsEditor;

    private VideoDao videoDao;

    public VideoFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView = view.findViewById(R.id.recyclerViewVideo);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        toolbar_main = view.findViewById(R.id.toolbar);
        toolbar_selection = view.findViewById(R.id.toolbar_sel);
        toolbar_search = view.findViewById(R.id.toolbar_search);
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

        sortOrderToggle = view.findViewById(R.id.sortOrderToggle);
        Asc_sort = view.findViewById(R.id.sort_Asc);
        Desc_sort = view.findViewById(R.id.sort_Desc);

        path_Details = view.findViewById(R.id.details_Path);
        date_Details = view.findViewById(R.id.details_Date);
        resol_Details = view.findViewById(R.id.details_Resol);
        fps_Details = view.findViewById(R.id.details_Fps);
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

        toolbarS_backB = view.findViewById(R.id.toolbarSearch_backB);
        toolbarS_clearB = view.findViewById(R.id.toolbarSearch_clearB);
        toolbarS_inputLayout = view.findViewById(R.id.toolbarSearch_inputLayout);
        toolbarS_inputText = view.findViewById(R.id.toolbarSearch_inputText);


        bottomSheet = view.findViewById(R.id.bottomsheet);
        behavior = BottomSheetBehavior.from(bottomSheet);

        context = requireContext();
        executorService = Executors.newFixedThreadPool(1);

        settingsPrefs = requireContext().getSharedPreferences("VideoListSettings", Context.MODE_PRIVATE);
        settingsPrefsEditor = settingsPrefs.edit();

        listPrefs = requireContext().getSharedPreferences("ListSetting", Context.MODE_PRIVATE);
        listPrefsEditor = listPrefs.edit();

        playerPrefs = requireContext().getSharedPreferences("PlayerSetting", Context.MODE_PRIVATE);
        playerPrefsEditor = playerPrefs.edit();

        videoDao = RoomDB.getDatabase(context).videoDao();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            File[] files = new File("/storage/emulated/0").listFiles();
            if (files != null) {
                for (File file : files) {
                    MediaScannerConnection.scanFile(
                            getContext(),
                            new String[]{file.getPath()},
                            new String[]{"video/*"},
                            (path, uri) -> {

                            }
                    );
                }
            }
            Load_Or_Query_MediaList();
            swipeRefreshLayout.setRefreshing(false);
        });

        animationLibs = new AnimationLibs();

        adapter = new VideoAdapter(requireContext(), mediaList, getChildFragmentManager());
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);

        temporaryMediaList = new ArrayList<>();

        boolean isLastPlayButtonEnabled = listPrefs.getBoolean("isFloatingResumeEnabled", true);
        lastPlay_Button.setVisibility(isLastPlayButtonEnabled ? View.VISIBLE : View.GONE);

//      Load at Start
        sortBy = settingsPrefs.getString("sortBy", "Name");
        isAscending = settingsPrefs.getBoolean("isAscending", true);
        isList = settingsPrefs.getBoolean("isList", true);

//      Setting at start
        setListObserver(true);

//      Loading at Start
        loadDetailsButtonVisibility();

//      Notifying Adapter at Start
        setDetailsVisibility();

//      Setting BG at Start
        setBackground_LayoutButtons(isList);
        setBackground_SortButtons(sortBy);
        setBackground_AscDesc_Buttons(isAscending);
        setBackground_DetailsButtons();

        adapter.addSelectionListener(new VideoAdapter.SelectionListener() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onItemClicked(int position, Video video) {
//                new Thread(() -> videoDao.markAsPlayed(video.getPath())).start();
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("Name", video.getName());
                intent.putExtra("Path", video.getPath());
                intent.putExtra("Uri", video.getUri());
                intent.putExtra("isVideo", video.isVideo());
                intent.putExtra("currentIndex", position);
                Log.d("isVideoFile", "onClick: " + video.isVideo());
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
            public void onCountChanged(int counts, List<Uri> list, int position) {
                toolbarSel_text.setText(String.format(Locale.getDefault(), "%d/%d selected", counts, storedMediaList.size()));
                bottomSel_del.setEnabled(counts != 0);
                bottomSel_ren.setEnabled(counts == 1);
                bottomSel_share.setEnabled(counts != 0);
                selectedListUris = new ArrayList<>(list);
                VideoFragment.this.position = position;
            }

            @Override
            public void onOptionButtonClicked(int p, Video video, String name) {
                MyBottomSheet sheet = new MyBottomSheet(p, name);
                sheet.show(getChildFragmentManager(), sheet.getTag());

                sheet.addBottomSheetDialogListeners(new MyBottomSheet.BottomSheetDialogListeners() {
                    @OptIn(markerClass = UnstableApi.class)
                    @Override
                    public void onPlayCLickListener(int position) {
                        Intent intent = new Intent(context, PlayerActivity.class);
                        intent.putExtra("Name", video.getName());
                        intent.putExtra("Path", video.getPath());
                        intent.putExtra("isVideo", video.isVideo());
                        intent.putExtra("currentIndex", position);
                        Log.d("isVideoFile", "onClick: " + video.isVideo());
                        context.startActivity(intent);
                    }

                    @Override
                    public void onInfoClickListener(int position) {
                        InfoDialog dialog = new InfoDialog(video, true);
                        dialog.show(getParentFragmentManager(), "infoDialog");
                    }

                    @Override
                    public void onRenameClickListener(int position) {
                        List<Uri> uris = new ArrayList<>();
                        uris.add(Uri.parse(storedMediaList.get(position).getUri()));
                        ConsentDialog consent = new ConsentDialog(2, uris, storedMediaList.get(position).getName());
                        consent.show(getChildFragmentManager(), sheet.getTag());
                    }

                    @Override
                    public void onDeleteClickListener(int p) {
                        List<Uri> uris = new ArrayList<>();
                        uris.add(Uri.parse(storedMediaList.get(p).getUri()));
                        ConsentDialog consent = new ConsentDialog(1, uris, storedMediaList.get(p).getName());
                        consent.show(getChildFragmentManager(), sheet.getTag());
                    }

                    @Override
                    public void onShareClickListener(int position) {
                        Uri uri = Uri.parse(storedMediaList.get(position).getUri());
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("video/*");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(shareIntent, "Share via"));
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

        search_Button.setOnClickListener(v -> {
            animationLibs.fadeIn(toolbar_search, 350, null, () -> {
                toggleKeyboard(true, toolbarS_inputText);
                isSearchToolbarShowing = true;
            });
        });

        toolbarS_inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                Toast.makeText(requireContext(), "Text : " + charSequence, Toast.LENGTH_SHORT).show();
                if (charSequence.toString().isEmpty()) {
                    adapter.submitList(storedMediaList);
                    recyclerView.scrollToPosition(0);
                    return;
                }

                List<Video> list = getSearchItemsList(charSequence.toString());
                adapter.submitList(list);
                recyclerView.smoothScrollToPosition(0);
                Log.d(TAG, "onTextChanged: " + list);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        toolbarS_backB.setOnClickListener(v -> {
            isSearchToolbarShowing = false;
            toggleKeyboard(false, toolbarS_inputText);
            animationLibs.fadeOut(toolbar_search, 350, null, () -> toolbar_search.setVisibility(View.GONE));
        });

        toolbarS_clearB.setOnClickListener(v -> {
            toolbarS_inputText.setText("");
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
        bottomBarListeners();

        apply_Button.setOnClickListener(v -> {
            settingsPrefsEditor.putString("sortBy", sortBy);
            settingsPrefsEditor.putBoolean("isAscending", isAscending);
            settingsPrefsEditor.apply();

            setDetailsVisibility();
            setListObserver(false);
            hideMenuLayout();
            Log.d("DUR_onThumb", "onViewCreated: " + isDur_OnThumbnail);
        });

        cancel_Button.setOnClickListener(v -> {
            hideMenuLayout();
            resetMoreContainerViews();
        });

        lastPlay_Button.setOnClickListener(new View.OnClickListener() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onClick(View v) {
                String name = listPrefs.getString("lastPlayedVideoName", "");
                String path = listPrefs.getString("lastPlayedVideoPath", "");
                Boolean isVideo = listPrefs.getBoolean("lastPlayedFile_isVideo", false);

                new Thread(() -> {
                    if (videoDao.isPathExists(path)) {
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
                MediaStore.Video.Media.getContentUri("external"), true,
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
                else if (isSearchToolbarShowing) {
                    isSearchToolbarShowing = false;
                    toggleKeyboard(false, toolbarS_inputText);
                    animationLibs.fadeOut(toolbar_search, 350, null, () -> toolbar_search.setVisibility(View.GONE));
                }
                else{
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }


    public void toggleKeyboard(boolean show, View targetView) {
        Window window = requireActivity().getWindow();
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());

        if (show) {
            targetView.requestFocus();
            controller.show(WindowInsetsCompat.Type.ime());
        } else {
            controller.hide(WindowInsetsCompat.Type.ime());
            targetView.clearFocus();
        }
    }


    private List<Video> getSearchItemsList(String str) {
        List<Video> list = new ArrayList<>();
        String[] words = str.trim().toLowerCase().split("\\s+");

        for (Video v : storedMediaList) {
            boolean isPresent = true;
            String name = v.getName().toLowerCase();

            for (String word : words) {
                if (!name.contains(word)) {
                    isPresent = false;
                    break;
                }
            }

            if (isPresent) list.add(v);
        }

        return list;
    }

    private void selectAllItems() {
        for (Video v : storedMediaList) {
            v.isSelected = true;
        }
        adapter.notifyDataSetChanged();

        adapter.selectionCounts = storedMediaList.size();
        toolbarSel_text.setText(String.format(Locale.getDefault(), "%d/%d selected", adapter.selectionCounts, adapter.selectionCounts));
//        adapter.listener.onCountChanged(storedMediaList.size());
    }

    private void deselectAllItems() {
        for (Video v : storedMediaList) {
            v.isSelected = false;
        }
        adapter.notifyDataSetChanged();

        adapter.selectionCounts = 0;
        toolbarSel_text.setText(String.format(Locale.getDefault(), "%d/%d selected", 0, storedMediaList.size()));
//        adapter.listener.onCountChanged(0);
    }

    private void clearSelection() {
        for (Video v : storedMediaList) {
            v.isSelected = false;
        }
        adapter.isSelectionMode = false;
        adapter.selectionCounts = 0;
        adapter.selectedList = new ArrayList<>();
        adapter.notifyDataSetChanged();
//        adapter.submitList(storedMediaList);
        adapter.listener.onSelectionEnds();
    }

    private void bottomBarListeners() {
        bottomSel_ren.setOnClickListener(v -> {
            ConsentDialog consent = new ConsentDialog(2, selectedListUris, storedMediaList.get(position).getName());
            consent.show(getChildFragmentManager(), "tag");
            clearSelection();
        });

        bottomSel_del.setOnClickListener(v -> {
            ConsentDialog consent = new ConsentDialog(1, selectedListUris, storedMediaList.get(position).getName());
            consent.show(getChildFragmentManager(), "tag");
            clearSelection();
        });

        bottomSel_share.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.setType("video/*");
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, new ArrayList<>(selectedListUris));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share Via"));
            clearSelection();
        });
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
        ByResol_sort.setOnClickListener(v -> setBackground_SortButtons("Resol"));
        ByFps_sort.setOnClickListener(v -> setBackground_SortButtons("Fps"));

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

        resol_Details.setOnClickListener(v -> {
            isResol_Visible = !isResol_Visible;
            resol_Details.setChecked(isResol_Visible);
        });

        fps_Details.setOnClickListener(v -> {
            isFps_Visible = !isFps_Visible;
            fps_Details.setChecked(isFps_Visible);
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

        durThumbnail_Details.setOnCheckedChangeListener((buttonView, isChecked) -> isDur_OnThumbnail = isChecked);
    }

    private boolean isStoredListAvailable = false;

    private void setListObserver(boolean isFirstTime) {
        LiveData<List<Video>> liveData = getSortedListLiveData();

        liveData.removeObservers(getViewLifecycleOwner());

        liveData.observe(getViewLifecycleOwner(), new Observer<List<Video>>() {
            @Override
            public void onChanged(List<Video> videos) {
                if (videos.isEmpty()) foundText.setVisibility(View.VISIBLE);
                else foundText.setVisibility(View.GONE);

                String lastPlayedPath = listPrefs.getString("lastPlayedVideoPath", "");
                lastPlayedPosition = 0;

                // Last Played Code
                for (int i = 0; i < videos.size(); i++) {
                    Video v = videos.get(i);
                    boolean isMatch = v.getPath().equals(lastPlayedPath);
                    v.setLastPlayed(isMatch);

                    if (isMatch) {
                        lastPlayedPosition = i; // Position for Last Play Floating Button
                    }
                }

                storedMediaList = new ArrayList<>(videos);
                adapter.submitList(videos);

                for (int i = 0; i < storedMediaList.size(); i++) {
                    Video m = videos.get(i);

                    if (m.getDuration() == null || m.getExtraMetadata() == null) {
                        getMediaDetails(m, i);
                    }
                }

                if (!isStoredListAvailable && isFirstTime) {
                    isStoredListAvailable = true;
                    Load_Or_Query_MediaList();
                    Log.d(TAG, "onChanged: " + "yes");
                }

                setVideoPlaylist();
            }
        });
    }

    private LiveData<List<Video>> getSortedListLiveData() {
        return switch (sortBy) {
            case "Date" -> isAscending ? videoDao.getListByDate() : videoDao.getListByDateDesc();
            case "Size" -> isAscending ? videoDao.getListBySize() : videoDao.getListBySizeDesc();
            case "Length" -> isAscending ? videoDao.getListByDuration() : videoDao.getListByDurationDesc();
            case "Resol" -> isAscending ? videoDao.getListByResol() : videoDao.getListByResolDesc();
            case "Fps" -> isAscending ? videoDao.getListByFrame() : videoDao.getListByFrameDesc();
            default -> isAscending ? videoDao.getListByName() : videoDao.getListByNameDesc();
        };
    }


    private void setDetailsVisibility() {
        adapter.setDetailsVisibility(isPath_Visible, isResol_Visible, isFps_Visible, isSize_Visible, isDate_Visible, isDur_Visible, isDur_OnThumbnail);
        saveDetailsButtonVisibility();
    }

    private void setBackground(Button button, boolean b) {
        if (!b)
            button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.btn_color)));
        else
            button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.btn_color_checked)));
    }

    private void setBackground_LayoutButtons(boolean isList) {
        if (isList) layout_List.setChecked(true);
        else layout_Grid.setChecked(true);
        this.isList = isList;
    }

    private void setBackground_SortButtons(String sortBy_Button) {
        List<MaterialButton> sortButtons = Arrays.asList(ByName_sort, BySize_sort, ByLength_sort, ByDate_sort, ByResol_sort, ByFps_sort);

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
            case "Resol":  ByResol_sort.setChecked(true);  break;
            case "Fps":    ByFps_sort.setChecked(true);    break;
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
        resol_Details.setChecked(isResol_Visible);
        fps_Details.setChecked(isFps_Visible);
        size_Details.setChecked(isSize_Visible);
        date_Details.setChecked(isDate_Visible);
        dur_Details.setChecked(isDur_Visible);
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

        animationLibs.slideInRight(menu_Container, 250,
                () -> {
                    switch_frag = true;
                    switch_isEnd = false;
                },
                () -> switch_isEnd = true);
    }

    private void hideMenuLayout() {
        Log.d("showMenuLayout", "showMenuLayout: NO");

        animationLibs.slideOutRight(menu_Container, 250,
                () -> {
                    switch_frag = false;
                    switch_isEnd = false;
                },
                () -> {
                    menu_Container.setVisibility(View.GONE);
                    switch_isEnd = true;
                });
    }

    private void resetMoreContainerViews() {
        sortBy = settingsPrefs.getString("sortBy", "Name");
        isAscending = settingsPrefs.getBoolean("isAscending", true);
        loadDetailsButtonVisibility();
        setBackground_SortButtons(sortBy);
        setBackground_AscDesc_Buttons(isAscending);
        setBackground_DetailsButtons();
    }


    @Override
    public void onStart() {
        super.onStart();

        Log.d("Hello", "Before Load: Yes " + (storedMediaList != null ? storedMediaList.size() : 0));
//        Load_Or_Query_MediaList();
        Log.d("Hello", "After Load: Yes " + (storedMediaList != null ? storedMediaList.size() : 0));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }


    private void Load_Or_Query_MediaList() {
        new Thread(() -> {
            mediaList = new ArrayList<>(queryMediaFiles());

            if (storedMediaList == null || storedMediaList.isEmpty()) {
                isFilesStored = false;

                // If no media files in preferences, query media files
                videoDao.insertAll(mediaList);

//                for (Video v : mediaList) {
//                    getMediaDetails(v);
//                }

                Log.d(TAG, "Load_Or_Query_MediaList: "+ "yes");
            }
            else {
                Log.d(TAG, "Load_Or_Query_MediaList: "+ "no");
                isFilesStored = true;
                Check_And_Update_Files();

//                for (Video v : storedMediaList) {
//                    getMediaDetails(v);
//                }
            }
        }).start();
    }

    private List<Video> queryMediaFiles() {
        List<Video> mediaList = new ArrayList<>();

        String[] videoProjection = new String[]{
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATE_ADDED
        };

        String sortOrder = MediaStore.Video.Media.DISPLAY_NAME + " ASC";

        if (!isAdded()) return mediaList;

        Cursor videoCursor = requireContext().getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                videoProjection,
                null,
                null,
                sortOrder
        );

        if (videoCursor != null) {
            mediaList.addAll(processCursor(videoCursor));
            Log.d(TAG, "Video list Count: " + videoCursor.getColumnCount());
            videoCursor.close();
        }

//        Log.d(TAG, "Video list: " + mediaList);

        return mediaList;
    }

    private List<Video> processCursor(Cursor cursor) {
        List<Video> mediaList = new ArrayList<>();

        int idInd = cursor.getColumnIndex(MediaStore.Video.Media._ID);
        int filePathInd = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
        int displayNameInd = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
        int dateAddedInd = cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED);

        if (idInd != -1 && filePathInd != -1 && displayNameInd != -1 && dateAddedInd != -1) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(idInd);
                String filePath = cursor.getString(filePathInd);
                String displayName = cursor.getString(displayNameInd);
                String date = cursor.getString(dateAddedInd);

                String videoUri = String.valueOf(ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id));

                FilesName.add(displayName);
                FilesPath.add(filePath);
                FilesDateAdded.add(date);

//                storedMediaList = loadMediaListFromPreferences();

                Video media = new Video(videoUri, displayName, filePath, date,  true);
                mediaList.add(media);
            }
        }

        return mediaList;
    }

    private void Check_And_Update_Files() {
        // URIs currently stored in DB
        Set<String> dbUris = new HashSet<>(videoDao.getAllUris());

        // URIs from MediaStore
        Set<String> mediaUris = new HashSet<>();
        for (Video m : mediaList) {
            mediaUris.add(m.getUri());
        }

        Log.d(TAG, "Check_And_Update_Files: " + "yes");

        // Delete files not in storage
        videoDao.deleteAllNotInUris(mediaUris);

        // Insert newly added files
        for (Video m : mediaList) {
            if (!dbUris.contains(m.getUri())) {
                // New file
                videoDao.insert(m);
//                getMediaDetails(m);
            }
            else {
                Video existing = videoDao.getByUri(m.getUri());
                if (existing != null && !existing.getName().equals(m.getName())) {
                    videoDao.update(m);
                }
            }
        }
    }

    private void getMediaDetails(Video media, int pos) {
        if (media.getDuration() == null || media.getExtraMetadata() == null) {
            new FFmpegMetadataRetriever(requireContext(), media.getPath(), new FFmpegMetadataRetriever.MetadataCallback() {
                @Override
                public void onMetadataReady(FFmpegMetadataRetriever retriever, String extraMetadataJsonString) {
                    try {
                        Long sizeInBytes = retriever.getFileSize();

                        Long duration = retriever.getDuration();
                        Integer height = retriever.getResolution();
                        Double fps = retriever.getFps();

                        // Setting all Values
                        media.setSize(sizeInBytes);
                        media.setDuration(duration);
                        media.setResolution(height);
                        media.setFrameRate(fps);
//                        media.setExtraMetadata();

                        requireActivity().runOnUiThread(() -> {
                            adapter.notifyItemChanged(pos);
                        });

                        executorService.execute(() -> {
                            videoDao.update(media);
                            videoDao.setExtraMetadata(media.getPath(), extraMetadataJsonString);
                        });
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void setVideoPlaylist() {
        if (storedMediaList != null) {
            List<MediaItem> mediaItemList = new ArrayList<>();

            for (Video v : storedMediaList) {
                MyMediaItem mediaItem = new MyMediaItem(v.getName(), v.getUri(), v.getPath(), v.isVideo());
                mediaItemList.add(mediaItem.toExoPlayerMediaItem());
            }
            MediaRepository.getInstance().setVideoPlaylist(mediaItemList);
        }
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
        try {
            SharedPreferences prefs = requireContext().getSharedPreferences("MediaPrefs", Context.MODE_PRIVATE);
            Gson gson = new Gson();
            String json = prefs.getString("videoList", null);
            Type type = new TypeToken<ArrayList<Video>>() {
            }.getType();
//            if (json == null) return new ArrayList<>();
            return gson.fromJson(json, type);
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.d("loadmedia", "loadMediaListFromPreferences: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    public static String getFormattedFileSize(Long sizeInBytes) {
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

    public String MillisToTime(Long millis) {
        long hours = (millis / (1000 * 60 * 60)) % 24;
        long minutes = (millis / (1000 * 60)) % 60;
        long seconds = (millis / 1000) % 60;

        if (hours >= 1) {
            return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
    }
}