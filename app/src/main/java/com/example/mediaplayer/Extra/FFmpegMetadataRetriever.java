package com.example.mediaplayer.Extra;

import android.content.Context;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONObject;

import com.antonkarpenko.ffmpegkit.FFprobeKit;
import com.antonkarpenko.ffmpegkit.ReturnCode;


public class FFmpegMetadataRetriever {

    private Long fileSize;
    private Double durationSec;
    private Integer bitrate;
    private Integer width;
    private Integer height;
    private Double frameRate;
    private String extraMetadata;
//    private boolean hasDolbyAtmos;
//    private boolean hasDolbyVision;

    public enum ValueType {
        STRING,
        NUMBER_1,
        NUMBER_0,
        DOUBLE
    }

    public interface MetadataCallback {
        void onMetadataReady(FFmpegMetadataRetriever metadata, String extraMetadataJsonString);
    }

    public FFmpegMetadataRetriever(Context context, String filePath, MetadataCallback callback) {
        // FFprobe command to get metadata in JSON format
//        String cmd = "-v quiet -print_format json -show_format -select_streams v:0 -show_entries format=duration,size:stream=width,height,r_frame_rate \"" + filePath + "\"";
        String cmd = "-v quiet -print_format json -show_format -show_streams \"" + filePath + "\"";

        FFprobeKit.executeAsync(cmd, session -> {
            if (ReturnCode.isSuccess(session.getReturnCode())) {
                try {
                    String output = session.getOutput();

                    JSONObject json = new JSONObject(output);

                    JSONObject extraMetadata = new JSONObject();
                    JSONObject hdrDetails = new JSONObject();
                    JSONArray audioArray = new JSONArray();
                    JSONArray subtitleArray = new JSONArray();

                    JSONObject formatObj = json.optJSONObject("format");
                    if (formatObj != null) {
                        durationSec = formatObj.optDouble("duration");
                        fileSize = formatObj.optLong("size");

                        JSONObject format = new JSONObject();
                        checkAndPutJson(format, "format_name", formatObj.opt("format_name"), ValueType.STRING);
                        checkAndPutJson(format, "bitrate", formatObj.opt("bit_rate"), ValueType.NUMBER_1);

                        JSONObject tags = formatObj.optJSONObject("tags");
                        if (tags != null) {
                            checkAndPutJson(format, "title", tags.optString("title"), ValueType.STRING);
//                            checkAndPutJson(format, "encoder", tags.optString("encoder"), ValueType.STRING);
                        }
                        extraMetadata.put("format", format);
                    }

                    // Streams
                    JSONArray streams = json.optJSONArray("streams");
                    if (streams != null && streams.length() > 0) {
                        boolean flag = false;
                        for (int i = 0; i < streams.length(); i++) {
                            JSONObject s = streams.optJSONObject(i);
                            if (s == null) continue;

                            String type = s.optString("codec_type");

                            if ("video".equals(type) && !flag) {
                                width = s.optInt("width", 0);
                                height = s.optInt("height", 0);
                                frameRate = evalFrameRate(s.getString("r_frame_rate"));

                                JSONObject video = new JSONObject();
                                checkAndPutJson(video, "codec", s.optString("codec_name"), ValueType.STRING);
                                checkAndPutJson(video, "profile", s.optString("profile"), ValueType.STRING);
                                checkAndPutJson(video, "width", s.optString("width"), ValueType.NUMBER_1);
                                checkAndPutJson(video, "bitrate", s.optString("bit_rate"), ValueType.NUMBER_1);
                                checkAndPutJson(video, "color_range", s.optString("color_range"), ValueType.STRING);
                                checkAndPutJson(video, "color_space", s.optString("color_space"), ValueType.STRING);

                                boolean isDolbyVision = false;
                                boolean isHDR10Plus = false;
                                boolean isHDR10 = false;
                                boolean isHLG = false;
                                boolean isHDR10Compatible = false;
                                boolean isHDR10PlusCompatible = false;
                                int dvProfile = -1;
                                int dvLevel = -1;

                                // transfer & primaries
                                String transfer = s.optString("color_transfer");
                                String primaries = s.optString("color_primaries");
                                checkAndPutJson(video, "color_transfer", transfer, ValueType.STRING);
                                checkAndPutJson(video, "color_primaries", primaries, ValueType.STRING);

                                // Bit depth
                                String pixFmt = s.optString("pix_fmt", "");
                                int bitDepth = 8;
                                if (pixFmt.contains("12")) bitDepth = 12;
                                else if (pixFmt.contains("10")) bitDepth = 10;
                                video.put("bit_depth", bitDepth);

                                boolean isPQ = "smpte2084".equals(transfer);
                                boolean isHLGTransfer = "arib-std-b67".equals(transfer);
                                boolean isBT2020 = "bt2020".equals(primaries);

                                // Side data parsing
                                JSONArray sideDataList = s.optJSONArray("side_data_list");
                                if (sideDataList != null) {
                                    for (int j = 0; j < sideDataList.length(); j++) {
                                        JSONObject sd = sideDataList.optJSONObject(j);
                                        if (sd == null) continue;

                                        String stype = sd.optString("side_data_type");

                                        if (stype.contains("DOVI")) {
                                            isDolbyVision = true;
                                            dvProfile = sd.optInt("dv_profile", -1);
                                            dvLevel = sd.optInt("dv_level", -1);
                                        }

                                        if (stype.contains("HDR10+") || stype.contains("ST 2094")) {
                                            isHDR10Plus = true;
                                        }

                                        if (stype.contains("Mastering display metadata")) {
                                            isHDR10 = true;
                                        }
                                    }
                                }

                                // HDR10 checking again
                                if (isPQ && isBT2020 && bitDepth >= 10) {
                                    isHDR10 = true;
                                }

                                // HLG
                                if (isHLGTransfer && isBT2020) {
                                    isHLG = true;
                                }

                                // Compatibility
                                if (isDolbyVision && isPQ && isBT2020) {
                                    isHDR10Compatible = true;
                                }

                                if (isDolbyVision && dvProfile >= 8 && isHDR10Compatible) {
                                    isHDR10PlusCompatible = true;
                                }

                                hdrDetails.put("isDV", isDolbyVision);
                                checkAndPutJson(hdrDetails, "dv_profile", dvProfile, ValueType.NUMBER_0);
                                checkAndPutJson(hdrDetails, "dv_level", dvLevel, ValueType.NUMBER_0);
                                hdrDetails.put("isHDR10", isHDR10);
                                hdrDetails.put("isHLG", isHLG);
                                hdrDetails.put("isHDR10+", isHDR10Plus);
                                hdrDetails.put("isHDR10Compatible", isHDR10Compatible);
                                hdrDetails.put("isHDR10+Compatible", isHDR10PlusCompatible);

                                JSONObject tags = s.optJSONObject("tags");
                                if (tags != null) {
                                    checkAndPutJson(video, "title", tags.optString("title"), ValueType.STRING);
                                    checkAndPutJson(video, "language", tags.optString("language"), ValueType.STRING);
                                }

                                video.put("hdrDetails", hdrDetails);
                                extraMetadata.put("video", video);
                                flag = true;
                            }

                            if ("audio".equals(type)) {
                                JSONObject audio = new JSONObject();
                                checkAndPutJson(audio, "index", s.optInt("index"), ValueType.NUMBER_0);
                                checkAndPutJson(audio, "codec", s.optString("codec_name"), ValueType.STRING);
                                checkAndPutJson(audio, "channels", s.optInt("channels"), ValueType.NUMBER_1);
                                checkAndPutJson(audio, "channel_layout", s.optString("channel_layout"), ValueType.STRING);
                                checkAndPutJson(audio, "sample_rate", s.optInt("sample_rate"), ValueType.NUMBER_1);
                                checkAndPutJson(audio, "bitrate", s.optInt("bit_rate"), ValueType.NUMBER_1);

                                JSONObject tags = s.optJSONObject("tags");
                                if (tags != null) {
                                    checkAndPutJson(audio, "title", tags.optString("title"), ValueType.STRING);
                                    checkAndPutJson(audio, "language", tags.optString("language"), ValueType.STRING);
                                }

                                audioArray.put(audio);
                            }

                            if ("subtitle".equals(type)) {
                                JSONObject sub = new JSONObject();
                                checkAndPutJson(sub, "index", s.optInt("index"), ValueType.NUMBER_0);
                                checkAndPutJson(sub, "codec", s.optString("codec_name"), ValueType.STRING);

                                JSONObject tags = s.optJSONObject("tags");
                                if (tags != null) {
                                    checkAndPutJson(sub, "title", tags.optString("title"), ValueType.STRING);
                                    checkAndPutJson(sub, "language", tags.optString("language"), ValueType.STRING);
                                }

                                subtitleArray.put(sub);
                            }

                            if (audioArray.length() > 0) {
                                extraMetadata.put("audio", audioArray);
                            }

                            if (subtitleArray.length() > 0) {
                                extraMetadata.put("subtitles", subtitleArray);
                            }
                        }
                    }

                    // Now metadata is ready
                    callback.onMetadataReady(this, extraMetadata.toString());

                } catch (Exception e) {
                    Log.e("Metafdata1", "Error parsing metadata" + filePath + e);
                }
            } else {
                Log.e("Metafdata1", "Error executing FFprobe");
            }
            });
    }


    public Long getFileSize() {
        return fileSize;
    }

    public Long getDuration() {
        return Math.round(durationSec);
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


    private void checkAndPutJson(JSONObject json, String key, Object value, ValueType type) {
        if (value == null) return;

        try {
            switch (type) {

                case STRING: {
                    String s = value.toString().trim();
                    if (s.isEmpty()) return;

                    String l = s.toLowerCase();
                    if (l.equals("null") || l.equals("n/a") || l.equals("na") || l.equals("unknown") || l.equals("und"))
                        return;

                    json.put(key, s);
                    break;
                }

                case NUMBER_0: {
                    long n = (value instanceof Number)
                            ? ((Number) value).longValue()
                            : Long.parseLong(value.toString().trim());

                    if (n <= 0) return;
                    json.put(key, n);
                    break;
                }

                case NUMBER_1: {
                    long n = (value instanceof Number)
                            ? ((Number) value).longValue()
                            : Long.parseLong(value.toString().trim());

                    json.put(key, n);
                    break;
                }

                case DOUBLE: {
                    double d = (value instanceof Number)
                            ? ((Number) value).doubleValue()
                            : Double.parseDouble(value.toString().trim());

                    if (Double.isNaN(d) || Double.isInfinite(d) || d <= 0.0) return;
                    json.put(key, d);
                    break;
                }
            }
        } catch (Exception ignored) {
        }
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
