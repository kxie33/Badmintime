package com.example.notificationtest;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.util.Log;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.room.Room;


/**
 * Class: DataManager. This class setup and interact with local room database to store and manipulate data on user's device locally.
 * This class also handles query, insert, update and delete requests from all the other classes which try to access events record
 * in the local room database. The decision tree is also setup and trained in this class. Both trees are trained and retrained in the update method.
 * Other classes can access data in DataManager through ContentResolver since DataManager is setup as a ContentProvider
 * @author Kun Xie
 */
public class DataManager extends ContentProvider {

    public static final String ID = "_id"; //club event ID
    public static final String calID = "calID"; // Calendar event ID that conflict with this club event
    public static final String clubName = "clubName"; // club name of this club event
    public static final String date = "date"; // event date of this club event
    public static final String startTime = "startTime"; // start time of this club event
    public static final String endTime = "endTime"; // end time of this club event
    public static final String notify = "notify"; // notify flag of this club event
    public static final String deleted = "deleted"; // delete flag of this club event
    public static final String content = "content"; // content string of the conflict calendar event
    public static final String calTime = "calTime"; // time of the conflicted calendar event
    public static final String overlap = "overlap";
    private List<ClubEvent> AllEventList; // the club event list that contains all club events despite the event state including the deleted club events
    private List<CalendarEvent> CalendarEventList; // calendar event list that contains all calendar events which are detected conflicting with some club event
    private List<ClubEvent> RecordEventList; // the club event list that contains all club events with state recorded
    private List<ClubEvent> DeletedEventList; // the club event list that contains all club events with state deleted
    private TreeDecisionControl treeClub; // the decision tree builds upon attributes from club events to determine new club events state
    private TreeDecisionControl treeCalendar; // the decision tree builds upon words from calendar events to determine new club events state that conflict with calendar events
    private TreeNode rootClub; // root node of the club event decision tree
    private TreeNode rootCalendar; // root node of the calendar event decision tree
    private static final int uriAll = 1; // uri to access all club events
    private static final int uriSingle = 2; // uri to access a single club event
    private static final int uriClubTree = 3; // uri to access club event state processed by the decision tree
    private static final int uriCalAll = 4; // uri to access all calendar events
    private static final int uriCalSingle = 5; // uri to access a single calendar event
    private static final int uriCalTree = 6; // uri to access club event state that conflict with calendar event which is process by calendar decision tree
    private static final int uriClubTreeAll = 7;
    private static final int uriCalTreeAll = 8;
    private static final int uriActive = 9;
    private static final UriMatcher uriMatcher; // uri matcher to match different uris
    private static ClubEventDao dao; // dao declared to use room database
    private static TimeZone tzDefault;
    private long offsetHrs = 0;
    private TimeZone tz;

    /**
     * this is the uri matcher matching uri names to actual uris
     */
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("com.example.notificationtest.DataManager", "ClubEvent", uriAll);
        uriMatcher.addURI("com.example.notificationtest.DataManager", "ClubEvent/#", uriSingle);
        uriMatcher.addURI("com.example.notificationtest.DataManager","ClubTree",uriClubTreeAll);
        uriMatcher.addURI("com.example.notificationtest.DataManager","ClubTree/#",uriClubTree);
        uriMatcher.addURI("com.example.notificationtest.DataManager","CalendarEvent",uriCalAll);
        uriMatcher.addURI("com.example.notificationtest.DataManager","CalendarEvent/#",uriCalSingle);
        uriMatcher.addURI("com.example.notificationtest.DataManager","CalendarTree",uriCalTreeAll);
        uriMatcher.addURI("com.example.notificationtest.DataManager","CalendarTree/#",uriCalTree);
        uriMatcher.addURI("com.example.notificationtest.DataManager","ClubTreeActive",uriActive);
    }

    /**
     * this is the onCreate method of DataManager
     * All event lists are initialized here
     * All tree roots are initialized here with default action notify and default entropy 10
     * A thread if created to run the start.() method which load events lists data into room database
     * @return boolean result of onCreate
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onCreate() {
        ClubEvent.DMctx = this.getContext();
        CalendarEventList = new ArrayList<>();
        AllEventList = new ArrayList<>();
        RecordEventList = Collections.synchronizedList(new ArrayList<>());
        DeletedEventList = new ArrayList<>();
        tzDefault = TimeZone.getTimeZone("GMT");
        tz = TimeZone.getDefault();
        offsetHrs = (tzDefault.getOffset(System.currentTimeMillis()) / (1000 * 3600)) - (tz.getOffset(System.currentTimeMillis()) / (1000 * 3600));
        initTrees();
        new Thread(()->loadData()).start();
        return true;
    }

    /**
     *
     */
    private void initTrees(){
        rootClub = new ActionNode("N",10);
        rootCalendar = new ActionNode("N",10);
    }

    /**
     * this is the method that get run in onCreate by a new thread to load event list data into room database
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadData() {
        getClubDao(this.getContext());
        for (ClubEvent e: dao.getAll()){
            AllEventList.add(e);
            if (e.getDeleted()){
                DeletedEventList.add(e);
            }
            else{
                RecordEventList.add(e);
            }
        }
    }

    /**
     * this is the method that returns dao used by room database
     * @param context application context used to create dao
     * @return the dao object
     */
    static ClubEventDao getClubDao(Context context) {
        if (dao == null) {
            EventDataBase db = Room.databaseBuilder(context,
                    EventDataBase.class, "EventDataBase")
                    .fallbackToDestructiveMigration()
                    .build();
            dao = db.clubEventDao();
        }
        return dao;
    }

    /**
     * this is the query method which handles query from other classes who try to access data through ContentResolver
     * a query to the tree uri will add the club event to appropriate list in DataManager and set the notify and delete flags according to the event state
     * uriTree returns a cursor with club event id, notify flag and deleted flag
     * uriAll returns a cursor with all club events that matches the selection string condition such as "deleted = false"
     * uriSingle returns a cursor with a single club event that matches the attached id
     * @param uri the input uri differentiating the type of data gets returned
     * @param projection projection of the query
     * @param selection selection string of the query
     * @param selectionArgs selection arguments of the query
     * @param sortOrder sort order of the query
     * @return a matrix cursor contains the result of the query
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        boolean includeDeleted = true;
        boolean includeRecorded = true;
        LocalDate minDate = null;
        LocalDate maxDate = null;

        int uriType = uriMatcher.match(uri);
        int id = -1;
        switch (uriType){
            case uriClubTree: {
                MatrixCursor mxCursor = new MatrixCursor(new String[]{ID, notify, deleted},
                        50);
                id = Integer.valueOf(uri.getPathSegments().get(1));
                ClubEvent event = findClubEvent(id);

                if (event != null) {
                    MatrixCursor.RowBuilder builder = mxCursor.newRow();
                    String action = processTree(event);
                    builder.add(event.getID());
                    builder.add(action.equals("N"));
                    builder.add(action.equals("D"));
                    //add to appropriate list
                    switch (action) {
                        case "N":
                            event.setNotify();
                            RecordEventList.add(event);
                            break;
                        case "R":
                            event.setRecord();
                            RecordEventList.add(event);
                            break;
                        case "D":
                            event.setDeleted();
                            DeletedEventList.add(event);
                            break;
                    }
                }
                return mxCursor;
            }
            case uriSingle: {
                id = Integer.valueOf(uri.getPathSegments().get(1));
                break;
            }
            case uriAll:{
                if (selection != null){
                    String[] words = selection.split("\\s");
                    switch (words[0].toLowerCase()){
                        case deleted:
                            if (words[2] .equalsIgnoreCase("false")){
                                includeDeleted = false;
                            }
                            else{
                                includeRecorded = false;
                                includeDeleted = true;
                            }
                            break;
                        case "mindate":
                            if(words[0] .equalsIgnoreCase("mindate")){
                                minDate = LocalDate.parse(words[2]);
                            }
                            if(words[4] .equalsIgnoreCase("maxdate")){
                                maxDate = LocalDate.parse(words[6]);
                            }
                            break;
                    }
                }
                break;
            }
            case uriActive:{
                Log.i("KunActive", selection);
                includeDeleted = false;
                if (selection != null){
                    String[] words = selection.split("\\s");
                    switch (words[0].toLowerCase()){
                        case "mindate":
                            if(words[0] .equalsIgnoreCase("mindate")){
                                minDate = LocalDate.parse(words[2]);
                            }
                            if(words[4] .equalsIgnoreCase("maxdate")){
                                maxDate = LocalDate.parse(words[6]);
                            }
                            break;
                    }
                }
                break;
            }
            case uriCalSingle:
                id = Integer.valueOf(uri.getPathSegments().get(1));
                for (CalendarEvent e: CalendarEventList){
                    if (e.getID() == id ){
                        return buildCursor(e);
                    }
                }
                CalendarEvent e = fetchCalendarEvent(id);
                return buildCursor(e);
            default: Log.i("KunQuery",uriType + " " + uri);
            return null;
            case uriClubTreeAll:
                rootClub.toLog("KunClubTree");
                return null;
            case uriCalTreeAll:
                rootCalendar.toLog("KunCalTree");
                return null;
        }

        MatrixCursor mxCursor = new MatrixCursor(new String[]{ID, clubName, date, startTime, endTime, notify, deleted, overlap, calID},
                50);
        for (ClubEvent event : AllEventList ) {
            if (id >= 0 && event.getID() != id){
                continue;
            }
            if (!includeDeleted && event.getDeleted()){
                continue;
            }
            if (!includeRecorded && !event.getDeleted()){
                continue;
            }
            if ( minDate != null && event.getDate() .isBefore(minDate)){
                continue;
            }
            if ( maxDate != null && event.getDate() .isAfter(maxDate)){
                continue;
            }
            MatrixCursor.RowBuilder builder = mxCursor.newRow();
            builder.add(event.getID());
            builder.add(event.getClubName());
            builder.add(event.getDate());
            builder.add(event.getStTime());
            builder.add(event.getEdTime());
            builder.add(event.getNotify());
            builder.add(event.getDeleted());
            builder.add(event.getOverLap());
            builder.add(event.getOverlapEventID());
        }
        return mxCursor;
    }

    /**
     * this method creates a cursor from an existing calendar event object
     * @param e the input calendar event
     * @return the result matrix cursor
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private Cursor buildCursor(CalendarEvent e){
        //build cursor from calendar event
        MatrixCursor mxCursor = new MatrixCursor(new String[]{calID, content, calTime},
                50);
        String eventContent = "";
        for (String a: e.getWords()){
            eventContent += a + " ";
        }
        MatrixCursor.RowBuilder builder = mxCursor.newRow();
        builder.add(e.getID());
        builder.add(eventContent); // get words before build and concatnate the words and build
        builder.add(e.getTimeStamp().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM,FormatStyle.SHORT))); // put formatting in time stamp

        return mxCursor;
    }

    /**
     * this method responds to the content resolver insert call
     * please use uriAll for all inserts
     * @param uri the uri for insert path
     * @param contentValues the values of the club event that gets inserted
     * @return uri of the club event if successfully inserted
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        int uriType = uriMatcher.match(uri);
        switch (uriType) {
            case uriAll:
                return insertClub(contentValues);
            case uriCalAll:
                return null;
        }

        return null;
    }

    /**
     * this method takes the input content values and create a new club event
     * the new club event is created after content values are iterated and compared to events in AllEventList and no match is found
     * after creating the new club event, a query is sent to Android Calendar searching for overLap calendar events
     * if the overLap event matches the club event's own calendar ID, overlap won't be set
     * if the overlap event matches the existing calendar event in the club event, overlap won't be set
     * because of this handling, for multiple overlapping Android Calendar events, only the first event will be scanned and used
     * @param contentValues the input values to create new club event
     * @return the result uri of the newly created club event
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private Uri insertClub(@Nullable ContentValues contentValues) {
        long eventStart;
        long eventEnd;
        String selection;
        ClubEvent event = null;

        //Search for the same event inside the AllEventList
        for (ClubEvent e : AllEventList){
            //if have a match, set event to e and break;
            if (e.getDate() .equals(contentValues.getAsString("Date"))
            && String.valueOf(e.getStTime()) .equalsIgnoreCase(contentValues.getAsString("Start_Time"))
            && String.valueOf(e.getStTime()) .equalsIgnoreCase(contentValues.getAsString("End_Time"))
            && e.getClubName() .equalsIgnoreCase(contentValues.getAsString("Club_Name"))){
                event = e;
                break;
            }
        }

        if (event == null) {
            ContentResolver cr = getContext().getContentResolver();
            event = new ClubEvent(new String[]{contentValues.getAsString("Date"),
                    contentValues.getAsString("Start_Time"),
                    contentValues.getAsString("End_Time"),
                    contentValues.getAsString("Club_Name")}, this.getContext());
            AllEventList.add(event);
            // scan for calendar events
            Uri uriCalendar = CalendarContract.Events.CONTENT_URI;
            uriCalendar.getEncodedQuery();
            String[] EVENT_PROJECTION = new String[]{
                    CalendarContract.Events.TITLE,                           // Event Title
                    CalendarContract.Events.DESCRIPTION,                  // Event Description
                    CalendarContract.Events.CALENDAR_ID,         // 2
                    CalendarContract.Events._ID,                 // Event_ID
                    CalendarContract.Events.EVENT_LOCATION,
                    CalendarContract.Events.DTSTART,
                    CalendarContract.Events.DTEND
            };
            eventStart = stEpoch(event);
            eventEnd = edEpoch(event);
            selection = "((dtstart <= " + eventEnd + ") AND (dtend >= " + eventStart + "))";
            Cursor calendarCursor = cr.query(uriCalendar, EVENT_PROJECTION, selection, null, null);
            //selection for searching time conflicted calendar events.
            while (calendarCursor.moveToNext()) {
                int ID = calendarCursor.getInt(calendarCursor.getColumnIndex(CalendarContract.Events._ID));
                if (ID == event.getClubCaleventID()) {
                    continue;
                }
                else if (ID == event.getOverlapEventID()){
                    continue;
                }
                String cTitle = calendarCursor.getString(calendarCursor.getColumnIndex(CalendarContract.Events.TITLE));
                String cDescription = calendarCursor.getString(calendarCursor.getColumnIndex(CalendarContract.Events.DESCRIPTION));
                String cLocation = calendarCursor.getString(calendarCursor.getColumnIndex(CalendarContract.Events.EVENT_LOCATION));
                String calendarEventName = cTitle + cDescription + cLocation;
                Calendar cal = Calendar.getInstance();
                int offset = cal.get(Calendar.ZONE_OFFSET);
                ZoneOffset zone = ZoneOffset.ofTotalSeconds(offset/1000);
                long startTime = calendarCursor.getLong(calendarCursor.getColumnIndex(CalendarContract.Events.DTSTART));
                LocalDateTime start = LocalDateTime.ofEpochSecond(startTime/1000, 0, zone);
                Log.i("KunTime2","Inserting CalendarEvent " + start + " " + startTime);
                createCalendarEvent(event, ID, calendarEventName, start);
                event.setOverlap();
            }
        }

        Uri newUri = Uri.parse("content://com.example.notificationtest.DataManager/ClubEvent")
                .buildUpon().appendEncodedPath( String.valueOf(event.getID()) )
                .build();
        return newUri;
    }

    /**
     * this method creates a new CalendarEvent object and attach the object with the overlapped club event
     * then add the created calendar event into CalendarEventList
     * @param event the overlapped club event
     * @param ID calendar event ID, same as the ID taken from Android Calendar for referencing
     * @param calendarEventName calendar event name, the Title in Android Calendar
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createCalendarEvent(ClubEvent event, int ID, String calendarEventName, LocalDateTime start) {
        CalendarEvent calendarEvent = new CalendarEvent(calendarEventName, start, ID);
        if (!CalendarEventList.contains(calendarEvent)){
            CalendarEventList.add(calendarEvent);
            event.setOverlapEvent(calendarEvent);
        }
        else{

        }
    }

    /**
     * this method fetches the target calendar event from Android Calendar according to input id
     * @param id target calendar event id
     * @return the result calendar event object from Android Calendar
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public CalendarEvent fetchCalendarEvent(int id){
        //return the calendar event if already exist in CalendarEventList
        for (CalendarEvent e: CalendarEventList){
            if (e.getID() == id){
                return e;
            }
        }
        //fetch data from android calendar event
        ContentResolver cr = getContext().getContentResolver();
        Uri uriCalendar = CalendarContract.Events.CONTENT_URI
                .buildUpon().appendEncodedPath( String.valueOf(id) )
                .build();
        uriCalendar.getEncodedQuery();
        String[] EVENT_PROJECTION = new String[] {
                CalendarContract.Events.TITLE,                           // Event Title
                CalendarContract.Events.DESCRIPTION,                  // Event Description
                CalendarContract.Events.CALENDAR_ID,         // 2
                CalendarContract.Events._ID,                 // Event_ID
                CalendarContract.Events.EVENT_LOCATION,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND
        };

        Cursor calendarCursor = cr.query(uriCalendar,EVENT_PROJECTION,CalendarContract.Events._ID + "=" + id,null,null);
        if(calendarCursor.moveToNext()) {
            String cTitle = calendarCursor.getString(calendarCursor.getColumnIndex(CalendarContract.Events.TITLE));
            String cDescription = calendarCursor.getString(calendarCursor.getColumnIndex(CalendarContract.Events.DESCRIPTION));
            String cLocation = calendarCursor.getString(calendarCursor.getColumnIndex(CalendarContract.Events.EVENT_LOCATION));
            String calendarEventname = cTitle + cDescription + cLocation;
            Calendar cal = Calendar.getInstance();
            //introduce method get Android Date Time
            int offset = cal.get(Calendar.ZONE_OFFSET);
            ZoneOffset zone = ZoneOffset.ofTotalSeconds(offset/1000);
            LocalDateTime startTime = LocalDateTime.ofEpochSecond(calendarCursor.getLong(calendarCursor.getColumnIndex(CalendarContract.Events.DTSTART))/1000,0,zone);
            CalendarEvent calendarEvent = new CalendarEvent(calendarEventname, startTime, id);
            CalendarEventList.add(calendarEvent);
            return calendarEvent;
        }
        return null;
    }


    /**
     * this method converts the date and start time of the club event into epoch time for further use
     * @param e the club event
     * @return result date and start time in epoch time of milliseconds
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private long stEpoch(ClubEvent e){
        long timeDate = (e.getDate().toEpochDay() * 24 + offsetHrs)  * 3600 * 1000;
        long stTime = e.getStTime().toSecondOfDay() * 1000;
        long result = timeDate + stTime;
        return result;
    }

    /**
     *this method converts the date and end time of the club event into epoch time for further use
     * @param e the club event
     * @return result date and end time in epoch time of milliseconds
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private long edEpoch(ClubEvent e){
        long timeDate = (e.getDate().toEpochDay() * 24 + offsetHrs) * 3600 * 1000;
        long edTime = e.getEdTime().toSecondOfDay() * 1000;
        long result = timeDate + edTime;
        return result;
    }

    /**
     * this method responds calls to content resolver delete
     * removes all events in room database and clear all event lists
     * please use this method for testing and reset only
     * all user data are supposed to be kept in the event lists at all time
     * user's delete action only set the target event state to delete and move the event into deletedEventList for learning user behavior
     * user's delete actions are handled by method update
     * @param uri the uri matching event to be deleted, use uriAll for debugging clean up
     * @param s selection string for deleting
     * @param strings selection arguments for deleting
     * @return the result value
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        int uriType = uriMatcher.match(uri);
        int id = -1;
        switch (uriType){
            case uriAll:
                new Thread (()->executeDelete()).start();
                return 1;
            case uriSingle:
                return -1;
        }
        return 0;
    }

    /**
     * this method clean up all data in all event lists and room data base
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void executeDelete() {
        getClubDao(this.getContext());
        for (ClubEvent e: dao.getAll()){
            dao.delete(e);
        }
        for (ClubEvent e: AllEventList){
            e.setDeleted();
        }
        AllEventList.clear();
        RecordEventList.clear();
        DeletedEventList.clear();
        CalendarEventList.clear();
        initTrees();
    }

    /**
     * this method responds to content resolver update to change state of an event
     * an event can be in state notify, recorded or deleted
     * user's notify action sets the club event to notify
     * notify and recorded events are in the recorded event list
     * user's delete action moves the club event into deleted event list and sets event state to delete
     * when the user's action is been processed, the tree is created and retrained considering the new user action
     * @param uri the uri of the club event needs to be updated
     * @param contentValues the new values to update the club event
     * @param s selection string condition to match certain events
     * @param strings selection arguments to match events
     * @return the update result value
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        //To grab actuall ID from uri matcher
        int uriType = uriMatcher.match(uri);
        int id = -1;
        ClubEvent event = null;
        switch (uriType){
            case uriAll:
                return 0;
            case uriSingle:
                id = Integer.valueOf(uri.getPathSegments().get(1));
                event = findClubEvent(id);
                processUpdate(contentValues, event);
                return 1;
        }
        return 0;
    }

    /**
     * this method handles the change of values in the target club event and retrain the decision trees every time when an update occurs
     * @param contentValues the values to update the target event
     * @param event the event to be updated
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void processUpdate(@Nullable ContentValues contentValues, ClubEvent event) {
        Log.i("KunUpdate", contentValues + " " + event.getID());
        if ( contentValues.containsKey(notify) ){
            if (contentValues.getAsBoolean(notify)){
                event.setNotify();
            }
            else{
                event.setRecord();
            }
        }
        if( contentValues.containsKey(deleted) ){
            if (contentValues.getAsBoolean(deleted)){
                event.setDeleted();
                if(!DeletedEventList.contains(event)){
                    DeletedEventList.add(event);
                }
                RecordEventList.remove(event);
            }
            else{
                event.setRecord();
                DeletedEventList.remove(event);
                RecordEventList.add(event);
            }
        }
        trainTrees(event);
        rootClub.toLog("KunTree");
        new Thread(new SaveInstance(dao,event)).start();
    }

    /**
     * this method finds a club event according to the passed in ID
     * @param id the id value
     * @return the club event matching the input id, null if not found
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private ClubEvent findClubEvent(int id){
        for (ClubEvent e: AllEventList) {
            if (e.getID() != id) {
                continue;
            }
            return e;
        }
        return null;
    }

    /**
     * This method gets the result action of the input club event from the club event tree.
     * If overlap events exist, the result action will be produced for each overlap event.
     * The action with lower entropy between the overlap events will be set as the final result from the calendar event tree.
     * The calendar event tree result will be compared to the club event tree result. The safer action that has the lower entropy will be the final result.
     * @param event the club event
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String processTree(ClubEvent event) {
        ActionNode club = treeClub.process(event, rootClub);
        ActionNode calendar = null;
        String action = null;
        if(event.getOverlapEventID() != 0){
            Log.i("KunDM","Found calendar event" + event.getOverlapEventID());
            Log.i("KunDM2","Calendar event flags" + event.getOverlapEvent().getFlags());
            calendar = treeCalendar.processCal(fetchCalendarEvent(event.getOverlapEventID()), rootCalendar);
            // if event.getOverlapEventID2 != 0
            if (event.getOverlapEventID2() != 0){
                ActionNode calendar1 = treeCalendar.processCal(fetchCalendarEvent(event.getOverlapEventID()), rootCalendar);
                ActionNode calendar2 = treeCalendar.processCal(fetchCalendarEvent(event.getOverlapEventID2()), rootCalendar);

                if (calendar1.getAction() .equals(calendar2.getAction()) ){
                    calendar = calendar1;
                }
                else if (calendar1.getEntropy() > calendar2.getEntropy()){
                    calendar = calendar2;
                }
                else if (calendar1.getEntropy() < calendar2.getEntropy()){
                    calendar = calendar1;
                }
                else if (calendar1.getEntropy() == calendar2.getEntropy()){
                    String c1 = calendar1.getAction();
                    String c2 = calendar2.getAction();
                    if (c1 .equals("N") || c2.equals("N")){
                        action = "N";
                    }
                    else{
                        action = "R";
                    }
                }
            }
            Log.i("KunDM"," Club = " + club.getAction() + " @" + club.getEntropy());
            Log.i("KunDM"," Calendar = " + calendar.getAction() + " @" + calendar.getEntropy());
            if (club.getAction() .equals(calendar.getAction())){
                action = club.getAction();
            }
            else if (club.getEntropy() > calendar.getEntropy()){
                action = calendar.getAction();
            }
            else if(club.getEntropy() < calendar.getEntropy()){
                action = club.getAction();
            }
            else if(club.getEntropy() == calendar.getEntropy()){
                String e = club.getAction();
                String c = calendar.getAction();
                if (e .equals("N") || c.equals("N")){
                    action = "N";
                }
                else{
                    action = "R";
                }
            }
        }
        else{
            action = club.getAction();
        }
        return action;
    }

    /**
     * This method saves the current club events into room database
     */
    private static final class SaveInstance implements Runnable{
        private ClubEventDao dao;
        private ClubEvent event;
        public SaveInstance(ClubEventDao d, ClubEvent e ){
            dao = d;
            event = e;
        }
        @Override
        public void run() {
            dao.update(event);
        }
    }

    /**
     * this method retrains the decision trees
     * the club event tree is retrained every time an update occurs
     * the calendar event tree is retrained when an update occurs and the club event has an overlap event
     * @param e the club event
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void trainTrees(ClubEvent e) {
        rootClub = TreeDecisionControl.learnTree(AllEventList);
        if (e.getOverLap()) {
            Log.i("KunTrainTree",e.toString());
            Log.i("KunTrainTree",String.valueOf(AllEventList));
            Log.i("KunTrainTree",String.valueOf(CalendarEventList));
            rootCalendar = TreeDecisionControl.learnTreeCal(CalendarEventList);
        }
    }

    /**
     * this method is used by the content provider and content resolver to get type by the uri
     * @param uri the target uri
     * @return type string
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

}
