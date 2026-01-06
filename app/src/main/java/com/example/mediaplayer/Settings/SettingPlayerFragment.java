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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mediaplayer.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;

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

    private MaterialButtonToggleGroup lockModeToggle;
    private MaterialButton lockModeAllB;
    private MaterialButton lockModeTouchB;

    private LinearLayout remainingTimeLayout;
    private MaterialSwitch remainingTimeSwitch;
    private LinearLayout trackSelectionLayout;
    private MaterialSwitch trackSelectionSwitch;

    private boolean isSwipeEnable;
    private boolean isSeekEnable;
    private boolean isPlayEnable;
    private int seekDuration;
    private boolean isSeekSwipeSlow;
    private boolean isSeekSwipeNormal;
    private boolean isSeekSwipeFast;
    private boolean isVerticalSwipeSlow;
    private boolean isVerticalSwipeNormal;
    private boolean isVerticalSwipeFast;
    private int hideTimeout;
    private int fadeTime;
    private boolean isLockModeAll;
    private boolean isLockModeTouch;
    private boolean isRemainingTimeEnable;
    private boolean isTrackSelectionEnable;

    private final List<String> seekDurationList = Arrays.asList("5", "10", "15", "20", "30", "40", "50", "60");

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

        lockModeToggle = view.findViewById(R.id.lockModeToggle);
        lockModeAllB = view.findViewById(R.id.lockModeFull);
        lockModeTouchB = view.findViewById(R.id.lockModeTouch);

        remainingTimeLayout = view.findViewById(R.id.remainingTime);
        remainingTimeSwitch = view.findViewById(R.id.remainingTimeSwitch);
        trackSelectionLayout = view.findViewById(R.id.trackSelection);
        trackSelectionSwitch = view.findViewById(R.id.trackSelectionSwitch);

        ArrayAdapter<String> fontDropdownAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, seekDurationList);
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

        seekDurationDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                seekDuration = Integer.parseInt(seekDurationList.get(i));
                playerPrefsEditor.putInt("seekDuration", i).apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        seekSwipeToggle.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (checkedId == R.id.seekSwipeSlow) {
                    setCheckedButtonsIcon(seekSwipeSlowB, isChecked);
                    isSeekSwipeSlow = isChecked;
                    playerPrefsEditor.putBoolean("isSeekSlow", isChecked).apply();
                }
                else if (checkedId == R.id.seekSwipeNormal) {
                    setCheckedButtonsIcon(seekSwipeNormalB, isChecked);
                    isSeekSwipeNormal = isChecked;
                    playerPrefsEditor.putBoolean("isSeekNormal", isChecked).apply();
                }
                else if (checkedId == R.id.seekSwipeFast) {
                    setCheckedButtonsIcon(seekSwipeFastB, isChecked);
                    isSeekSwipeFast = isChecked;
                    playerPrefsEditor.putBoolean("isSeekFast", isChecked).apply();
                }
            }
        });

        verticalSwipeToggle.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (checkedId == R.id.verticalSwipeSlow) {
                    setCheckedButtonsIcon(verticalSwipeSlowB, isChecked);
                    isVerticalSwipeSlow = isChecked;
                    playerPrefsEditor.putBoolean("isVerticalSlow", isChecked).apply();
                }
                else if (checkedId == R.id.verticalSwipeNormal) {
                    setCheckedButtonsIcon(verticalSwipeNormalB, isChecked);
                    isVerticalSwipeNormal = isChecked;
                    playerPrefsEditor.putBoolean("isVerticalNormal", isChecked).apply();
                }
                else if (checkedId == R.id.verticalSwipeFast) {
                    setCheckedButtonsIcon(verticalSwipeFastB, isChecked);
                    isVerticalSwipeFast = isChecked;
                    playerPrefsEditor.putBoolean("isVerticalFast", isChecked).apply();
                }
            }
        });

        timeoutSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                int val = (int) value;
                hideTimeout = val;
                timeoutText.setText(String.valueOf(val));
                playerPrefsEditor.putInt("hideTimeout", val).apply();
            }
        });

        lockModeToggle.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (checkedId == R.id.lockModeFull) {
                    setCheckedButtonsIcon(lockModeAllB, isChecked);
                    isLockModeAll = isChecked;
                    playerPrefsEditor.putBoolean("isLockModeAll", isChecked).apply();
                }
                else if (checkedId == R.id.lockModeTouch) {
                    setCheckedButtonsIcon(lockModeTouchB, isChecked);
                    isLockModeTouch = isChecked;
                    playerPrefsEditor.putBoolean("isLockModeTouch", isChecked).apply();
                }
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

        trackSelectionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isTrackSelectionEnable = b;
                playerPrefsEditor.putBoolean("isTrackSelectionEnable", b).apply();
            }
        });
        trackSelectionLayout.setOnClickListener(v -> trackSelectionSwitch.setChecked(!isTrackSelectionEnable));

    }


    private void setCheckedButtonsIcon(MaterialButton btn, boolean isChecked) {
        if (isChecked) btn.setIconResource(androidx.media3.session.R.drawable.media3_icon_check_circle_filled);
        else btn.setIcon(null);
    }
}