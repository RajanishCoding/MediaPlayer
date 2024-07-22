package com.example.mediaplayer;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mediaplayer.Media;

import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.VideoViewHolder> {
    private List<Media> mediaList;
    private Context context;


    public MediaAdapter(Context context, List<Media> mediaList) {
        this.context = context;
        this.mediaList = mediaList;
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        Media media = mediaList.get(position);
        holder.name.setText(media.getName());
        holder.path.setText(media.getPath());
        holder.duration.setText(media.getDuration());
        Glide.with(holder.thumbnail.getContext()).load(R.drawable.icon).into(holder.thumbnail);

        Log.d("Media Added", "Added");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Item Clicked", holder.getBindingAdapterPosition() + " : " + media.getName());

                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("Name", media.getName());
                intent.putExtra("Path", media.getPath());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView name;
        TextView path;
        TextView duration;

        public VideoViewHolder(View item_layout) {
            super(item_layout);
            thumbnail = item_layout.findViewById(R.id.thumbnail);
            name = item_layout.findViewById(R.id.name);
            path = item_layout.findViewById(R.id.path);
            duration = item_layout.findViewById(R.id.duration);
        }
    }
}
