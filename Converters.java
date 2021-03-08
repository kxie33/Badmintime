package com.example.notificationtest;

import android.content.ContentResolver;
import android.os.Build;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import androidx.annotation.RequiresApi;
import androidx.room.TypeConverter;

/**
 *
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class Converters {
    static Calendar cal = Calendar.getInstance();
    static int offset = cal.get(Calendar.ZONE_OFFSET);
    static ZoneOffset zone = ZoneOffset.ofTotalSeconds(offset/1000);

    /**
     *
     * @param t
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @TypeConverter
    public static Integer fromLocalTime(LocalTime t){
        if (t == null){
            return null;
        }
        return t.toSecondOfDay();
    }

    /**
     *
     * @param i
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @TypeConverter
    public static LocalTime toLocalTime(Integer i){
        if (i == null){
            return null;
        }
        return LocalTime.ofSecondOfDay(i);
    }

    /**
     *
     * @param d
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @TypeConverter
    public static Long fromLocalDate(LocalDate d){
        if (d == null){
            return null;
        }
        return d.toEpochDay();
    }

    /**
     *
     * @param l
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @TypeConverter
    public static LocalDate toLocalDate(Long l){
        if (l == null){
            return null;
        }
        return LocalDate.ofEpochDay(l);
    }

    /**
     *
     * @param l
     * @return
     */
    @TypeConverter
    public static Long fromLocalDateTime(LocalDateTime l){
        if (l == null){
            return null;
        }
        return l.toEpochSecond(zone);
    }

    /**
     *
     * @param l
     * @return
     */
    @TypeConverter
    public static LocalDateTime toLocalDateTime(Long l){
        if(l == null){
            return null;
        }
        return LocalDateTime.ofEpochSecond(l,0,zone);
    }

}
