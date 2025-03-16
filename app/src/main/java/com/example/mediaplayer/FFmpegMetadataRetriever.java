package com.example.mediaplayer;

import android.util.Log;

import com.arthenica.ffmpegkit.FFprobeKit;
import com.arthenica.ffmpegkit.ReturnCode;

import org.json.JSONArray;
import org.json.JSONObject;


public class FFmpegMetadataRetriever {

    private long fileSize;
    private double durationSec;
    private int bitrate;
    private int width;
    private int height;
    private double frameRate;

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
                        if (streams.getJSONObject(i).getString("codec_type").equals("video")) {
                            videoStream = streams.getJSONObject(i);
                            break;
                        }
                    }

                    if (videoStream != null) {
                        width = videoStream.optInt("width", 0);
                        height = videoStream.optInt("height", 0);
                        frameRate = evalFrameRate(videoStream.getString("r_frame_rate"));

                        // Log metadata (similar to MediaMetadataRetriever)
                        Log.d("Metafdata", "Duration: " + duration);
                        Log.d("Metafdata", "File Size: " + fileSize + " bytes");
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