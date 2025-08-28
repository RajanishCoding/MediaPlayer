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


    @Insert (onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Video> videos);

    @Query("delete from video where uri not in (:uris)")
    void deleteAllByUris(List<String> uris);

    @Query("delete from video where uri = :uri")
    void deleteByUri(String uri);

    @Query("select * from video order by name")
    List<Video> getDirectList();



    @Query("select * from video order by name")
    LiveData<List<Video>> getList();
}
