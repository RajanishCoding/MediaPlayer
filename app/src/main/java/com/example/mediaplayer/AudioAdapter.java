package com.example.mediaplayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.content.ContextCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioViewHolder> {
    private List<Audio> mediaList;
    private Context context;

    private boolean dur;
    private boolean dur_onThumbnail;

    private boolean path;
    private boolean size = true;
    private boolean date = true;


    public AudioAdapter(Context context, List<Audio> mediaList) {
        this.context = context;
        this.mediaList = mediaList;
    }

    public void setDetailsVisibility(boolean isPath, boolean isSize, boolean isDate, boolean isDur, boolean isDur_onThumb) {
        path = isPath;
        size = isSize;
        date = isDate;
        dur = isDur;
        dur_onThumbnail = isDur_onThumb;
        notifyDataSetChanged();
    }

    private void setVisibilities(AudioAdapter.AudioViewHolder holder) {
        holder.resolutionFrame.setVisibility(View.GONE);

        if (path) holder.path.setVisibility(View.VISIBLE);
        else holder.path.setVisibility(View.GONE);

        if (size) holder.size.setVisibility(View.VISIBLE);
        else holder.size.setVisibility(View.GONE);

        if (date) holder.dateAdded.setVisibility(View.VISIBLE);
        else holder.dateAdded.setVisibility(View.GONE);

        holder.duration1.setVisibility((dur && dur_onThumbnail) ? View.VISIBLE : View.GONE);
        holder.duration2.setVisibility((dur && !dur_onThumbnail) ? View.VISIBLE : View.GONE);
    }

    @Override
    public AudioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout, parent, false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AudioViewHolder holder, int position) {
        Audio media = mediaList.get(position);
        holder.name.setText(media.getName());
        holder.path.setText(media.getPath());
        holder.dateAdded.setText(getFormattedDate(Long.parseLong(media.getDateAdded())));

//        holder.thumbnail.setImageBitmap(media.getThumbnail());
//        Glide.with(holder.thumbnail.getContext()).load(media.getThumbnail()).into(holder.thumbnail);

        setVisibilities(holder);

        if (media.getDuration() == null || media.getSize() == null) {
            // MediaExtractor Thread
//            new Thread(() -> {
//                MediaExtractor extractor = new MediaExtractor();
//                File file = new File(media.getPath());
//
//                try {
//                    extractor.setDataSource(media.getPath());
//
//                    long sizeInBytes = file.length();
//                    String size = getFormattedFileSize(sizeInBytes);
//
//                    // Iterate through tracks
//                    for (int i = 0; i < extractor.getTrackCount(); i++) {
//                        MediaFormat format = extractor.getTrackFormat(i);
//
//                        if (format.containsKey(MediaFormat.KEY_MIME) && format.getString(MediaFormat.KEY_MIME).startsWith("video/")) {
//                            long duration = format.getLong(MediaFormat.KEY_DURATION);
//                            int width = format.getInteger(MediaFormat.KEY_WIDTH);
//                            int height = format.getInteger(MediaFormat.KEY_HEIGHT);
//                            int fpsInt = 0;
//                            float fpsFloat = 0f;
//
//                            if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
//                                try {
//                                    fpsFloat = format.getFloat(MediaFormat.KEY_FRAME_RATE);
//                                    Log.d("MediaFormat", "Frame rate (float): " + fpsFloat);
//                                } catch (Exception e) {
//                                    // Handle type mismatch
//                                    Log.e("MediaFormat", "Frame rate is not a float: " + e.getMessage());
//                                    fpsInt = format.getInteger(MediaFormat.KEY_FRAME_RATE);
//                                    Log.d("MediaFormat", "Frame rate (int): " + fpsInt);
//                                }
//                            }
//
//                            // Setting all Values
//                            media.setSize(size);
//                            media.setDuration(String.valueOf(duration));
//                            media.setResolution(String.valueOf(height));
//                            media.setFrameRate(String.valueOf((fpsFloat != 0f ? fpsFloat : fpsInt)));
//
//                            Log.d("MediaExtractor", "File Size: " + size + " MB, Duration: " + MicrosToTime(duration) + ", Resolution: " + width + "x" + height + ", FPS: " + (fpsFloat != 0f ? fpsFloat : fpsInt));
//
//                            if (position == mediaList.size() - 1) {
//                                saveMediaListToPreferences(mediaList);
//                            }
//                        }
//                    }
//                }
//                catch (Exception e) {
//                    e.printStackTrace();
//                }
//                finally {
//                    extractor.release();
//                }
//
//                holder.itemView.post(() -> {
//                    holder.duration1.setText(MicrosToTime(Long.parseLong(media.getDuration())));
//                    holder.duration2.setText(MicrosToTime(Long.parseLong(media.getDuration())));
//                    holder.resolutionFrame.setText(media.getResolution() + "@" + media.getFrameRate());
//                    holder.size.setText(media.getSize());
//                });
//            }).start();

            // MediaMetadataRetriever Thread

//          MediaMetadataRetriever Thread
//            new Thread(() -> {
//                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//                File file = new File(media.getPath());
//
//                try {
//                    retriever.setDataSource(media.getPath());
//
//                    long sizeInBytes = file.length();
//                    String size = getFormattedFileSize(sizeInBytes);
//
//                    String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION); // in ms
//
//                    // Setting all Values
//                    media.setSize(size);
//                    media.setDuration(duration);
//
//                    Log.d("MediaExtractor", "File Size: " + size + " MB, Duration: " + MillisToTime(Long.parseLong(duration)));
//
//                    if (position == mediaList.size() - 1) {
//                        saveMediaListToPreferences(mediaList);
//                    }
//                }
//
//                catch (Exception e) {
//                    e.printStackTrace();
//                }
//                finally {
//                    try {
//                        retriever.release();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                holder.itemView.post(() -> {
//                    holder.duration1.setText(MillisToTime(Long.parseLong(media.getDuration() != null ? media.getDuration() : "0")));
//                    holder.duration2.setText(MillisToTime(Long.parseLong(media.getDuration() != null ? media.getDuration() : "0")));
//                    holder.size.setText(media.getSize());
//                });
//            }).start();

            new FFmpegMetadataRetriever(media.getPath(), retriever -> {
                try {
                    String sizeInBytes = String.valueOf(retriever.getFileSize());
//                    String size = getFormattedFileSize(sizeInBytes);

                    String duration = String.valueOf(retriever.getDuration()); // in s

                    // Setting all Values
                    media.setSize(sizeInBytes);
                    media.setDuration(duration);

                    Log.d("MediaExtractor", "File Size: " + getFormattedFileSize(Long.parseLong(media.getSize())) + " MB, Duration: " + MillisToTime(Long.parseLong(duration)));

                    if (position == mediaList.size() - 1) {
                        saveMediaListToPreferences(mediaList);
                    }
                }

                catch (Exception e) {
                    e.printStackTrace();
                }

                holder.itemView.post(() -> {
                    holder.duration1.setText(SecsToTime(Long.parseLong(media.getDuration() != null ? media.getDuration() : "0")));
                    holder.duration2.setText(SecsToTime(Long.parseLong(media.getDuration() != null ? media.getDuration() : "0")));
                    holder.size.setText(getFormattedFileSize(Long.parseLong(media.getSize())));
                });
            });

        }
        else {
            Log.d("MediaExtractor", "onBindViewHolder: " + media.getDuration());

            // MediaExtractor Thread
//            holder.duration1.setText(MicrosToTime(Long.parseLong(media.getDuration())));
//            holder.duration2.setText(MicrosToTime(Long.parseLong(media.getDuration())));

            // MediaMetadataRetriever Thread
            holder.duration1.setText(SecsToTime(Long.parseLong(media.getDuration())));
            holder.duration2.setText(SecsToTime(Long.parseLong(media.getDuration())));

            holder.size.setText(getFormattedFileSize(Long.parseLong(media.getSize())));
        }

        try {
            Glide.with(holder.thumbnail.getContext())
            .asBitmap()
            .load(media.getPath()) // Unique identifier, ensures correct thumbnail
            .placeholder(R.drawable.music1)
            .override(420)
            .centerCrop()
            .into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    holder.thumbnail.setImageBitmap(resource);
                    Log.d("TAG", "onResourceReady: YES : "+ media.getName());
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    Glide.with(holder.thumbnail.getContext()).load(getThumbnail(media.getPath()))
                    .placeholder(R.drawable.music1)
                    .override(420)
                    .into(holder.thumbnail);

                    Log.d("TAG", "onLoadFailed: YES : "+ media.getName());
                }
            });
        }
        catch (Exception e) {
            Log.e("Glide Error", "onBindViewHolder: " + e);
        }

        Log.d("Audio Added", "Added");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onClick(View v) {
                Log.d("Item Clicked", holder.getBindingAdapterPosition() + " : " + media.getName());

                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("Name", media.getName());
                intent.putExtra("Path", media.getPath());
                intent.putExtra("isVideo", media.isVideo());
                Log.d("isVideoFile", "onClick: " + media.isVideo());
                context.startActivity(intent);
            }
        });
    }


    public static class AudioViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView name;
        TextView path;
        TextView dateAdded;
        TextView duration1;
        TextView duration2;
        TextView resolutionFrame;
        TextView size;

        public AudioViewHolder(View item_layout) {
            super(item_layout);
            thumbnail = item_layout.findViewById(R.id.thumbnail);
            name = item_layout.findViewById(R.id.name);
            path = item_layout.findViewById(R.id.path);
            dateAdded = item_layout.findViewById(R.id.t4_date);
            duration1 = item_layout.findViewById(R.id.duration);
            duration2 = item_layout.findViewById(R.id.t1_duration);
            resolutionFrame = item_layout.findViewById(R.id.t2_resolutionFrame);
            size = item_layout.findViewById(R.id.t3_size);
        }
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
                Log.d("TAG", "getThumbnail: ");
            }
            else {
                if (frame != null) {
                    thumbnail = frame;
                }
                else {
                    Drawable vectorDrawable = ContextCompat.getDrawable(context, R.drawable.music1);
                    assert vectorDrawable != null;
                    thumbnail = Bitmap.createBitmap(
                            vectorDrawable.getIntrinsicWidth(),
                            vectorDrawable.getIntrinsicHeight(),
                            Bitmap.Config.ARGB_8888
                    );

                    Canvas canvas = new Canvas(thumbnail);
                    vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    vectorDrawable.draw(canvas);
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

    public void addMediaItem(Audio media) {
        mediaList.add(media);  // Add new item to the list
        notifyItemInserted(mediaList.size() - 1);  // Notify the adapter of the new item
    }

    public void addMediaItems(List<Audio> newMediaItems) {
        int startPosition = mediaList.size();
        mediaList.addAll(newMediaItems);
        notifyItemRangeInserted(startPosition, newMediaItems.size());
    }

    public void addAllMediaItems() {
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mediaList != null) {
            return mediaList.size();
        }
        return 0;
    }


    private void saveMediaListToPreferences(List<Audio> mediaList) {
        new Thread(() -> {
            try {
                SharedPreferences prefs = context.getSharedPreferences("MediaPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                Gson gson = new Gson();
                String json = gson.toJson(mediaList);
                editor.putString("audioList", json);
                editor.apply();
            } catch (Exception e) {
                Log.e("TAG", "saveMediaListToPreferences: ", e);
            }
        }).start();
    }

    public static String getFormattedDate(long dateAdded) {
        // Convert from seconds to milliseconds
        Date date = new Date(dateAdded * 1000);

        SimpleDateFormat sameYearFormat = new SimpleDateFormat("d MMM", Locale.getDefault()); // 15 Nov
        SimpleDateFormat differentYearFormat = new SimpleDateFormat("d MMM, yyyy", Locale.getDefault()); // 15 Nov, 2024

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        calendar.setTime(date);
        int givenYear = calendar.get(Calendar.YEAR);

        Log.d("date", "getFormattedDate: " + currentYear + givenYear);

        // Format the date accordingly
        if (currentYear == givenYear) {
            return sameYearFormat.format(date); // 15 Nov
        } else {
            return differentYearFormat.format(date); // 15 Nov, 2024
        }
    }

    public static String getFormattedFileSize(long sizeInBytes) {
        // All sizes in bytes
        final int KILOBYTE = 1024;
        final int MEGABYTE = KILOBYTE * 1024;
        final int GIGABYTE = MEGABYTE * 1024;

        // Converting based on size
        if (sizeInBytes < KILOBYTE) {
            return sizeInBytes + " B"; // Bytes
        }
        else if (sizeInBytes < MEGABYTE) {
            return (sizeInBytes / KILOBYTE) + " KB"; // Kilobytes
        }
        else if (sizeInBytes < GIGABYTE) {
            return (sizeInBytes / MEGABYTE) + " MB"; // Megabytes
        }
        else {
            double sizeInGB = (double) sizeInBytes / GIGABYTE;
            return String.format(Locale.ROOT,"%.1f GB", sizeInGB); // Gigabytes
        }
    }

    public String MicrosToTime(long micros) {
        long millis = micros / 1000;
        long hours = (millis / (1000 * 60 * 60)) % 24;
        long minutes = (millis / (1000 * 60)) % 60;
        long seconds = (millis / 1000) % 60;

        if (hours >= 1) {
            return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
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

    public String SecsToTime(long seconds) {
        long hours = (seconds / 3600);
        long minutes = (seconds % 3600) / 60;
        long second = seconds % 60;

        if (hours >= 1) {
            return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, second);
        }
        return String.format(Locale.ROOT, "%02d:%02d", minutes, second);
    }

    public int DpToPixel(float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}