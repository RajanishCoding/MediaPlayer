package com.example.mediaplayer.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.mediaplayer.FilesListActivity;
import com.example.mediaplayer.R;
import com.example.mediaplayer.Video.Video;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class SettingsFragment extends Fragment {

    private MaterialButtonToggleGroup themeGroup;

    private View playerSettings, audioSettings, subtitleSettings, listSettings, storageSettings;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        themeGroup = view.findViewById(R.id.themeGroup);

        playerSettings = view.findViewById(R.id.playerSetting);
        audioSettings = view.findViewById(R.id.audioSetting);
        subtitleSettings = view.findViewById(R.id.subtitleSetting);
        listSettings = view.findViewById(R.id.listSetting);
        storageSettings = view.findViewById(R.id.systemSetting);

        sharedPreferences = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        setModeGroup();

        setSettingItemsViews();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        menu_Button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d("showMenuLayout", "showMenuLayout: YES");
//                showMenuLayout();
//            }
//        });

        themeGroup.findViewById(R.id.auto).setOnClickListener(v -> {
            setThemeMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        });

        themeGroup.findViewById(R.id.light).setOnClickListener(v -> {
            setThemeMode(AppCompatDelegate.MODE_NIGHT_NO);
        });

        themeGroup.findViewById(R.id.dark).setOnClickListener(v -> {
            setThemeMode(AppCompatDelegate.MODE_NIGHT_YES);
        });

        playerSettings.setOnClickListener(v -> createFragment(new SettingPlayerFragment()));
        audioSettings.setOnClickListener(v -> createFragment(new SettingAudioFragment()));
        subtitleSettings.setOnClickListener(v -> createFragment(new SettingSubtitleFragment()));
        listSettings.setOnClickListener(v -> createFragment(new SettingListFragment()));
        storageSettings.setOnClickListener(v -> createFragment(new SettingGeneralFragment()));

    }

    private void setSettingItemsViews() {
        setSettingsItem(playerSettings, "Player", R.drawable.rounded_smart_display_24);
        setSettingsItem(audioSettings, "Audio", R.drawable.round_music_video_24);
        setSettingsItem(subtitleSettings, "Subtitle", R.drawable.rounded_subtitles_24);
        setSettingsItem(listSettings, "List", R.drawable.rounded_list_alt_24);
        setSettingsItem(storageSettings, "Storage", R.drawable.rounded_info_24);
    }

    private void setModeGroup() {
        int m = sharedPreferences.getInt("theme_mode", -1);
        themeGroup.check(m == 1 ? R.id.light : m == 2 ? R.id.dark : R.id.auto);
    }

    private void setThemeMode(int mode) {
        int savedMode = sharedPreferences.getInt("theme_mode", -1);

        if (savedMode == mode) {
            return;
        }

        editor.putInt("theme_mode", mode);
        editor.apply();

        FilesListActivity.isThemeChanged = true;

        AppCompatDelegate.setDefaultNightMode(mode);
    }

    private void setSettingsItem(View view, String text, int icon) {
        ((TextView) view.findViewById(R.id.itemText)).setText(text);
        ((ImageView) view.findViewById(R.id.itemIconStart)).setImageResource(icon);
    }

    private void createFragment(Fragment fragment) {
                getChildFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainerSetting, fragment)
                        .addToBackStack(null)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
        }
}