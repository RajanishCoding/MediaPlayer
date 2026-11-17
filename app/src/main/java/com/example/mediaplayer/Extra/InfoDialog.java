package com.example.mediaplayer.Extra;

import android.app.Dialog;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mediaplayer.Audio.Audio;
import com.example.mediaplayer.R;
import com.example.mediaplayer.Video.Video;
import com.example.mediaplayer.Video.VideoAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class InfoDialog extends DialogFragment {

    private Object media;
    private boolean isVideo;

    private JSONObject metadataJson;

    private LinearLayout videoSection;
    private LinearLayout audioSection;
    private LinearLayout subSection;
    private View dividerVideo;
    private View dividerAudio;
    private View dividerSub;

    private LinearLayout fileContainer;
    private LinearLayout formatContainer;
    private LinearLayout videoContainer;
    private LinearLayout audioContainer;
    private LinearLayout subContainer;

    private JSONObject format;
    private JSONObject video;
    private JSONArray audio;
    private JSONArray subtitle;

    private String metadata;
    private Video videoObj;
    private Audio audioObj;
    String name, path, dateAdded;
    long duration, size;

    public InfoDialog(Object media, boolean isVideo) {
        this.media = media;
        this.isVideo = isVideo;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.dialog_info, container, false);

        videoSection = view.findViewById(R.id.layout_video);
        audioSection = view.findViewById(R.id.layout_audio);
        subSection = view.findViewById(R.id.layout_subtitle);
        dividerVideo = view.findViewById(R.id.dividerVideo);
        dividerAudio = view.findViewById(R.id.dividerAudio);
        dividerSub = view.findViewById(R.id.dividerSub);

        fileContainer = view.findViewById(R.id.containerFile);
        formatContainer = view.findViewById(R.id.containerFormat);
        videoContainer = view.findViewById(R.id.containerVideo);
        audioContainer = view.findViewById(R.id.containerAudio);
        subContainer = view.findViewById(R.id.containerSubtitle);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (isVideo) {
            videoObj = (Video) media;
            name = videoObj.getName();
            path = videoObj.getPath();
            dateAdded = videoObj.getDateAdded();
            duration = videoObj.getDuration();
            size = videoObj.getSize();
            metadata = videoObj.getExtraMetadata();
        }
        else {
            audioObj = (Audio) media;
            name = audioObj.getName();
            path = audioObj.getPath();
            dateAdded = audioObj.getDateAdded();
            duration = audioObj.getDuration();
            size = audioObj.getSize();
            metadata = audioObj.getExtraMetadata();
        }

        try {
            metadataJson = metadata != null ? new JSONObject(metadata) : null;
            if (metadataJson == null) return;
        }
        catch (JSONException e) {
            throw new RuntimeException(e);
        }

        format = metadataJson.optJSONObject("format");
        video = metadataJson.optJSONObject("video");
        audio = metadataJson.optJSONArray("audio");
        subtitle = metadataJson.optJSONArray("subtitles");

        // File & Format Binding
        setFileFormatSections();

        // Video
        if (video != null & videoObj != null) {
            videoSection.setVisibility(View.VISIBLE);
            dividerVideo.setVisibility(View.VISIBLE);
            setVideoSection();
        }

        // Audio
        if (audio != null && audio.length() >= 0) {
            audioSection.setVisibility(View.VISIBLE);
            dividerAudio.setVisibility(View.VISIBLE);
            setAudioSection(audio);
        }

        // Subtitle
        if (subtitle != null && subtitle.length() >= 0) {
            subSection.setVisibility(View.VISIBLE);
            dividerSub.setVisibility(View.VISIBLE);
            setSubSection(subtitle);
        }
    }
    

    @Override
    public void onStart() {
        super.onStart();
        updateDialogSize();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateDialogSize();
    }


    private void setFileFormatSections() {
        addInfoRow(fileContainer, "Name", name);
        addInfoRow(fileContainer, "Location", path);
        addInfoRow(fileContainer, "Size", VideoAdapter.getFormattedFileSize(size));
        addInfoRow(fileContainer, "Date Added", VideoAdapter.getFormattedDate(Long.parseLong(dateAdded)));

        // Format
        if (format.has("title")) {
            addInfoRow(formatContainer, "Title", format.optString("title"));
        }

        if (format.has("format_name")) {
            addInfoRow(formatContainer, "Format", format.optString("format_name").replace(",", ", ").toUpperCase());
        }

        if (format.has("bit_rate")) {
            addInfoRow(formatContainer, "Overall Bitrate", getFormattedBitrate(format.optLong("bit_rate")));
        }

        if (format.has("duration")) {
            addInfoRow(formatContainer, "Duration", VideoAdapter.SecsToTime(duration));
        }

        if (format.has("encoder")) {
            addInfoRow(formatContainer, "Encoder", format.optString("encoder"));
        }
    }

    private void setVideoSection() {
        if (video.has("title")) {
            addInfoRow(videoContainer, "Title", video.optString("title"));
        }

        if (video.has("codec")) {
            addInfoRow(videoContainer, "Codec", video.optString("codec").replace(",", ", ").toUpperCase());
        }

        if (video.has("profile")) {
            addInfoRow(videoContainer, "Profile", video.optString("profile"));
        }

        setHDRFormat();

        if (video.has("width")) {
            addInfoRow(videoContainer, "Resolution", video.optInt("width") + " x " + videoObj.getResolution().toString());
        }

        String fps = videoObj.getFPS();
        addInfoRow(videoContainer, "Frame Rate", fps + " fps");

        if (video.has("bit_rate")) {
            addInfoRow(videoContainer, "Bitrate", getFormattedBitrate(video.optLong("bit_rate")));
        }

        if (video.has("color_range")) {
            addInfoRow(videoContainer, "Color Range", video.optString("color_range").toUpperCase());
        }

        if (video.has("color_space")) {
            addInfoRow(videoContainer, "Color Space", video.optString("color_space").toUpperCase());
        }

        if (video.has("color_transfer")) {
            addInfoRow(videoContainer, "Color Transfer", video.optString("color_transfer").toUpperCase());
        }

        if (video.has("color_primaries")) {
            addInfoRow(videoContainer, "Color Primaries", video.optString("color_primaries").toUpperCase());
        }

        if (video.has("bit_depth")) {
            addInfoRow(videoContainer, "Bit Depth", video.optInt("bit_depth") + " bits");
        }

        if (video.has("language")) {
            addInfoRow(videoContainer, "Language", getFullLanguageCName(video.optString("language")));
        }
    }

    private void setHDRFormat() {
        JSONObject hdrDetails = video.optJSONObject("hdrDetails");
        if (hdrDetails == null) return;

        boolean isDV = hdrDetails.optBoolean("isDV");
        boolean isHDR10Plus = hdrDetails.optBoolean("isHDR10+");
        boolean isHDR10 = hdrDetails.optBoolean("isHDR10");
        boolean isHLG = hdrDetails.optBoolean("isHLG");
        boolean isHDR10Compatible = hdrDetails.optBoolean("isHDR10Compatible");
        boolean isHDR10PlusCompatible = hdrDetails.optBoolean("isHDR10+Compatible");
        int dvProfile = -1, dvLevel = -1;
        if (hdrDetails.has("dv_profile")) dvProfile = hdrDetails.optInt("dv_profile");
        if (hdrDetails.has("dv_level")) dvLevel = hdrDetails.optInt("dv_level");

        StringBuilder sb = new StringBuilder();

        if (isDV) {
            sb.append("Dolby Vision\n");
            if (dvProfile != -1) sb.append("Profile: ").append(dvProfile);
            if (dvLevel != -1) sb.append(", Level: ").append(dvLevel);
        }
        if (isHDR10Plus) sb.append("\nHDR10+\n");
        else if (isHDR10PlusCompatible) sb.append("\nHDR10+ Compatible\n");
        if (isHDR10) sb.append("HDR10");
        else if (isHDR10Compatible) sb.append("HDR10 Compatible");

        String hdrFormat = sb.toString();
        if (!hdrFormat.isEmpty()) {
            addInfoRow(videoContainer, "HDR Format", hdrFormat);
        }
    }

    private void setAudioSection(JSONArray audioArray) {
        for (int i = 0; i < audioArray.length(); i++) {
            JSONObject audio = audioArray.optJSONObject(i);
            if (audio == null) continue;

            if (i != 0) addSectionDivider(audioContainer);

            addSectionHeader(audioContainer, "Audio " + (i + 1) +
                    (audio.has("language") ? " (" + getFullLanguageName(audio.optString("language")) + ")" : ""));

            if (audio.has("title")) {
                addInfoRow(audioContainer, "Title", audio.optString("title"));
            }

            if (audio.has("codec")) {
                addInfoRow(audioContainer, "Codec", audio.optString("codec").replace(",", ", ").toUpperCase());
            }

            if (audio.has("channels")) {
                addInfoRow(audioContainer, "Channels", audio.optInt("channels") + " channels");
            }

            if (audio.has("channel_layout")) {
                addInfoRow(audioContainer, "Channel Layout", capitalize(audio.optString("channel_layout")));
            }

            if (audio.has("sample_rate")) {
                addInfoRow(audioContainer, "Sample Rate", audio.optInt("sample_rate") + " Hz");
            }

            if (audio.has("bitrate")) {
                addInfoRow(audioContainer, "Bitrate", getFormattedBitrate(audio.optLong("bitrate")));
            }

            if (audio.has("language")) {
                addInfoRow(audioContainer, "Language", getFullLanguageCName(audio.optString("language")));
            }
        }
    }

    private void setSubSection(JSONArray subtitleArray) {
        for (int i = 0; i < subtitleArray.length(); i++) {
            JSONObject sub = subtitleArray.optJSONObject(i);
            if (sub == null) continue;

            if (i != 0) addSectionDivider(subContainer);

            addSectionHeader(subContainer, "Subtitle " + (i + 1) +
                    (sub.has("language") ? " (" + getFullLanguageName(sub.optString("language")) + ")" : ""));

            if (sub.has("title")) {
                addInfoRow(subContainer, "Title", sub.optString("title"));
            }

            if (sub.has("codec")) {
                addInfoRow(subContainer, "Codec", sub.optString("codec").replace(",", ", ").toUpperCase());
            }

            if (sub.has("language")) {
                addInfoRow(subContainer, "Language", getFullLanguageCName(sub.optString("language")));
            }
        }
    }

    // Info Row
    private void addInfoRow(LinearLayout container, String label, String value) {
        View row = LayoutInflater.from(getContext()).inflate(R.layout.layout_info_row, container, false);

        TextView tvLabel = row.findViewById(R.id.label);
        TextView tvValue = row.findViewById(R.id.value);

        tvLabel.setText(label + ":");
        tvValue.setText(value);

        container.addView(row);
    }

    // Header Row
    private void addSectionHeader(LinearLayout container, String title) {
        TextView subHeader = new TextView(getContext());
        subHeader.setText(title);
        subHeader.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_color2));
        subHeader.setTypeface(null, Typeface.BOLD);
        subHeader.setPadding(0, 0, 0, DpToPx(5));
        container.addView(subHeader);
    }

    private void addSectionDivider(LinearLayout container) {
        View v = new View(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (getResources().getDisplayMetrics().density));

        lp.setMargins(0, DpToPx(10), 0, DpToPx(10));
        v.setLayoutParams(lp);
        v.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.divider_color));
        container.addView(v);
    }

    public String getFormattedBitrate(long bitrate) {
        // All values in bits
        final int kb = 1000;
        final int mb = kb * 1000;
        final int gb = mb * 1000;

        // Converting based on bits
        if (bitrate < kb) {
            return bitrate + " b/s"; // bits
        }
        else if (bitrate < mb) {
            return (bitrate / kb) + " Kb/s"; // Kilobits
        }
        else if (bitrate < gb) {
            double bitrateMb = (double) bitrate / mb;
            return String.format(Locale.ROOT, "%.1f Mb/s", bitrateMb);
        }
        else {
            double bitrateGb = (double) bitrate / gb;
            return String.format(Locale.ROOT, "%.1f Gb/s", bitrateGb); // Gigabits
        }
    }

    public static String getFullLanguageName(String code) {
        String[] parts = code.split("[-_]");
        Locale locale = parts.length == 2 ? new Locale(parts[0], parts[1]) : new Locale(parts[0]);
        return locale.getDisplayLanguage(Locale.ENGLISH) +
                (parts.length == 2 ? " " + locale.getCountry() : "");
    }

    public static String getFullLanguageCName(String code) {
        String[] parts = code.split("[-_]");
        Locale locale = parts.length == 2 ? new Locale(parts[0], parts[1]) : new Locale(parts[0]);
        return locale.getDisplayLanguage(Locale.ENGLISH) +
                (parts.length == 2 ? " (" + locale.getDisplayCountry() + ")" : "");
    }

    private void updateDialogSize() {
        int p = DpToPx(20);

        if (getShowsDialog()) {
            Dialog dialog = getDialog();
            if (dialog == null) return;
            Window window = dialog.getWindow();
            Log.d("TAG", "updateDialogSize: " + "1");

            if (window != null) {
                boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
                DisplayMetrics dm = getResources().getDisplayMetrics();

                window.setBackgroundDrawableResource(R.drawable.shape_bottom_bar);
                window.getDecorView().setPadding(p, p, p, p);

                window.setLayout(
                        (int) (dm.widthPixels  * (isLandscape ? 0.6f : 0.9f)),
                        (int) (dm.heightPixels  * (isLandscape ? 0.9f : 0.65f))
                );
            }
        }
        else {
            Log.d("TAG", "updateDialogSize: " + "2");
            View view = getView();
            if (view == null) return;
            view.setPadding(0, p/2, 0, p);
        }
    }

    public int DpToPx(int dp) {
        float density = this.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}