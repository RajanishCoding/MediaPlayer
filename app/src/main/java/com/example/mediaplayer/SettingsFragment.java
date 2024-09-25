package com.example.mediaplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
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


public class SettingsFragment extends Fragment {


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



        return view;
    }



    public void saveMediaListToPreferences(List<Media> mediaList) {
        SharedPreferences prefs = requireContext().getSharedPreferences("MediaPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mediaList);
        editor.putString("mediaList", json);
        editor.apply();
    }

    public List<Media> loadMediaListFromPreferences() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MediaPrefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("mediaList", null);
        Type type = new TypeToken<ArrayList<Media>>() {}.getType();
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