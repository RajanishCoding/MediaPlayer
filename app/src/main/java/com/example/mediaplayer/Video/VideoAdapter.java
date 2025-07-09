package com.example.mediaplayer.Video;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.mediaplayer.Extra.FFmpegMetadataRetriever;
import com.example.mediaplayer.Extra.MyBottomSheet;
import com.example.mediaplayer.PlayerActivity;
import com.example.mediaplayer.R;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    public interface SelectionListener {
        void onSelectionStarts();
        void onSelectionEnds();
        void onCountChanged(int counts);
    }

    private List<Video> mediaList;
    private Context context;

    private boolean dur;
    private boolean dur_onThumbnail;

    private boolean path;
    private boolean resol;
    private boolean fps;
    private boolean size = true;
    private boolean date = true;

    private FragmentManager fragmentManager;

    public boolean isSelectionMode;
    public int selectionCounts = 0;

    public SelectionListener listener;

    private Drawable icon_more;
    private Drawable icon_check;
    private Drawable icon_uncheck;


    public VideoAdapter(Context context, List<Video> mediaList, FragmentManager fragmentManager) {
        this.context = context;
        this.mediaList = mediaList;
        this.fragmentManager = fragmentManager;

        icon_more = ContextCompat.getDrawable(context, R.drawable.baseline_more_vert_24);
        icon_check = ContextCompat.getDrawable(context, R.drawable.round_check_circle);
        icon_uncheck = ContextCompat.getDrawable(context, R.drawable.round_check_circle_outline);
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout, parent, false);

        VideoViewHolder holder = new VideoViewHolder(view);

        return holder;
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView name;
        TextView path;
        TextView dateAdded;
        TextView duration1;
        TextView duration2;
        TextView resolutionFrame;
        TextView size;
        ImageButton moreB;

        public VideoViewHolder(View item_layout) {
            super(item_layout);
            thumbnail = item_layout.findViewById(R.id.thumbnail);
            name = item_layout.findViewById(R.id.name);
            path = item_layout.findViewById(R.id.path);
            dateAdded = item_layout.findViewById(R.id.t4_date);
            duration1 = item_layout.findViewById(R.id.duration);
            duration2 = item_layout.findViewById(R.id.t1_duration);
            resolutionFrame = item_layout.findViewById(R.id.t2_resolutionFrame);
            size = item_layout.findViewById(R.id.t3_size);
            moreB = item_layout.findViewById(R.id.moreB_item);
        }
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        Video media = mediaList.get(holder.getBindingAdapterPosition());
        holder.name.setText(media.getName());
        holder.path.setText(media.getPath());
        holder.dateAdded.setText(getFormattedDate(Long.parseLong(media.getDateAdded())));

        setVisibilities(holder);

        if (isSelectionMode) {
            holder.moreB.setEnabled(false);
            if (media.isSelected) {
                holder.itemView.setSelected(true);
                holder.moreB.setImageDrawable(icon_check);
            }
            else {
                holder.itemView.setSelected(false);
                holder.moreB.setImageDrawable(icon_uncheck);
            }
        }
        else {
            holder.itemView.setSelected(false);
            holder.moreB.setEnabled(true);
            holder.moreB.setImageDrawable(icon_more);
        }

        if (media.getDuration() == null || media.getResolution() == null || media.getSize() == null) {
                new FFmpegMetadataRetriever(media.getPath(), retriever -> {
                    try {

                        String sizeInBytes = String.valueOf(retriever.getFileSize());
    //                    String size = getFormattedFileSize(sizeInBytes);

                        String duration = String.valueOf(retriever.getDuration());
                        String width = String.valueOf(retriever.getResolution());
                        String height = String.valueOf(retriever.getResolution());
                        String fps = String.valueOf(retriever.getFps());

                        // Setting all Values
                        media.setSize(sizeInBytes);
                        media.setDuration(duration);
                        media.setResolution(height);
                        media.setFrameRate(fps);

                        Log.d("MediaExtractor", "File Size: " + getFormattedFileSize(Long.parseLong(media.getSize())) + " MB, Duration: " + MillisToTime(Long.parseLong(duration)) + ", Resolution: " + width + "x" + height + ", FPS: " + fps);

                        if (holder.getBindingAdapterPosition() == mediaList.size() - 1) {
                            saveMediaListToPreferences(mediaList);
                        }
                    }

                    catch (Exception e) {
                        e.printStackTrace();
                    }

                    holder.itemView.post(() -> {
                        holder.duration1.setText(SecsToTime(Long.parseLong(media.getDuration() != null ? media.getDuration() : "0")));
                        holder.duration2.setText(SecsToTime(Long.parseLong(media.getDuration() != null ? media.getDuration() : "0")));

                        if (resol || fps) {
                            holder.resolutionFrame.setText(
                                    resol && fps ? media.getResolution() + "P@" + media.getFrameRate() :
                                            resol ? media.getResolution() + "P" : media.getFrameRate() + "FPS");
                        }
                        else {
                            holder.resolutionFrame.setVisibility(View.GONE);
                        }

                        holder.size.setText(getFormattedFileSize(Long.parseLong(media.getSize())));
                    });
                });

        }

        else {
            Log.d("MediaExtractor", "onBindViewHolder: " + media.getDuration());

            // MediaMetadataRetriever Thread
            holder.duration1.setText(SecsToTime(Long.parseLong(media.getDuration())));
            holder.duration2.setText(SecsToTime(Long.parseLong(media.getDuration())));

            if (resol || fps) {
                holder.resolutionFrame.setText(
                        resol && fps ? media.getResolution() + "P@" + media.getFrameRate() :
                                resol ? media.getResolution() + "P" : media.getFrameRate() + "FPS");
            }
            else {
                holder.resolutionFrame.setVisibility(View.GONE);
            }

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

        Log.d("Video Added", "Added");


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @OptIn(markerClass = UnstableApi.class)
            @Override
            public void onClick(View v) {
                Log.d("Item Clicked", holder.getBindingAdapterPosition() + " : " + media.getName());

                if (isSelectionMode) {
                    toggleSelection(holder.getBindingAdapterPosition());
                }
                else {
                    Intent intent = new Intent(context, PlayerActivity.class);
                    intent.putExtra("Name", media.getName());
                    intent.putExtra("Path", media.getPath());
                    intent.putExtra("isVideo", media.isVideo());
                    intent.putExtra("currentIndex", holder.getBindingAdapterPosition());
                    Log.d("isVideoFile", "onClick: " + media.isVideo());
                    context.startActivity(intent);
                }
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            int p = holder.getBindingAdapterPosition();
            if (!isSelectionMode) {
                isSelectionMode = true;
                listener.onSelectionStarts();
                notifyDataSetChanged();
            }
            toggleSelection(p);
            return true;
        });

        holder.moreB.setOnClickListener(v -> {
            int p = holder.getBindingAdapterPosition();
            MyBottomSheet sheet = new MyBottomSheet(p);
            sheet.show(fragmentManager, sheet.getTag());
        });
    }


    private void toggleSelection(int position) {
        Video item = mediaList.get(position);
        item.isSelected = !item.isSelected;
        notifyItemChanged(position);
        selectionCounts = item.isSelected ? selectionCounts+1 : selectionCounts-1;
        listener.onCountChanged(selectionCounts);

        if (selectionCounts == 0) {
            isSelectionMode = false;
            listener.onSelectionEnds();
            notifyDataSetChanged();
        }
    }

    public void addSelectionListener(SelectionListener listener) {
        this.listener = listener;
    }

    public boolean isAllSelected() {
        return selectionCounts == mediaList.size();
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

    @Override
    public int getItemCount() {
        if (mediaList != null) {
            return mediaList.size();
        }
        return 0;
    }

    public void setDetailsVisibility(boolean isPath, boolean isResol, boolean isFps, boolean isSize, boolean isDate, boolean isDur, boolean isDur_onThumb) {
        path = isPath;
        resol = isResol;
        fps = isFps;
        size = isSize;
        date = isDate;
        dur = isDur;
        dur_onThumbnail = isDur_onThumb;
        notifyDataSetChanged();
    }

    private void setVisibilities(VideoViewHolder holder) {
        if (path) holder.path.setVisibility(View.VISIBLE);
        else holder.path.setVisibility(View.GONE);

        if (size) holder.size.setVisibility(View.VISIBLE);
        else holder.size.setVisibility(View.GONE);

        if (date) holder.dateAdded.setVisibility(View.VISIBLE);
        else holder.dateAdded.setVisibility(View.GONE);

        if (resol || fps) holder.resolutionFrame.setVisibility(View.VISIBLE);
        else holder.resolutionFrame.setVisibility(View.GONE);

        holder.duration1.setVisibility((dur && dur_onThumbnail) ? View.VISIBLE : View.GONE);
        holder.duration2.setVisibility((dur && !dur_onThumbnail) ? View.VISIBLE : View.GONE);
    }

    private void saveMediaListToPreferences(List<Video> mediaList) {
        new Thread(() -> {
            try {
                SharedPreferences prefs = context.getSharedPreferences("MediaPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                Gson gson = new Gson();
                String json = gson.toJson(mediaList);
                editor.putString("videoList", json);
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