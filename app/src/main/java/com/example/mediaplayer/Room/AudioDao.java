package com.example.mediaplayer.Room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mediaplayer.Audio.Audio;
import com.example.mediaplayer.Video.Video;

import java.util.List;

@Dao
public interface AudioDao {
    @Insert (onConflict = OnConflictStrategy.IGNORE)
    void insert(Audio audio);

    @Update
    void update(Audio audio);

    @Delete
    void delete(Audio audio);


    @Insert (onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Audio> audios);

    @Query("delete from audio where uri not in (:uris)")
    void deleteAllByUris(List<String> uris);

    @Query("delete from audio where uri = :uri")
    void deleteByUri(String uri);

    @Query("select * from audio order by name")
    List<Audio> getDirectList();

    @Query("SELECT * FROM audio WHERE uri = :uri")
    Audio getByUri(String uri);



    @Query("select * from audio order by name")
    LiveData<List<Audio>> getListByName();

    @Query("select * from audio order by name desc")
    LiveData<List<Audio>> getListByNameDesc();


    @Query("select * from audio order by dateAdded")
    LiveData<List<Audio>> getListByDate();

    @Query("select * from audio order by dateAdded desc")
    LiveData<List<Audio>> getListByDateDesc();


    @Query("select * from audio order by size")
    LiveData<List<Audio>> getListBySize();

    @Query("select * from audio order by size desc")
    LiveData<List<Audio>> getListBySizeDesc();


    @Query("select * from audio order by duration")
    LiveData<List<Audio>> getListByDuration();

    @Query("select * from audio order by duration desc")
    LiveData<List<Audio>> getListByDurationDesc();

}
