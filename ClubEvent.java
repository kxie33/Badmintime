package com.example.notificationtest;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.util.Log;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import androidx.annotation.RequiresApi;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Class: ClubEvent. ClubEvent object that contains event details and interact with Android Calendar
 * Each instance is a rwo in the room database
 * Setting event flags to record or delete also takes effect to the same event inserted in Android Calendar
 * Other classes are not allowed to access ClubEvent objects except the DataManager Class
 * All interactions and changes to the ClubEvent objects must go through ContentResolver actions such as query, insert and update
 * @author Kun Xie
 */
@RequiresApi(api = Build.VERSION_CODES.O)
@Entity
public class ClubEvent extends Event {
    @PrimaryKey(autoGenerate = true)
    private int ID; // ID in room database, auto generated by the database
    @ColumnInfo
    private LocalDateTime timeStamp; // timeStamp for tracking creation time for each club event
    @ColumnInfo
    private String clubName = ""; // the club hosting this club event
    @ColumnInfo
    private LocalDate date = null; // date of this club event
    @ColumnInfo
    private LocalTime stTime= null; // starting time of this club event
    @ColumnInfo
    private LocalTime edTime= null; // ending time of this club event
    @Ignore
    private DayOfWeek dayOfWeek = null; // day of the week of this club event
    @Ignore
    private boolean overLap = false; // flag for noticing any conflict event in Android Calendar
    @Ignore
    private CalendarEvent overlapEvent; // the conflict calendar event object extracted from Android Calendar
    @Ignore
    private CalendarEvent overlapEvent2;
    @ColumnInfo
    private int overlapEventID2;
    @ColumnInfo
    private int overlapEventID; // ID of the conflict calendar event in room database
    @ColumnInfo
    private boolean notify = false; // notify flag of this club event
    @ColumnInfo
    private boolean deleted = false; // delete flag of this club event
    private static ClubEventDao dao; // the dao used by room database
    @Ignore
    private Uri clubCalUri; // the result uri after inserting this club event into Android Calendar
    @ColumnInfo
    private int clubCaleventID; // the ID associate this club event with the same club event inserted into Android Calendar
    @ColumnInfo
    private static List<String> allAttrs;
    @Ignore
    private Context ctx;
    private final static Calendar cal = Calendar.getInstance();
    private final static int offset = cal.get(Calendar.ZONE_OFFSET);
    private final static ZoneOffset zone = ZoneOffset.ofTotalSeconds(offset/1000);
    @Ignore
    private ContentResolver cr;
    public static Context DMctx;

    /**
     * getter for clubCalendarID
     * @return the clubCalendarID referencing this club event in Android Calendar
     */
    public int getClubCaleventID() {
        return clubCaleventID;
    }

    /**
     * setter for clubCaleventID
     * @param clubCaleventID the clubCalendarID referencing this club event in Android Calendar
     */
    public void setClubCaleventID(int clubCaleventID) {
        this.clubCaleventID = clubCaleventID;
    }


    /**
     * Set the timestamp this event was created.
     * @param timeStamp the time this event was created.
     */
    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * getter for clubCaluri
     * @return the result uri after inserting club event into Android Calendar
     */
    public Uri getClubCalUri() {
        return clubCalUri;
    }

    /**
     *setter for clubCalUri
     * @param clubCalUri the result uri after inserting club event into Android Calendar
     */
    public void setClubCalUri(Uri clubCalUri) {
        this.clubCalUri = clubCalUri;
    }


    /**
     * Constructor for club event object.
     * the club event is inserted into room database and Android Calendar after been created.
     * clubCalID is set after this club event get inserted into Android Calendar
     * @param attrs the string array that contains all attributes value of the club event
     * @param context application context used by ContentResolver
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public ClubEvent(String[] attrs,Context context){
        if (attrs.length != 0) {
            this.date = LocalDate.parse(attrs[0]);
            this.stTime = LocalTime.parse(attrs[1]);
            this.edTime = LocalTime.parse(attrs[2]);
            this.clubName = attrs[3];
            this.dayOfWeek = this.date.getDayOfWeek();
        }
        resetOverlap();
        this.ctx = context;
        cr = ctx.getContentResolver();
        timeStamp = LocalDateTime.now();
        setClubCaleventID(0);
        ID = (int)DataManager.getClubDao(context).insertNew(this);
        setClubCalUri(insertCal());
        setClubCaleventID(Integer.valueOf(clubCalUri.getPathSegments().get(1)));
    }

    /**
     * getter for day of week of the club event
     * @return the day of week as string
     */
    public String getDayOfWeek(){
        return dayOfWeek.name();
    }

    /**
     * empty constructor for room database
     */
    public ClubEvent(){
        cr = DMctx.getContentResolver();
    }

    /**
     * this method returns the specific club event attribute value as requested
     * @param attribute the target attribute name
     * @return the target attribute value
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public String getValue(String attribute) {

        String result = "";
        switch (attribute) {
            case "Start": 		result = this.stTime.toString(); break;
            case "End":         result = this.edTime.toString(); break;
            case "Date" :        result = this.date.toString(); break;
            case "clubName" : result = this.clubName; break;
            case "dayOfWeek" : result = this.dayOfWeek.name(); break;
        }
        return result;
    }

    /**
     *
     * @return
     */
    public static List<String> getAllAttrs(){
        return allAttrs;
    }

    /**
     * 
     * @param l
     */
    public static void setAllAttrs(List<String> l){
        for (String s: l){
            allAttrs.add(s);
        }
    }

    /**
     * the getter for club event timestamp
     * @return timestamp value as LocalDateTime
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    /**
     * to string method for print out purpose
     * @return string value of stTime, edTime, overLap and overlapEvent
     */
    public String toString() {
        String base = stTime + " " + edTime + " " + " " + notify + ";" + deleted;
        if (getOverLap()){
            base += ";" + " " + overLap + " " + overlapEventID;

        }
        return base;
    }

    /**
     * setter for club event date attribute
     * @param d date value as LocalDate
     */
    public void setDate(LocalDate d){
        date = d;
        dayOfWeek = d.getDayOfWeek();
    }

    /**
     * setter for club name attribute
     * @param name the value to be set as club name
     */
    public void setClubName(String name){
        clubName = name;
    }

    /**
     * setter for club event start time attribute
     * @param t the value to be set as event start time
     */
    public void setStTime(LocalTime t){
        stTime = t;
    }

    /**
     * setter for club event end time attribute
     * @param t the value to be set as event end time
     */
    public void setEdTime(LocalTime t){
        edTime = t;
    }

    /**
     * setter for notify flag
     * please use the other setNotify method for more complete functionality unless setting individual flag is necessary
     * @param b the boolean value for notify flag
     */
    public void setNotify(boolean b){
        notify = b;
    }

    /**
     * setter for delete flag
     * please use the other setDeleted method for more complete functionality unless setting individual flag is necessary
     * @param b the boolean value for deleted flag
     */
    public void setDeleted(boolean b){
        deleted = b;
    }

    /**
     * getter for club event date attribute
     * @return value of date attribute as LocaDate
     */
    public LocalDate getDate(){
        return this.date;
    }

    /**
     * getter for club event start time attribute
     * @return value of the start time attribute as LocalTime
     */
    public LocalTime getStTime(){
        return stTime;
    }

    /**
     * getter for club event start time attribute
     * @return value of the end time attibute as LocalTime
     */
    public LocalTime getEdTime(){
        return edTime;
    }

    /**
     * setting the club event as notify status which delete flag is false and notify flag is true
     * please use this setNotify method unless setting individual flag is necessary
     */
    public void setNotify(){
        setNotify(true);
        setDeleted(false);
        if (this.getOverLap()){
            getOverlapEvent().setNotify();
            if (getOverlapEvent2() != null){
                getOverlapEvent2().setNotify();
            }
        }
        setClubCalUri(insertCal());
    }

    /**
     * setting the club event as recorded status which delete flag is false and notify flag is false
     * please use this setRecord method unless setting individual flag is necessary
     */
    public void setRecord(){
        setNotify(false);
        setDeleted(false);
        if (this.getOverLap()){
            getOverlapEvent().setRecord();
            if (getOverlapEvent2() != null){
                getOverlapEvent2().setRecord();
            }
        }
        setClubCalUri(insertCal());
    }

    /**
     * setting the club event as deleted status which delete flag is true and notify flag is false
     * please use this setDeleted method unless setting individual flag is necessary
     */
    public void setDeleted(){
        setNotify(false);
        setDeleted(true);
        if (this.getOverLap()){
            getOverlapEvent().setDeleted();
            setOverlapEvent(null);
        }
        deleteCal();
    }

    /**
     * getter for notify flag
     * please use getFlags method to set event status
     * this method is recommended for checking notify flag value
     * @return the boolean value of notify flag
     */
    public Boolean getNotify(){
        return notify;
    }

    /**
     * getter for delete flag
     * please use getFlags method to set event status
     * this method is recommended for checking deleted flag value
     * @return the boolean value of delete flag
     */
    public Boolean getDeleted(){
        return deleted;
    }

    /**
     * this method returns the club event status after checking the flag values
     * @return result as the club event status
     */
    public String getAction(){
        String result = null;

        if (this.notify == true && this.deleted == false){
            result = "N";
        }
        else if(this.notify == false && this.deleted == false){
            result = "R";
        }
        else if(this.notify == false && this.deleted == true){
            result = "D";
        }
        else{
            result = "Error: Notify & Deleted are both true";
        }
        return result;
    }

    /**
     * getter for overLap flag which indicates a club event is overlapping with another event in Android Calendar
     * @return boolean value of the overlap flag
     */
    public Boolean getOverLap() {
        return overLap;
    }

    /**
     * setting the overlap flag to be true
     */
    public void setOverlap(){
        overLap = true;
    }

    /**
     * setting the overlap flag to be false
     */
    public void resetOverlap(){
        overLap = false;
    }

    /**
     * this method returns the event from Android Calendar which overlaps with this club event
     * @return
     */
    public CalendarEvent getOverlapEvent() {
        return overlapEvent;
    }

    public CalendarEvent getOverlapEvent2() {
        return overlapEvent2;
    }

    /**
     * setter for club event id
     * @param id the club event id
     */
    public void setID(int id){
        ID = id;
    }

    /**
     * getter for club event id
     * @return the club event id
     */
    public int getID(){
        return ID;
    }

    /**
     *getter for attribute club name
     * @return the string value of club name
     */
    public String getClubName(){
        return clubName;
    }

    /**
     * this method creates a calendar event as the overlap event of this club event
     * recording the calendarEvent ID as overlapEventID
     * and setting the overlap flag to true
     * @param e the calendar event values to be passed into the CalendarEvent constructor
     */
    public void setOverlapEvent(CalendarEvent e){
        if (e == null){
            this.setOverlapEventID(0);
            this.overlapEventID2 = 0;
            this.overlapEvent = null;
            this.overlapEvent2 = null;
            resetOverlap();
        }
        else if (overlapEvent == null) {
            this.overlapEvent = e;
            this.overlapEventID = e.getID();
            this.setOverlap();
            if (notify){
                e.setNotify();
            }
            else if(deleted){
                e.setDeleted();
            }
            else{
                e.setRecord();
            }

        }
        else {
            if (overlapEvent2 != null){
                Log.i("KunOverLap2", "Ignoring overlap2 " + this);
            }
            this.overlapEvent2 = e;
            this.overlapEventID2 = e.getID();
            if (notify){
                e.setNotify();
            }
            else if(deleted){
                e.setDeleted();
            }
            else{
                e.setRecord();
            }
        }
    }

    /**
     * getter for the overlapEventID
     * @return the overlapEventID
     */
    public int getOverlapEventID(){
        return overlapEventID;
    }

    public int getOverlapEventID2(){
        return overlapEventID2;
    }

    /**
     * this is the overlapEventID setter used by the room database
     * @param id the id value for the overlapEvent
     */
    public void setOverlapEventID(int id){
        this.overlapEventID = id; // id from the android ID
        if (id == 0){
            this.resetOverlap();
            return;
        }
        this.setOverlap();
        Uri uriCalSingle = Uri.parse("com.example.notificationtest.DataManager/CalendarEvent/" + id);
        //a context resolver to data manager to ask for calendar event of id
        if (ctx != null) {
            ContentResolver cr;
            cr = ctx.getContentResolver();
            Cursor cursor = cr.query(uriCalSingle, null, null, null);
            // calendarevent = ...
            int calID = cursor.getInt(cursor.getColumnIndex(DataManager.calID));
            LocalDateTime dateTime = LocalDateTime.parse(cursor.getString(cursor.getColumnIndex(DataManager.calTime)));
            String words = cursor.getString(cursor.getColumnIndex(DataManager.content));
            overlapEvent = new CalendarEvent(words, dateTime, calID);
        }
    }

    public void setOverlapEventID2(int id){
        this.overlapEventID2 = id; // id from the android ID
        if (id == 0){
            this.resetOverlap();
            return;
        }
        this.setOverlap();
        Uri uriCalSingle = Uri.parse("com.example.notificationtest.DataManager/CalendarEvent/" + id);
        //a context resolver to data manager to ask for calendar event of id
        if (ctx != null) {
            ContentResolver cr;
            cr = ctx.getContentResolver();
            Cursor cursor = cr.query(uriCalSingle, null, null, null);
            // calendarevent = ...
            int calID = cursor.getInt(cursor.getColumnIndex(DataManager.calID));
            LocalDateTime dateTime = LocalDateTime.parse(cursor.getString(cursor.getColumnIndex(DataManager.calTime)));
            String words = cursor.getString(cursor.getColumnIndex(DataManager.content));
            overlapEvent2 = new CalendarEvent(words, dateTime, calID);
        }
    }

    /**
     * this method inserts this club event into Android Calendar
     * self checking is handled
     * insert ID in striped from return uri and stored into ClubCaleventID
     * @return the uri after inserting into Android Calendar
     */
    private Uri insertCal() {

        TimeZone tzDefault = TimeZone.getTimeZone("GMT");
        TimeZone tz = TimeZone.getDefault();
        long offsetHrs = (tzDefault.getOffset(System.currentTimeMillis()) / (1000 * 3600)) - (tz.getOffset(System.currentTimeMillis()) / (1000 * 3600));

        String selection;
        LocalDate date = this.getDate();
        LocalTime stTime = this.getStTime();
        LocalTime edTime = this.getEdTime();
        String clubName = this.getClubName();
        Long startTime = ( (date.toEpochDay() * 24 + offsetHrs ) * 3600 * 1000) + stTime.toSecondOfDay() * 1000;
        Long endTime = ( (date.toEpochDay() * 24 + offsetHrs ) * 3600 * 1000) + edTime.toSecondOfDay() * 1000;

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startTime);
        values.put(CalendarContract.Events.DTEND, endTime);
        values.put(CalendarContract.Events.TITLE, clubName);
        values.put(CalendarContract.Events.DESCRIPTION, clubName + "  " + stTime + "  " + edTime);

        Uri uriCalendar = CalendarContract.Events.CONTENT_URI;
        uriCalendar.getEncodedQuery();
        String[] EVENT_PROJECTION = new String[] {
                CalendarContract.Events.TITLE,                           // Event Title
                CalendarContract.Events.DESCRIPTION,                  // Event Description
                CalendarContract.Events.CALENDAR_ID,         // 3
                CalendarContract.Events._ID,                 // Event_ID
                CalendarContract.Events.EVENT_LOCATION,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND
        };
        selection = "(dtstart = " + startTime + ") AND (title = ?) AND (dtend = " + endTime + ")";
        String[] selArgs = new String[]{clubName};
        Cursor calendarCursor = cr.query(uriCalendar,EVENT_PROJECTION,selection,selArgs,null);
        if (calendarCursor.moveToNext()) {
            //Request a record on an event already in the Android Calendar
            int id = calendarCursor.getInt(calendarCursor.getColumnIndex(CalendarContract.Events._ID));
            Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,id);
            setClubCaleventID(id);
            return uri;
        }

        TimeZone timeZone = TimeZone.getTimeZone("America/New_York");
        //timeZone.getOffset();
        values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
        values.put(CalendarContract.Events.CALENDAR_ID, 3);
        values.put(CalendarContract.Events.HAS_ALARM, 1);
        Log.i("KunInsertCal","About to insert " + values);
        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
        Log.i("KunInsertCal",uri.toString());
        setClubCaleventID(Integer.valueOf(uri.getPathSegments().get(1)));

        return uri;
    }

    /**
     * this method deletes the inserted club event in Android Calendar
     */
    private void deleteCal(){
        Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,Long.parseLong(String.valueOf(getClubCaleventID())));
        int u = cr.delete(deleteUri,null,null);
        setClubCalUri(null);
    }

    /**
     * this method can update the club event information in Android Calendar
     * @param values the values to be updated
     * @return the result uri after updated
     */
    private int updateCal(ContentValues values){
        int result;
        Uri updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI,Long.parseLong(String.valueOf(getClubCaleventID())));
        result = cr.update(updateUri,values,null,null);
        return result;
    }
}
