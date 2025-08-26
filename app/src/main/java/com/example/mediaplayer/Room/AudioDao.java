package com.example.mediaplayer.Room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.mediaplayer.Audio.Audio;

import java.util.List;

@Dao
public interface AudioDao {
    @Insert
    void insert(Audio audio);

    @Delete
    void delete(Audio audio);

    @Query("select * from audio")
    List<Audio> getList();
}
