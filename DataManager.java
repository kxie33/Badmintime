package com.example.notificationtest;

import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Build;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class DataManager extends ContentProvider {
    public static final String ID = "id";
    public static final String clubName = "clubName";
    public static final String date = "date";
    public static final String startTime = "startTime";
    public static final String endTime = "endTime";
    public static final String notify = "notify";
    public static final String deleted = "deleted";
    private ArrayList<ClubEvent> AllEventList = new ArrayList<>();
    private ArrayList<ClubEvent> RecordEventList = new ArrayList<>();
    private ArrayList<ClubEvent> DeletedEventList = new ArrayList<>();
    private TreeDecisionControl tree;
    private TreeNode root;
    private static final int uriAll = 1;
    private static final int uriSingle = 2;
    private static final int uriClubTree = 3;
    private static final UriMatcher uriMatcher;
    static SQLiteDatabase db;

    static final String MAIN_TABLE = "Club_Events";
    static final int DB_VERSION = 1;
    static final String DATABASE_NAME = "Event_DB";
    static final String CREATE_DB_CLUB_EVENT = "CREATE TABLE " + MAIN_TABLE +
            " (event_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " start_time TEXT NOT NULL, " +
            " end_time TEXT NOT NULL, " +
            " day TEXT NOT NULL, " +
            " calendar_event INTEGER , " +
            " club_name TEXT, " +
            " notified Boolean NOT NULL, " +
            " deleted Boolean NOT NULL);";

    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("com.example.notificationtest", "ClubEvent", uriAll);
        uriMatcher.addURI("com.example.notificationtest", "ClubEvent/#", uriSingle);
        uriMatcher.addURI("com.example.notificationtest","ClubTree/#",uriClubTree);
    }


    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        int uriType = uriMatcher.match(uri);
        int id = -1;
        switch (uriType){
            case uriClubTree: {
                MatrixCursor mxCursor = new MatrixCursor(new String[]{ID, notify, deleted},
                        50);
                id = Integer.valueOf(uri.getPathSegments().get(2));
                ClubEvent event = null;
                for (ClubEvent e : AllEventList) {
                    if (e.getID() == id){break;}
                }
                if (event != null) {
                    MatrixCursor.RowBuilder builder = mxCursor.newRow();
                    String action = tree.process(event,root,AllEventList);
                    builder.add(event.getID());
                    builder.add(action.equals("N"));
                    builder.add(!action.equals("D"));
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
                id = Integer.valueOf(uri.getPathSegments().get(2));
                break;
            }
        }

        MatrixCursor mxCursor = new MatrixCursor(new String[]{ID, clubName, date, startTime, endTime, notify, deleted},
                50);
        for (ClubEvent event : AllEventList ) {
            if (id >= 0 && event.getID() != id){
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
        }

        return mxCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        ClubEvent event = new ClubEvent(new String[]{contentValues.getAsString("Date"),
                contentValues.getAsString("Start_Time"),
                contentValues.getAsString("End_Time"),
                contentValues.getAsString("Overlap"),
                contentValues.getAsString("CalendarEvent"),
                contentValues.getAsString("Club_Name")});
        AllEventList.add(event);

        Uri newUri = new Uri.Builder().appendPath("content://com.example.notificationtest.DataManager/ClubEvent")
                .appendEncodedPath( String.valueOf(event.getID()) )
                .build();
        return newUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        //To grab actuall ID from uri matcher
        int uriType = uriMatcher.match(uri);
        int id = -1;
        switch (uriType){
            case uriAll:
                return -1;
            case uriSingle:
                id = Integer.valueOf(uri.getPathSegments().get(2));
                break;
        }
        
        for (ClubEvent event: AllEventList){
            if (event.getID() != id) {
                continue;
            }

            if ( contentValues.containsKey("deleted") ){
                if (contentValues.getAsBoolean("deleted")){
                    RecordEventList.remove(event);
                    DeletedEventList.add(event);
                    event.setDeleted();
                }
                else{
                    DeletedEventList.remove(event);
                    RecordEventList.add(event);
                    event.setRecord();
                }
            }

            //Check for notified change
            if ( contentValues.containsKey("recorded") ){
                if (contentValues.getAsBoolean("recorded")){
                    event.setRecord();
                }
                else{
                    event.setNotify();
                }
            }

            return 1;

        }
        return 0;
    }

}
