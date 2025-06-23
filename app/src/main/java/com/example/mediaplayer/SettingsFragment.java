package com.example.mediaplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.Switch;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class SettingsFragment extends Fragment {

    private View menu_Container;
    private ImageButton menu_Button;
    private ImageButton search_Button;
    private Switch mode_Switch;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        search_Button = view.findViewById(R.id.search_button);
        menu_Container = view.findViewById(R.id.MenuContainer);
        menu_Button = view.findViewById(R.id.menu_button);
        mode_Switch = view.findViewById(R.id.mode_switch);

        setModeSwitch();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        menu_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("showMenuLayout", "showMenuLayout: YES");
                showMenuLayout();
            }
        });

        mode_Switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                setThemeMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                setThemeMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }

    private void setModeSwitch() {
        sharedPreferences = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        mode_Switch.setChecked(sharedPreferences.getBoolean("mode_switch", true));
    }

    private void setThemeMode(int mode) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        int savedMode = sharedPreferences.getInt("theme_mode", -1);

        if (savedMode == mode) {
            Log.d("theme_mode", "Theme mode is already set. No need to restart.");
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("theme_mode", mode);

        editor.putBoolean("mode_switch", mode == 2);

        editor.apply();

        Log.d("theme_mode", "New theme mode set: " + mode);

        FilesListActivity.isThemeChanged = true;

        AppCompatDelegate.setDefaultNightMode(mode);
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

    public void saveMediaListToPreferences(List<Video> mediaList) {
        SharedPreferences prefs = requireContext().getSharedPreferences("MediaPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mediaList);
        editor.putString("mediaList", json);
        editor.apply();
    }

    public List<Video> loadMediaListFromPreferences() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MediaPrefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("mediaList", null);
        Type type = new TypeToken<ArrayList<Video>>() {}.getType();
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

}