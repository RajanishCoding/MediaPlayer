package com.example.mediaplayer.Extra;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import com.arthenica.ffmpegkit.FFprobeKit;
import com.arthenica.ffmpegkit.ReturnCode;


public class FFmpegMetadataRetriever {

    private long fileSize;
    private double durationSec;
    private int bitrate;
    private int width;
    private int height;
    private double frameRate;
//    private boolean hasDolbyAtmos;
//    private boolean hasDolbyVision;

    public interface MetadataCallback {
        void onMetadataReady(FFmpegMetadataRetriever metadata);
    }

    public FFmpegMetadataRetriever(String filePath, MetadataCallback callback) {
        // FFprobe command to get metadata in JSON format
        String cmd = "-v quiet -print_format json -show_format -show_streams \"" + filePath + "\"";

        FFprobeKit.executeAsync(cmd, session -> {
            if (ReturnCode.isSuccess(session.getReturnCode())) {
                try {

                    String output = session.getOutput(); // ✅ FIX: Use session.getOutput() in v6.0.2
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

                    // Notify when metadata is ready
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

    public long getFileSize() {
        return fileSize;
    }

    public Long getDuration() {
        return (long) durationSec;
    }

    public int getBitrate() {
        return bitrate;
    }

    public int getResolution() {
        return height;
    }

    public double getFps() {
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
