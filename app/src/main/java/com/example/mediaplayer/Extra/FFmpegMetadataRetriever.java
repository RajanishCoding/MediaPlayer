package com.example.mediaplayer.Extra;

import android.content.Context;
import android.util.Log;

import androidx.annotation.OptIn;
import androidx.core.content.ContextCompat;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.Tracks;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.source.TrackGroupArray;
import androidx.media3.inspector.MetadataRetriever;


import org.json.JSONArray;
import org.json.JSONObject;

import com.antonkarpenko.ffmpegkit.FFprobeKit;
import com.antonkarpenko.ffmpegkit.ReturnCode;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class FFmpegMetadataRetriever {

    private Long fileSize;
    private Double durationSec;
    private Integer bitrate;
    private Integer width;
    private Integer height;
    private Double frameRate;
//    private boolean hasDolbyAtmos;
//    private boolean hasDolbyVision;

    public interface MetadataCallback {
        void onMetadataReady(FFmpegMetadataRetriever metadata);
    }

    public FFmpegMetadataRetriever(Context context, String filePath, MetadataCallback callback) {
        // FFprobe command to get metadata in JSON format
        String cmd = "-v quiet -print_format json -show_format -show_streams \"" + filePath + "\"";

        FFprobeKit.executeAsync(cmd, session -> {
            if (ReturnCode.isSuccess(session.getReturnCode())) {
                try {

                    String output = session.getOutput();
//                    Log.d("djkfhkd", output);

                    JSONObject json = new JSONObject(output);

                    // Get duration
                    durationSec = json.getJSONObject("format").getDouble("duration");
                    String duration = formatDuration(durationSec);

                    // Get file size
                    fileSize = json.getJSONObject("format").getLong("size");

                    // Get bitrate
                    bitrate = json.getJSONObject("format").optInt("bit_rate", 0);

                    // Get video stream details
                    JSONArray streams = json.getJSONArray("streams");
                    JSONObject videoStream = null;
                    for (int i = 0; i < streams.length(); i++) {
                        JSONObject stream = streams.getJSONObject(i);
                        if (stream.getString("codec_type").equals("video")) {
                            videoStream = stream;
                        }
                    }

                    Log.d("Metafdata", "Duration: " + duration);
                    Log.d("Metafdata", "File Size: " + fileSize + " bytes");

                    if (videoStream != null) {
                        width = videoStream.optInt("width", 0);
                        height = videoStream.optInt("height", 0);
                        frameRate = evalFrameRate(videoStream.getString("r_frame_rate"));

                        // Log metadata (similar to MediaMetadataRetriever)
                        Log.d("Metafdata", "Bitrate: " + bitrate + " bps");
                        Log.d("Metafdata", "Resolution: " + width + "x" + height);
                        Log.d("Metafdata", "Frame Rate: " + frameRate + " fps");
                    }

                    // Now metadata is ready
                    callback.onMetadataReady(this);

                } catch (Exception e) {
                    Log.e("Metafdata", "Error parsing metadata", e);
                }
            }
            else {
                Log.e("Metafdata", "Error executing FFprobe");
            }
        });
    }


    public Long getFileSize() {
        return fileSize;
    }

    public Long getDuration() {
        return durationSec.longValue();
    }

    public Integer getBitrate() {
        return bitrate;
    }

    public Integer getResolution() {
        return height;
    }

    public Double getFps() {
        return frameRate;
    }


    private static String formatDuration(double seconds) {
        int h = (int) (seconds / 3600);
        int m = (int) ((seconds % 3600) / 60);
        int s = (int) (seconds % 60);
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    private static double evalFrameRate(String rate) {
        if (rate.contains("/")) {
            String[] parts = rate.split("/");
            return Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
        }
        return Double.parseDouble(rate);
    }

//    return ArrayList<String>["dd"];
}


//@OptIn(markerClass = UnstableApi.class)
//public FFmpegMetadataRetriever(Context context, String filePath, MetadataCallback callback) {
//    // 1. Get File Size (Native Java)
//    File file = new File(filePath);
//    this.fileSize = file.length();
//
//    // 2. Setup Media3 Retriever
//    MediaItem mediaItem = MediaItem.fromUri(filePath);
//    MetadataRetriever retriever = new MetadataRetriever.Builder(context, mediaItem).build();
//
//    ListenableFuture<TrackGroupArray> trackGroupsFuture = retriever.retrieveTrackGroups();
//    ListenableFuture<Long> durationUsFuture = retriever.retrieveDurationUs();
//
//    ListenableFuture<List<Object>> allFutures = Futures.allAsList(trackGroupsFuture, durationUsFuture);
//
//    Futures.addCallback(allFutures, new FutureCallback<List<Object>>() {
//        @Override
//        public void onSuccess(List<Object> result) {
//            TrackGroupArray trackGroupArray = (TrackGroupArray) result.get(0);
//
//            long durationUs = (long) result.get(1);
//            FFmpegMetadataRetriever.this.durationSec = durationUs / 1000000.0;
//
//            // Loop through track group array to find the video track
//            for (int i = 0; i < trackGroupArray.length; i++) {
//
//                Format format = trackGroupArray.get(i).getFormat(0);
//
//                // We only want the Video stream for these specific details
//                if (format.sampleMimeType != null && format.sampleMimeType.startsWith("video")) {
//
//                    // Extract Media3 Data
//                    FFmpegMetadataRetriever.this.bitrate = format.bitrate == Format.NO_VALUE ? 0 : format.bitrate;
//                    FFmpegMetadataRetriever.this.width = format.width == Format.NO_VALUE ? 0 : format.width;
//                    FFmpegMetadataRetriever.this.height = format.height == Format.NO_VALUE ? 0 : format.height;
//                    FFmpegMetadataRetriever.this.frameRate = format.frameRate == Format.NO_VALUE ? 0.0 : (double) format.frameRate;
//
//                    break; // Stop after finding the primary video track
//                }
//            }
//
//            callback.onMetadataReady(FFmpegMetadataRetriever.this);
//        }
//
//        @Override
//        public void onFailure(Throwable t) {
//
//        }
//    }, ContextCompat.getMainExecutor(context));
//
//}


//public class FFmpegMetadataRetriever {
//
//    private long fileSize;
//    private double durationSec;
//    private int bitrate;
//    private int width;
//    private int height;
//    private double frameRate;
//
//    public interface MetadataCallback {
//        void onMetadataReady(FFmpegMetadataRetriever metadata);
//    }
//
//    public FFmpegMetadataRetriever(String filePath, MetadataCallback callback) {
//        new Thread(() -> {
//            try {
//                MediaInfo mediaInfo = new MediaInfo();
//                mediaInfo.Open(filePath);
//                String jsonString = mediaInfo.Inform();
//                mediaInfo.Close();
//
//                JSONObject json = new JSONObject(jsonString);
//
//                // Get general track info
//                JSONArray tracks = json.getJSONArray("media").getJSONObject(0).getJSONArray("track");
//                JSONObject general = null;
//                JSONObject video = null;
//
//                for (int i = 0; i < tracks.length(); i++) {
//                    JSONObject track = tracks.getJSONObject(i);
//                    String type = track.getString("@type");
//                    if (type.equals("General")) general = track;
//                    if (type.equals("Video")) video = track;
//                }
//
//                if (general != null) {
//                    durationSec = Double.parseDouble(general.optString("Duration", "0")) / 1000.0;
//                    String duration = formatDuration(durationSec);
//
//                    fileSize = Long.parseLong(general.optString("FileSize", "0"));
//                    bitrate = Integer.parseInt(general.optString("OverallBitRate", "0"));
//
//                    Log.d("Metafdata", "Duration: " + duration);
//                    Log.d("Metafdata", "File Size: " + fileSize + " bytes");
//                }
//
//                if (video != null) {
//                    width = Integer.parseInt(video.optString("Width", "0"));
//                    height = Integer.parseInt(video.optString("Height", "0"));
//                    frameRate = Double.parseDouble(video.optString("FrameRate", "0"));
//
//                    Log.d("Metafdata", "Bitrate: " + bitrate + " bps");
//                    Log.d("Metafdata", "Resolution: " + width + "x" + height);
//                    Log.d("Metafdata", "Frame Rate: " + frameRate + " fps");
//                }
//
//                callback.onMetadataReady(this);
//
//            } catch (Exception e) {
//                Log.e("Metafdata", "Error parsing metadata", e);
//            }
//        }).start();
//    }
//
//    public long getFileSize() {
//        return fileSize;
//    }
//
//    public Long getDuration() {
//        return (long) durationSec;
//    }
//
//    public int getBitrate() {
//        return bitrate;
//    }
//
//    public int getResolution() {
//        return height;
//    }
//
//    public double getFps() {
//        return frameRate;
//    }
//
//    private static String formatDuration(double seconds) {
//        int h = (int) (seconds / 3600);
//        int m = (int) ((seconds % 3600) / 60);
//        int s = (int) (seconds % 60);
//        return String.format("%02d:%02d:%02d", h, m, s);
//    }
//
//    private static double evalFrameRate(String rate) {
//        if (rate.contains("/")) {
//            String[] parts = rate.split("/");
//            return Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
//        }
//        return Double.parseDouble(rate);
//    }
//}
