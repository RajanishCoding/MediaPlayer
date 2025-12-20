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

    @Query("SELECT * FROM video WHERE uri = :uri")
    Video getByUri(String uri);



    @Query("select * from video order by name")
    LiveData<List<Video>> getListByName();

    @Query("select * from video order by name desc")
    LiveData<List<Video>> getListByNameDesc();


    @Query("select * from video order by dateAdded")
    LiveData<List<Video>> getListByDate();

    @Query("select * from video order by dateAdded desc")
    LiveData<List<Video>> getListByDateDesc();


    @Query("select * from video order by size")
    LiveData<List<Video>> getListBySize();

    @Query("select * from video order by size desc")
    LiveData<List<Video>> getListBySizeDesc();


    @Query("select * from video order by duration")
    LiveData<List<Video>> getListByDuration();

    @Query("select * from video order by duration desc")
    LiveData<List<Video>> getListByDurationDesc();


    @Query("select * from video order by resolution")
    LiveData<List<Video>> getListByResol();

    @Query("select * from video order by resolution desc")
    LiveData<List<Video>> getListByResolDesc();


    @Query("select * from video order by frameRate")
    LiveData<List<Video>> getListByFrame();

    @Query("select * from video order by frameRate desc")
    LiveData<List<Video>> getListByFrameDesc();
}
