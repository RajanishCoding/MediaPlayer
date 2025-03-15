package com.example.mediaplayer;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

@UnstableApi
public class AudioTracksAdapter extends RecyclerView.Adapter<AudioTracksAdapter.AudioTrackViewHolder> {
    private List<AudioTracks> audioTracksList;
    private DefaultTrackSelector trackSelector;

    private int selectedPosition = -1; // Tracks the selected position
    private int previousPosition = -1;
    private boolean firstSelection = true;


    public AudioTracksAdapter(List<AudioTracks> audioTracksList, DefaultTrackSelector trackSelector) {
        this.audioTracksList = audioTracksList;
        this.trackSelector = trackSelector;
    }

    @Override
    public AudioTrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.audiotrackslist_layout, parent, false);
        return new AudioTrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AudioTrackViewHolder holder, int position) {
        AudioTracks track = audioTracksList.get(position);

        if (track.getLabel() == null) {
            holder.label.setText("Audio Track " + (position+1));
        }
        else {
            holder.label.setText(track.getLabel());
        }

        String finalText;
        if (track.getLanguage() != null)
            finalText = track.getLanguage() + " - " + track.getChannels() + " Channels";
        else
            finalText = track.getChannels() + " Channels";

        holder.LangChannels.setText(finalText);

        if (track.isSelected() && firstSelection) {
            holder.radioButton.setChecked(true);
            previousPosition = position;
            Log.d("AudioTrackSelect", "onBindViewHolder: " + position);
        }
        else {
            holder.radioButton.setChecked(position == selectedPosition);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onClick(View v) {

                TrackSelectionOverride override = new TrackSelectionOverride(track.getTrackGroup(), track.getTrackIndex());

                // Build new parameters with the override
                DefaultTrackSelector.Parameters parameters = trackSelector.buildUponParameters()
                        .clearOverridesOfType(C.TRACK_TYPE_AUDIO)
                        .addOverride(override)
                        .build();

                // Apply the new parameters
                trackSelector.setParameters(parameters);

                if (selectedPosition != position) {
                    // Store the previous selected position

                    if (firstSelection) {
                        selectedPosition = position;
                        firstSelection = false;
                    }
                    else {
                        previousPosition = selectedPosition;
                        selectedPosition = position; // Update to the new position
                    }
                    Log.d("AudioTrackSelect", "CLicked: " + previousPosition + "   " + selectedPosition);

                    // Notify adapter to refresh previously and newly selected items
                    notifyItemChanged(previousPosition); // Uncheck the previously selected RadioButton
                    notifyItemChanged(selectedPosition); // Check the newly selected RadioButton
//                    holder.radioButton.setChecked(true);
                }

                Log.d("trackLog", "onClick: " + track.getLabel());
            }
        });
    }

    @Override
    public int getItemCount() {
        if (audioTracksList != null) {
            return audioTracksList.size();
        }
        return 0;
    }


    public static class AudioTrackViewHolder extends RecyclerView.ViewHolder {
        TextView label;
        TextView LangChannels;
        RadioButton radioButton;

        public AudioTrackViewHolder(View audiotrackslist_layout) {
            super(audiotrackslist_layout);
            label = audiotrackslist_layout.findViewById(R.id.audioTrackLabel);
            LangChannels = audiotrackslist_layout.findViewById(R.id.audioTrack_LangChannels);
            radioButton = audiotrackslist_layout.findViewById(R.id.audioRadioButton);
        }
    }

    // Optional: Method to check if a specific position's RadioButton is checked
    public boolean isRadioButtonChecked(int position) {
        return selectedPosition == position;
    }
}