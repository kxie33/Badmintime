package com.example.notificationtest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.util.Log;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.TimeZone;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * Class: ClubEventWorker. ClubEventWorker class deal with the input data from main activity and decided whether to create a new notification for the new event.
 * This class also creates the content of the notification including details on the overlap events if exists.
 *
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class ClubEventWorker extends Worker {
    public NotificationManagerCompat notificationManager;
    private String title;
    private String message;
    private String big_text;
    private String CHANNEL_ID;
    private Context ctx;
    private ContentResolver cr;
    private Uri uriClub;
    private Uri uriCalendar;
    private int notifyID;
    private DataManager DM;
    public static int nextID = 1;

    /**
     *
     * @param context
     * @param workerParams
     */
    public ClubEventWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        ctx = getApplicationContext();
        uriClub = Uri.parse("content://com.example.notificationtest.DataManager/ClubEvent/");
        uriCalendar = CalendarContract.Events.CONTENT_URI;
        cr = ctx.getContentResolver();
    }

    /**
     *
     * @return
     */
    @NonNull
    @Override
    public Result doWork() {

        //
        // Rewrite code below to scrap data access api to fetch event data
        //

        title = "Title";
        message = "Message";
        big_text = "";
        CHANNEL_ID = "channel1";
        createNotificationChannel();
        notificationManager = NotificationManagerCompat.from(getApplicationContext());

        ContentValues newVals = new ContentValues();
        //getEvent();
        Data data = getInputData();
        String[] evData = data.getStringArray("event");

        //Finish struct
        newVals.put("Date",evData[0]);
        newVals.put("Start_Time",evData[1]);
        newVals.put("End_Time",evData[2]);
        newVals.put("Club_Name",evData[3]);

        //
        // Keep this code
        //

        processNewClubEvent(newVals);
        return Result.success();
    }

    /**
     * process a newly discovered club event
     * @param newVals includes definitions for Data, Start_Time, End_Time and Club_Name all as Strings
     */
    private void processNewClubEvent(ContentValues newVals) {
        Uri resultClub;
        resultClub = cr.insert(uriClub,newVals);

        int id = Integer.valueOf(resultClub.getPathSegments().get(1));
        Uri uriTree = Uri.parse("content://com.example.notificationtest.DataManager/ClubTree")
                .buildUpon().appendEncodedPath( String.valueOf(id))
                .build();

        Uri uriID = Uri.parse("content://com.example.notificationtest.DataManager/ClubEvent")
                .buildUpon().appendEncodedPath( String.valueOf(id))
                .build();

        Uri uriCalTree = Uri.parse("content://com.example.notificationtest.DataManager/CalendarTree")
                .buildUpon().appendEncodedPath( String.valueOf(id))
                .build();

        Cursor cursor = cr.query(uriTree,null,null,null,null);
        if (cursor.moveToNext()){
            int eventID  = cursor.getInt(cursor.getColumnIndex(DataManager.ID));
            if (cursor.getString(cursor.getColumnIndex(DataManager.notify)).equalsIgnoreCase("True")){
                //generate notification
                try {
                    Cursor eventCursor = cr.query(uriID,null,null,null,null);
                    if (eventCursor.moveToNext()) {
                        title = eventCursor.getString(eventCursor.getColumnIndex(DataManager.clubName));
                        message = eventCursor.getString(eventCursor.getColumnIndex(DataManager.date)) + " " +
                                eventCursor.getString(eventCursor.getColumnIndex(DataManager.startTime)) + " " +
                                eventCursor.getString(eventCursor.getColumnIndex(DataManager.endTime)) + " ";
                        big_text = ""; // Overlap event if exists
                        // if ( if overlapping event )
                        // append "CONFLICT" to message
                        // format data from conflict into big_text
                        if (eventCursor.getString(eventCursor.getColumnIndex(DataManager.overlap)) .equalsIgnoreCase("true")){
                            String oldMessage = message;
                            message = message + " CONFLICT DETECTED";
                            String Calid = eventCursor.getString(eventCursor.getColumnIndex(DataManager.calID));
                            Uri calUri = Uri.parse("content://com.example.notificationtest.DataManager/CalendarEvent/")
                                    .buildUpon().appendEncodedPath(Calid)
                                    .build();
                            Cursor calCursor = cr.query(calUri,null,null,null);
                            if (calCursor.moveToNext()){
                                String start = calCursor.getString(calCursor.getColumnIndex(DataManager.calTime));
                                big_text = start;
                                String words = calCursor.getString(calCursor.getColumnIndex(DataManager.content));
                                big_text += " " + words;
                            }
                            big_text = oldMessage + " conflict with " + big_text;
                        }
                        notifyID = nextID;
                        nextID++;
                        sendNote(eventID);
                        ContentValues values = new ContentValues();
                        values.put(DataManager.notify,true);
                        cr.update(uriID,values,null,null);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else if (cursor.getString(cursor.getColumnIndex(DataManager.deleted)).equalsIgnoreCase("True")){
                //delete the event
                ContentValues vals = new ContentValues();
                vals.put(DataManager.deleted,true);
                vals.put(DataManager.notify,false);
                Uri uriSingle = Uri.parse("content://com.example.notificationtest.DataManager/ClubEvent/" + id);
                cr.update(uriSingle,vals,null,null);
            }
            else{
                //record the event
                ContentValues vals = new ContentValues();
                vals.put(DataManager.deleted,false);
                vals.put(DataManager.notify,false);
                Uri uriSingle = Uri.parse("content://com.example.notificationtest.DataManager/ClubEvent/" + id);
                cr.update(uriSingle,vals,null,null);
            }
        }
        else{
            Log.i("QueryFailed","Event not found");
        }
    }


    /**
     * This method setup the notification channel for creating notifications
     */
    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = String.valueOf(R.string.channel_name);
            String description = String.valueOf(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * This method creates a notification with three intents packed inside.
     * The recordIntent is triggered by the record button on the notification and sends the intent with extra data into the recorded event list class
     * The deleteIntent is triggered by the delete button on the notification and sends the intent with extra data into the deleted event list class
     * The tapIntent is triggered by tapping the notification. The intent will dismiss the notification.
     * @param eventID the eventID of the event referenced by this notification
     * @throws InterruptedException the interrupted exception
     */
    private void sendNote(int eventID) throws InterruptedException {
        Intent recordIntent = new Intent(ctx, RecordedEventList.class);
        recordIntent.putExtra("noteID",notifyID);
        recordIntent.putExtra("id", eventID);
        recordIntent.putExtra("action", "record");
        PendingIntent recordPendingIntent = PendingIntent.getActivity(ctx, eventID * 4 + 2, recordIntent, Intent.FILL_IN_DATA);
        Log.i("recordIntent", String.valueOf(recordIntent) + " " + recordIntent.getExtras());

        Intent deleteIntent = new Intent(ctx, DeletedEventList.class);
        deleteIntent.putExtra("noteID",notifyID);
        deleteIntent.putExtra("id", eventID);
        deleteIntent.putExtra("action", "delete");
        PendingIntent deletePendingIntent = PendingIntent.getActivity(ctx, eventID * 4 + 1, deleteIntent, Intent.FILL_IN_DATA);

        Intent tapIntent = new Intent(ctx, MainActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingTapIntent = PendingIntent.getActivity(ctx, eventID * 4, tapIntent, 0);

        Notification notification = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_access_alarm_24)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(big_text))
                .setContentIntent(pendingTapIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_record, "Record Event", recordPendingIntent)
                .addAction(R.drawable.ic_delete, "Delete Event", deletePendingIntent)
                .build();

        notificationManager.notify(notifyID, notification);
    }

}

