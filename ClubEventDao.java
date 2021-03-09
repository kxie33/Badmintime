package com.example.notificationtest;

import java.util.List;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

/**
 * CLass: ClubEventDao. This is the interface of ClubEventDao. Methods here are used to access and manage data in room database.
 */
@Dao
public interface ClubEventDao {

    /**
     * Get all club events from room database
     * @return list of all club events
     */
    @Query("SELECT * FROM clubevent")
    List<ClubEvent> getAll();

    /**
     * Insert all input events into room databse
     * @param events all events need to be inserted
     */
    @Insert
    void insertAll(ClubEvent... events);

    /**
     * Delete target club event from room database
     * @param event the target club event
     */
    @Delete
    void delete(ClubEvent event);

    /**
     * Insert a new club event into room data base
     * @param event the new club event
     * @return insert result
     */
    @Insert
    long insertNew(ClubEvent event);

    /**
     * Update an existing club event in the database.
     * @param event the existing event
     * @return update result
     */
    @Update
    int update(ClubEvent event);
}
