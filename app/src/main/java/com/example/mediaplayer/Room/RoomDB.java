package com.example.mediaplayer.Room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.mediaplayer.Audio.Audio;
import com.example.mediaplayer.Video.Video;


@Database(entities = {Audio.class, Video.class}, version = 1, exportSchema = false)
public abstract class RoomDB extends RoomDatabase {

    public abstract AudioDao audioDao();
    public abstract VideoDao videoDao();

    private static volatile RoomDB instance;

    public static RoomDB getDatabase(Context context) {
        if (instance != null) {
            synchronized (RoomDB.class) {
                if (instance != null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    RoomDB.class, "MediaDB")
                            .build();
                }
            }
        }
        return instance;
    }
}
