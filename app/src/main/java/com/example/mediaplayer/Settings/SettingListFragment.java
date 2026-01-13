package com.example.mediaplayer.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.example.mediaplayer.R;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingListFragment extends Fragment {

    private ImageButton backB;

    private LinearLayout newMediaLayout;
    private MaterialSwitch newMediaSwitch;
    private LinearLayout lastPlayedLayout;
    private MaterialSwitch lastPlayedSwitch;
    private LinearLayout floatingResumeLayout;
    private MaterialSwitch floatingResumeSwitch;

    private boolean isNewMediaEnabled;
    private boolean isLastPlayedEnabled;
    private boolean isFloatingResumeEnabled;

    private SharedPreferences listPrefs;
    private SharedPreferences.Editor listPrefsEditor;


    public SettingListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting_list, container, false);

        backB = view.findViewById(R.id.backB);

        newMediaLayout = view.findViewById(R.id.newMediaLayout);
        newMediaSwitch = view.findViewById(R.id.newMediaSwitch);
        lastPlayedLayout = view.findViewById(R.id.lastPlayedLayout);
        lastPlayedSwitch = view.findViewById(R.id.lastPlayedSwitch);
        floatingResumeLayout = view.findViewById(R.id.floatingResumeLayout);
        floatingResumeSwitch = view.findViewById(R.id.floatingResumeSwitch);

        listPrefs = requireContext().getSharedPreferences("ListSetting", Context.MODE_PRIVATE);
        listPrefsEditor = listPrefs.edit();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        backB.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        requireActivity()
                .getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        getParentFragmentManager().popBackStack();
                    }
                });
        

        newMediaSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isNewMediaEnabled = b;
                listPrefsEditor.putBoolean("isNewMediaEnabled", b).apply();
            }
        });
        newMediaLayout.setOnClickListener(v -> newMediaSwitch.setChecked(!isNewMediaEnabled));

        lastPlayedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isLastPlayedEnabled = b;
                listPrefsEditor.putBoolean("isLastPlayedEnabled", b).apply();
            }
        });
        lastPlayedLayout.setOnClickListener(v -> lastPlayedSwitch.setChecked(!isLastPlayedEnabled));

        floatingResumeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isFloatingResumeEnabled = b;
                listPrefsEditor.putBoolean("isFloatingResumeEnabled", b).apply();
            }
        });
        floatingResumeLayout.setOnClickListener(v -> floatingResumeSwitch.setChecked(!isFloatingResumeEnabled));

        setValuesViewsAtStart();
    }

    private void setValuesViewsAtStart() {
        isNewMediaEnabled = listPrefs.getBoolean("isNewMediaEnabled", true);
        isLastPlayedEnabled = listPrefs.getBoolean("isLastPlayedEnabled", true);
        isFloatingResumeEnabled = listPrefs.getBoolean("isFloatingResumeEnabled", true);

        newMediaSwitch.setChecked(isNewMediaEnabled);
        lastPlayedSwitch.setChecked(isLastPlayedEnabled);
        floatingResumeSwitch.setChecked(isFloatingResumeEnabled);
    }
}