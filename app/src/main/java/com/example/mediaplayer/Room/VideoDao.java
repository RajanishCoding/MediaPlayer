package com.example.mediaplayer.Room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Delete;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;
import androidx.room.Query;

import com.example.mediaplayer.Video.Video;

import java.util.List;

@Dao
public interface VideoDao {
    @Insert (onConflict = OnConflictStrategy.IGNORE)
    void insert(Video video);

    @Update
    void update(Video video);

    @Delete
    void delete(Video video);

    @Query("select * from video order by name")
    LiveData<List<Video>> getList();
}
