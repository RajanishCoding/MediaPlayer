package com.example.mediaplayer.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mediaplayer.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.skydoves.colorpickerview.ActionMode;

import java.util.Arrays;
import java.util.List;

public class SettingPlayerFragment extends Fragment {

    private ImageButton backB;

    private LinearLayout swipeEnableLayout;
    private MaterialSwitch swipeEnableSwitch;
    private LinearLayout seekEnableLayout;
    private MaterialSwitch seekEnableSwitch;
    private LinearLayout playEnableLayout;
    private MaterialSwitch playEnableSwitch;

    private AutoCompleteTextView seekDurationDropdown;

    private MaterialButtonToggleGroup seekSwipeToggle;
    private MaterialButton seekSwipeSlowB;
    private MaterialButton seekSwipeNormalB;
    private MaterialButton seekSwipeFastB;

    private MaterialButtonToggleGroup verticalSwipeToggle;
    private MaterialButton verticalSwipeSlowB;
    private MaterialButton verticalSwipeNormalB;
    private MaterialButton verticalSwipeFastB;

    private Slider timeoutSlider;
    private TextView timeoutText;

    private LinearLayout remainingTimeLayout;
    private MaterialSwitch remainingTimeSwitch;

    private boolean isSwipeEnable;
    private boolean isSeekEnable;
    private boolean isPlayEnable;
    private int seekDuration;
    private int seekSwipeValue;
    private int verticalSwipeValue;
    private int hideTimeout;
    private boolean isRemainingTimeEnable;

//    private final int[] seekDurationList = new int[]{5, 10, 15, 20, 30, 40, 50, 60};
//    private final List<String> seekDurationListDropdown = Arrays.asList("5 seconds", "10 seconds", "15 seconds", "20 seconds", "30 seconds", "40 seconds", "50 seconds", "60 seconds");
    private final List<String> seekDurationListDropdown = Arrays.asList("5", "10", "15", "20", "30", "40", "50", "60");

    private SharedPreferences playerPrefs;
    private SharedPreferences.Editor playerPrefsEditor;


    public SettingPlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting_player, container, false);

        backB = view.findViewById(R.id.backB);

        swipeEnableLayout = view.findViewById(R.id.swipeEnable);
        swipeEnableSwitch = view.findViewById(R.id.swipeEnableSwitch);
        seekEnableLayout = view.findViewById(R.id.seekEnable);
        seekEnableSwitch = view.findViewById(R.id.seekEnableSwitch);
        playEnableLayout = view.findViewById(R.id.playEnable);
        playEnableSwitch = view.findViewById(R.id.playEnableSwitch);

        seekDurationDropdown = view.findViewById(R.id.seekDurationDropdown);

        seekSwipeToggle = view.findViewById(R.id.seekSwip);
        seekSwipeSlowB = view.findViewById(R.id.seekSwipeSlow);
        seekSwipeNormalB = view.findViewById(R.id.seekSwipeNormal);
        seekSwipeFastB = view.findViewById(R.id.seekSwipeFast);

        verticalSwipeToggle = view.findViewById(R.id.verticalSwipeToggle);
        verticalSwipeSlowB = view.findViewById(R.id.verticalSwipeSlow);
        verticalSwipeNormalB = view.findViewById(R.id.verticalSwipeNormal);
        verticalSwipeFastB = view.findViewById(R.id.verticalSwipeFast);

        timeoutSlider = view.findViewById(R.id.timeoutSlider);
        timeoutText = view.findViewById(R.id.timeoutText);

        remainingTimeLayout = view.findViewById(R.id.remainingTime);
        remainingTimeSwitch = view.findViewById(R.id.remainingTimeSwitch);

        ArrayAdapter<String> fontDropdownAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, seekDurationListDropdown);
        seekDurationDropdown.setAdapter(fontDropdownAdapter);

        playerPrefs = requireContext().getSharedPreferences("PlayerSetting", Context.MODE_PRIVATE);
        playerPrefsEditor = playerPrefs.edit();

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


        swipeEnableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isSwipeEnable = b;
                playerPrefsEditor.putBoolean("isSwipe", b).apply();
            }
        });
        swipeEnableLayout.setOnClickListener(v -> swipeEnableSwitch.setChecked(!isSwipeEnable));

        seekEnableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isSeekEnable = b;
                playerPrefsEditor.putBoolean("isSeek", b).apply();
            }
        });
        seekEnableLayout.setOnClickListener(v -> seekEnableSwitch.setChecked(!isSeekEnable));

        playEnableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isPlayEnable = b;
                playerPrefsEditor.putBoolean("isPlay", b).apply();
            }
        });
        playEnableLayout.setOnClickListener(v -> playEnableSwitch.setChecked(!isPlayEnable));

        seekDurationDropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                seekDuration = Integer.parseInt(seekDurationListDropdown.get(i));
                seekDurationDropdown.setText(seekDuration + " seconds", false);
                playerPrefsEditor.putInt("seekDuration", seekDuration).apply();
            }
        });

        seekSwipeToggle.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (checkedId == R.id.seekSwipeSlow) {
                    setCheckedButtonsIcon(seekSwipeSlowB, isChecked);
                    if (isChecked) {
                        seekSwipeValue = 0;
                        playerPrefsEditor.putInt("seekSwipeValue", 0).apply();
                    }
                }
                else if (checkedId == R.id.seekSwipeNormal) {
                    setCheckedButtonsIcon(seekSwipeNormalB, isChecked);
                    if (isChecked) {
                        seekSwipeValue = 1;
                        playerPrefsEditor.putInt("seekSwipeValue", 1).apply();
                    }
                }
                else if (checkedId == R.id.seekSwipeFast) {
                    setCheckedButtonsIcon(seekSwipeFastB, isChecked);
                    if (isChecked) {
                        seekSwipeValue = 2;
                        playerPrefsEditor.putInt("seekSwipeValue", 2).apply();
                    }
                }
            }
        });

        verticalSwipeToggle.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (checkedId == R.id.verticalSwipeSlow) {
                    setCheckedButtonsIcon(verticalSwipeSlowB, isChecked);
                    if (isChecked) {
                        verticalSwipeValue = 0;
                        playerPrefsEditor.putInt("verticalSwipeValue", 0).apply();
                    }
                }
                else if (checkedId == R.id.verticalSwipeNormal) {
                    setCheckedButtonsIcon(verticalSwipeNormalB, isChecked);
                    if (isChecked) {
                        verticalSwipeValue = 1;
                        playerPrefsEditor.putInt("verticalSwipeValue", 1).apply();
                    }
                }
                else if (checkedId == R.id.verticalSwipeFast) {
                    setCheckedButtonsIcon(verticalSwipeFastB, isChecked);
                    if (isChecked) {
                        verticalSwipeValue = 2;
                        playerPrefsEditor.putInt("verticalSwipeValue", 2).apply();
                    }
                }
            }
        });

        timeoutSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                int val = (int) value;
                hideTimeout = val;
                timeoutText.setText(val + "s");
                playerPrefsEditor.putInt("hideTimeout", val).apply();
            }
        });

        remainingTimeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isRemainingTimeEnable = b;
                playerPrefsEditor.putBoolean("isRemainingTimeEnable", b).apply();
            }
        });
        remainingTimeLayout.setOnClickListener(v -> remainingTimeSwitch.setChecked(!isRemainingTimeEnable));

        setValuesViewsAtStart();
    }

    private void setValuesViewsAtStart() {
        isSwipeEnable = playerPrefs.getBoolean("isSwipe", true);
        isSeekEnable = playerPrefs.getBoolean("isSeek", true);
        isPlayEnable = playerPrefs.getBoolean("isPlay", true);
        seekDuration = playerPrefs.getInt("seekDuration", 10);
        seekSwipeValue = playerPrefs.getInt("seekSwipeValue", 1);
        verticalSwipeValue = playerPrefs.getInt("verticalSwipeValue", 1);
        hideTimeout = playerPrefs.getInt("hideTimeout", 5);
        isRemainingTimeEnable = playerPrefs.getBoolean("isRemainingTimeEnable", false);

        swipeEnableSwitch.setChecked(isSwipeEnable);
        seekEnableSwitch.setChecked(isSeekEnable);
        playEnableSwitch.setChecked(isPlayEnable);
        seekDurationDropdown.setText(seekDuration + " seconds", false);

        seekSwipeToggle.check(seekSwipeValue == 0 ? R.id.seekSwipeSlow : seekSwipeValue == 1 ? R.id.seekSwipeNormal : R.id.seekSwipeFast);
        verticalSwipeToggle.check(verticalSwipeValue == 0 ? R.id.verticalSwipeSlow : verticalSwipeValue == 1 ? R.id.verticalSwipeNormal : R.id.verticalSwipeFast);

        timeoutSlider.setValue(hideTimeout);
        remainingTimeSwitch.setChecked(isRemainingTimeEnable);
    }

    private void setCheckedButtonsIcon(MaterialButton btn, boolean isChecked) {
        if (isChecked) btn.setIconResource(androidx.media3.session.R.drawable.media3_icon_check_circle_filled);
        else btn.setIcon(null);
    }
}