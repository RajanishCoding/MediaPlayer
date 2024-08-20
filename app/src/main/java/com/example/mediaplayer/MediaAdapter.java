package com.example.mediaplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaController2;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
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
//        holder.thumbnail.setImageBitmap(media.getThumbnail());
//        Glide.with(holder.thumbnail.getContext()).load(media.getThumbnail()).into(holder.thumbnail);

        Glide.with(holder.thumbnail.getContext())
                .asBitmap()
                .load(media.getPath()) // Unique identifier, ensures correct thumbnail
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        holder.thumbnail.setImageBitmap(resource);
                        Log.d("TAG", "onResourceReady: YES");
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        Glide.with(holder.thumbnail.getContext()).load(getThumbnail(media.getPath())).into(holder.thumbnail);
                        Log.d("TAG", "onLoadFailed: YES");
                    }
                });

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

    private Bitmap getThumbnail(String filePath) {
//        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(filePath, 1);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        Bitmap thumbnail = null;

        try {
            retriever.setDataSource(filePath);
            Bitmap frame = retriever.getFrameAtTime(1000000); // Get frame at 1 second (1000000 microseconds)
            byte[] art = retriever.getEmbeddedPicture();

            if (art != null) {
                thumbnail = BitmapFactory.decodeByteArray(art, 0, art.length);
            }
            else {
                if (frame != null) {
                    thumbnail = frame;
                }
                else {
                    thumbnail = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            // Handle cases where the file is invalid or data source is unsupported
        }
        finally {
            try {
                retriever.release();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return thumbnail;
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