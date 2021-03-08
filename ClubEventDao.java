package com.example.notificationtest;

import java.util.List;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

/**
 *
 */
@Dao
public interface ClubEventDao {

    /**
     *
     * @return
     */
    @Query("SELECT * FROM clubevent")
    List<ClubEvent> getAll();

    /**
     *
     * @param events
     */
    @Insert
    void insertAll(ClubEvent... events);

    /**
     *
     * @param event
     */
    @Delete
    void delete(ClubEvent event);

    /**
     *
     * @param event
     * @return
     */
    @Insert
    long insertNew(ClubEvent event);

    /**
     * Update an existing club event in the database.
     * @param event the existing event
     * @return
     */
    @Update
    public int update(ClubEvent event);
}
