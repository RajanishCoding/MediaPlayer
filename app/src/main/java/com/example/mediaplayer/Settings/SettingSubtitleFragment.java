package com.example.mediaplayer.Settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.media3.ui.CaptionStyleCompat;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.mediaplayer.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.tabs.TabLayout;
import com.skydoves.colorpickerview.ActionMode;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorListener;
import com.skydoves.colorpickerview.sliders.AlphaSlideBar;
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar;

public class SettingSubtitleFragment extends Fragment {

    private ImageButton backB;

    private TabLayout subColorTabs;
    private MaterialCardView subColorPickerB;

    private TextView subSizeText;
    private Slider subFontSizeSlider;
    private ImageButton subSizeAddB;
    private ImageButton subSizeMinusB;
    private AutoCompleteTextView subFontDropdown;
    private MaterialButtonToggleGroup subBoldItalicToggleGroup;
    private MaterialButton subBoldB;
    private MaterialButton subItalicB;
    private SwitchMaterial subEnableSwitch;
    private MaterialButtonToggleGroup subEdgeToggleGroup;
    private MaterialButton subOutlineB;
    private MaterialButton subShadowB;
    private TextView subMarginText;
    private Slider subMarginSlider;
    private ImageButton subMarginAddB;
    private ImageButton subMarginMinusB;

    private int subFont;
    private int subFontSize;
    private boolean subIsBold;
    private boolean subIsItalic;
    private int subTextColor = Color.WHITE;
    private boolean isBgEnable;
    private int subBgColor;
    private boolean isOutlineEnable;
    private boolean isShadowEnable;
    private int subEdgeColor = Color.BLACK;
    private int subMargin;

    private final String[] fontFamilyList = {"Default", "Monospace", "Sans-Serif", "Serif"};

    private ColorPickerDialog.Builder colorPickerBuilder;

    private SharedPreferences subtitlePrefs;
    private SharedPreferences.Editor subtitlePrefsEditor;


    public SettingSubtitleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting_subtitle, container, false);
        backB = view.findViewById(R.id.backB);

        subColorPickerB = view.findViewById(R.id.subPickColorB);
        subColorTabs = view.findViewById(R.id.subColorTabs);

        subSizeText = view.findViewById(R.id.subSizeText);
        subFontSizeSlider = view.findViewById(R.id.subTextSizeSeek);
        subSizeAddB = view.findViewById(R.id.subSizeAddB);
        subSizeMinusB = view.findViewById(R.id.subSizeMinusB);
        subBoldItalicToggleGroup = view.findViewById(R.id.subBIToggleView);
        subBoldB = view.findViewById(R.id.subBoldB);
        subItalicB = view.findViewById(R.id.subItalicB);
        subEnableSwitch = view.findViewById(R.id.subEnableSwitch);
        subEdgeToggleGroup = view.findViewById(R.id.subOSToggleView);
        subOutlineB = view.findViewById(R.id.subOutlineB);
        subShadowB = view.findViewById(R.id.subShadowB);
        subMarginText = view.findViewById(R.id.subMarginText);
        subMarginSlider = view.findViewById(R.id.subTextMarginSeek);
        subMarginAddB = view.findViewById(R.id.subMarginAddB);
        subMarginMinusB = view.findViewById(R.id.subMarginMinusB);

        subFontDropdown = view.findViewById(R.id.subFontDropdown);
        ArrayAdapter<String> fontDropdownAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, fontFamilyList);
        subFontDropdown.setAdapter(fontDropdownAdapter);

        subtitlePrefs = requireContext().getSharedPreferences("SubtitleSetting", Context.MODE_PRIVATE);
        subtitlePrefsEditor = subtitlePrefs.edit();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        backB.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        subColorTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int tabPos = tab.getPosition();

                if (tabPos == 0 ) {
                    subEnableSwitch.setVisibility(View.GONE);
                    subEdgeToggleGroup.setVisibility(View.GONE);
                    subColorPickerB.setCardBackgroundColor(subTextColor);
                    setColorTabsViews(true);
                }

                else if (tabPos == 1) {
                    subEnableSwitch.setVisibility(View.VISIBLE);
                    subEdgeToggleGroup.setVisibility(View.GONE);
                    subEnableSwitch.setChecked(isBgEnable);
                    subColorPickerB.setCardBackgroundColor(subBgColor);
                    setColorTabsViews(isBgEnable);
                }

                else {
                    subEnableSwitch.setVisibility(View.GONE);
                    subEdgeToggleGroup.setVisibility(View.VISIBLE);
                    subColorPickerB.setCardBackgroundColor(subEdgeColor);
                    subOutlineB.setChecked(isOutlineEnable);
                    subShadowB.setChecked(isShadowEnable);
                    setCheckedButtonsIcon(subOutlineB, isOutlineEnable);
                    setCheckedButtonsIcon(subShadowB, isShadowEnable);
                    setColorTabsViews(isOutlineEnable || isShadowEnable);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        subFontSizeSlider.addOnChangeListener((slider, value, fromUser) -> {
            subFontSize = (int) value;
            subSizeText.setText(String.valueOf(subFontSize));
            subtitlePrefsEditor.putInt("size", subFontSize).apply();
        });

        subSizeAddB.setOnClickListener(v -> {
            if (subFontSize == 30) return;

            subFontSize++;
            subFontSizeSlider.setValue(subFontSize);
        });

        subSizeMinusB.setOnClickListener(v -> {
            if (subFontSize == 10) return;

            subFontSize--;
            subFontSizeSlider.setValue(subFontSize);
        });

        subFontDropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                subFont = i;
                subtitlePrefsEditor.putInt("font", subFont).apply();
            }
        });

        subEnableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isBgEnable = b;
                setColorTabsViews(isBgEnable);
                subtitlePrefsEditor.putBoolean("isBg", b).apply();
            }
        });

        ColorListener colorListener = new ColorListener() {
            @Override
            public void onColorSelected(int color, boolean fromUser) {
                if (!fromUser) return;

                if (subColorTabs.getSelectedTabPosition() == 0) {
                    subTextColor = color;
                    subtitlePrefsEditor.putInt("textColor", color);
                }
                else if (subColorTabs.getSelectedTabPosition() == 1) {
                    subBgColor = color;
                    subtitlePrefsEditor.putInt("bgColor", color);
                }
                else {
                    subEdgeColor = color;
                    subtitlePrefsEditor.putInt("edgeColor", color);
                }
                subColorPickerB.setCardBackgroundColor(color);
                subtitlePrefsEditor.apply();
            }
        };

        subColorPickerB.setOnClickListener(v -> {
            int tab = subColorTabs.getSelectedTabPosition();
            int initColor = (tab == 0) ? subTextColor : (tab == 1) ? subBgColor : subEdgeColor;

            ColorPickerDialog.Builder dialog = new ColorPickerDialog.Builder(requireContext())
                    .setTitle("Subtitle Color Picker")
                    .setPositiveButton("Select", colorListener)
                    .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                    .attachBrightnessSlideBar(true)
                    .attachAlphaSlideBar(true)
                    .setBottomSpace(12);

            ColorPickerView picker = dialog.getColorPickerView();
            picker.post(() -> picker.setInitialColor(initColor));

            dialog.show();
        });

        subMarginSlider.addOnChangeListener((slider, value, fromUser) -> {
            subMargin = (int) value;
            subMarginText.setText(String.valueOf(subMargin));
            subtitlePrefsEditor.putInt("margin", subMargin).apply();
        });

        subMarginAddB.setOnClickListener(v -> {
            if (subMargin == 20) return;

            subMargin += 5;
            subMarginSlider.setValue(subMargin);
        });

        subMarginMinusB.setOnClickListener(v -> {
            if (subMargin == 0) return;

            subMargin -= 5;
            subMarginSlider.setValue(subMargin);
        });

        subBoldItalicToggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (checkedId == R.id.subBoldB) {
                    setCheckedButtonsIcon(subBoldB, isChecked);
                    subIsBold = isChecked;
                    subtitlePrefsEditor.putBoolean("isBold", subIsBold).apply();
                }
                else if (checkedId == R.id.subItalicB) {
                    setCheckedButtonsIcon(subItalicB, isChecked);
                    subIsItalic = isChecked;
                    subtitlePrefsEditor.putBoolean("isItalic", subIsItalic).apply();
                }
            }
        });

        subEdgeToggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (checkedId == R.id.subOutlineB) {
                    setCheckedButtonsIcon(subOutlineB, isChecked);
                    isOutlineEnable = isChecked;
                    subtitlePrefsEditor.putBoolean("isOutline", isOutlineEnable).apply();
                }
                else if (checkedId == R.id.subShadowB) {
                    setCheckedButtonsIcon(subShadowB, isChecked);
                    isShadowEnable = isChecked;
                    subtitlePrefsEditor.putBoolean("isShadow", isShadowEnable).apply();
                }
            }
        });

        setSubtitleValuesViews();


        requireActivity()
                .getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        getParentFragmentManager().popBackStack();
                    }
                });
    }


    private void setSubtitleValuesViews() {
        subFontSize = subtitlePrefs.getInt("size", 20);
        subFont = subtitlePrefs.getInt("font", 0);
        subIsBold = subtitlePrefs.getBoolean("isBold", false);
        subIsItalic = subtitlePrefs.getBoolean("isItalic", false);
        subTextColor = subtitlePrefs.getInt("textColor", Color.WHITE);
        isBgEnable = subtitlePrefs.getBoolean("isBg", false);
        subBgColor = subtitlePrefs.getInt("bgColor", Color.BLACK);
        isOutlineEnable = subtitlePrefs.getBoolean("isOutline", false);
        isShadowEnable = subtitlePrefs.getBoolean("isShadow", true);
        subEdgeColor = subtitlePrefs.getInt("edgeColor", Color.BLACK);
        subMargin = subtitlePrefs.getInt("margin", 0);

        subSizeText.setText(String.valueOf(subFontSize));
        subFontSizeSlider.setValue(subFontSize);

        subFontDropdown.setText(fontFamilyList[subFont], false);

        subBoldB.setChecked(subIsBold);
        subItalicB.setChecked(subIsItalic);

        subEnableSwitch.setVisibility(View.GONE);
        subColorPickerB.setCardBackgroundColor(subTextColor);
        setColorTabsViews(true);

        subMarginText.setText(String.valueOf(subMargin));
        subMarginSlider.setValue(subMargin);
    }

    private void setColorTabsViews(boolean flag) {
        if (flag) {
            subColorPickerB.setEnabled(true);
            subColorPickerB.setAlpha(1);
        }
        else {
            subColorPickerB.setEnabled(false);
            subColorPickerB.setAlpha(.2f);

        }
    }

    private void setCheckedButtonsIcon(MaterialButton btn, boolean isChecked) {
        if (isChecked) btn.setIconResource(androidx.media3.session.R.drawable.media3_icon_check_circle_filled);
        else btn.setIcon(null);
    }
}