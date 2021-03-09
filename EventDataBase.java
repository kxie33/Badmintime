package com.example.notificationtest;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 *
 */
@Database(entities = {ClubEvent.class}, version = 14,exportSchema = false)
@TypeConverters({Converters.class})
public abstract class EventDataBase extends RoomDatabase  {

    /**
     *
     * @return
     */
    public abstract ClubEventDao clubEventDao();
}
