package com.example.mediaplayer.Room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.mediaplayer.Video.Video;

import java.util.List;

@Dao
public interface VideoDao {
    @Insert
    void insert(Video video);

    @Delete
    void delete(Video video);

    @Query("select * from video")
    LiveData<List<Video>> getList();
}
