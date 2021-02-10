package com.spidchenko.week2task.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.spidchenko.week2task.db.models.SyncImage;

import java.util.List;

@Dao
public interface SyncImageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long addSyncImage(SyncImage syncImage);

    @Query("DELETE FROM syncImages WHERE url LIKE :url")
    void deleteSyncImage(String url);

    @Query("SELECT * FROM syncImages")
    LiveData<List<SyncImage>> getAllImages();
}
