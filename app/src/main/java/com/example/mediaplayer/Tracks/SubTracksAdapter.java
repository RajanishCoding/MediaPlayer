package com.example.mediaplayer.Tracks;

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

import com.example.mediaplayer.R;

import java.util.List;

@UnstableApi
public class SubTracksAdapter extends RecyclerView.Adapter<SubTracksAdapter.SubTrackViewHolder> {
    private List<SubTracks> subTracksList;
    private DefaultTrackSelector trackSelector;

    private int selectedPosition = -1; // Tracks the selected position
    private int previousPosition = -1;
    private boolean firstSelection = true;


    public SubTracksAdapter(List<SubTracks> subTracksList, DefaultTrackSelector trackSelector) {
        this.subTracksList = subTracksList;
        this.trackSelector = trackSelector;
    }

    @Override
    public SubTrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.subtrackslist_layout, parent, false);
        return new SubTrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SubTrackViewHolder holder, int position) {
        SubTracks track = subTracksList.get(position);

        if (position != 0) {
            if (track.getLabel() == null) {
                holder.label.setText("Sub Track " + (position + 1));
            } else {
                holder.label.setText(track.getLabel());
            }

            holder.language.setVisibility(View.VISIBLE);
            holder.language.setText(track.getLanguage());
        }
        else {
            holder.label.setText("None");
            holder.language.setVisibility(View.GONE);
        }

        if (track.isSelected() && firstSelection) {
            holder.radioButton.setChecked(true);
            previousPosition = position;
            Log.d("SubTrackSelect", "onBindViewHolder: " + position);
        }
        else {
            holder.radioButton.setChecked(position == selectedPosition);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onClick(View v) {
                DefaultTrackSelector.Parameters parameters;
                int pos = holder.getBindingAdapterPosition();

                if (pos != 0 && pos != RecyclerView.NO_POSITION) {
                    SubTracks t = subTracksList.get(pos);
                    TrackSelectionOverride override = new TrackSelectionOverride(t.getTrackGroup(), t.getTrackIndex());

                    // Build new parameters with the override
                    parameters = trackSelector.buildUponParameters()
                            .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                            .addOverride(override)
                            .build();
                }
                else {
                    parameters = trackSelector.buildUponParameters()
                            .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true) // disables Subtitle
                            .build();
                }

                // Apply the new parameters
                trackSelector.setParameters(parameters);

                if (selectedPosition != pos) {
                    // Store the previous selected position

                    if (firstSelection) {
                        selectedPosition = pos;
                        firstSelection = false;
                    }
                    else {
                        previousPosition = selectedPosition;
                        selectedPosition = pos; // Update to the new position
                    }
                    Log.d("SubTrackSelect", "CLicked: " + previousPosition + "   " + selectedPosition);

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
        if (subTracksList != null) {
            return subTracksList.size();
        }
        return 0;
    }


    public static class SubTrackViewHolder extends RecyclerView.ViewHolder {
        TextView label;
        TextView language;
        RadioButton radioButton;

        public SubTrackViewHolder(View subtrackslist_layout) {
            super(subtrackslist_layout);
            label = subtrackslist_layout.findViewById(R.id.subTrackLabel);
            language = subtrackslist_layout.findViewById(R.id.subTrackLanguage);
            radioButton = subtrackslist_layout.findViewById(R.id.subRadioButton);
        }
    }

    // Optional: Method to check if a specific position's RadioButton is checked
    public boolean isRadioButtonChecked(int position) {
        return selectedPosition == position;
    }
}