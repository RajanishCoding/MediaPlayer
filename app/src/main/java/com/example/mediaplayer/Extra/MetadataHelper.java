package com.example.mediaplayer.Extra;

import org.json.JSONObject;

public class MetadataHelper {

    private final JSONObject json;

    public MetadataHelper(String jsonStr) {
        try {
            json = jsonStr != null ? new JSONObject(jsonStr) : new JSONObject();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public String getJsonString() {
        return json.toString();
    }

    public String getString(String key) {
        return json.optString(key, "");
    }

    public int getInt(String key) {
        return json.optInt(key, 0);
    }

    public boolean getBoolean(String key) {
        return json.optBoolean(key, false);
    }
}
