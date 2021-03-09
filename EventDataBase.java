package com.example.notificationtest;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * Class: EventDataBase. This class is the room database interface.
 */
@Database(entities = {ClubEvent.class}, version = 14,exportSchema = false)
@TypeConverters({Converters.class})
public abstract class EventDataBase extends RoomDatabase  {

    /**
     * This is the room database method to setup the event dao
     * @return ClubEventDoa
     */
    public abstract ClubEventDao clubEventDao();
}
