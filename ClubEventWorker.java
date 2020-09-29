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
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ClubEventWorker extends Worker {
    private List<String[]> rawEventList = new ArrayList<>();
    private NotificationManagerCompat notificationManager;
    private String title;
    private String message;
    private String big_text;
    private String CHANNEL_ID;
    private Context ctx;
    private ContentResolver cr;
    private Uri uri;


    public ClubEventWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        ctx = getApplicationContext();
        uri = Uri.parse("content://com.example.notificationtest.DataManager/ClubEvent");
        Log.i("Worker","Created Worker");
        cr = ctx.getContentResolver();
    }

    @NonNull
    @Override
    public Result doWork() {
        title = "Title";
        message = "Message";
        big_text = "";
        CHANNEL_ID = "channel1";
        createNotificationChannel();
        notificationManager = NotificationManagerCompat.from(getApplicationContext());
        //notificationManager.createNotificationChannel(channel);

        Uri result;
        ContentValues newVals = new ContentValues();
        //getEvent();
        Data data = getInputData();
        String[] evData = data.getStringArray("event");
        ClubEvent newEvent = new ClubEvent(evData);

        //Finish struct
        newVals.put("Date",evData[0]);
        newVals.put("Start_Time",evData[1]);
        newVals.put("End_Time",evData[2]);
        newVals.put("Overlap",evData[3]);
        newVals.put("CalendarEvent",evData[4]);
        newVals.put("Club_Name",evData[5]);
        result = cr.insert(uri,newVals);
        int id = Integer.valueOf(result.getPathSegments().get(2));
        Uri uriTree = new Uri.Builder().appendPath("content://com.example.notificationtest.DataManager/ClubTree")
                .appendEncodedPath( String.valueOf(id))
                .build();
        Cursor cursor = cr.query(uriTree,null,null,null,null);
        if (cursor.moveToNext()){
            if (cursor.getString(cursor.getColumnIndex(DataManager.notify)).equalsIgnoreCase("True")){
                //generate notification
                try {
                    sendNote();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return Result.success();
    }

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

    public void sendNote() throws InterruptedException {
        Intent recordIntent = new Intent(ctx, RecordedEventList.class);
        recordIntent.setAction("");
        recordIntent.putExtra("record",0);
        PendingIntent recordPendingIntent = PendingIntent.getActivity(ctx, 0, recordIntent, 0);

        Intent deleteIntent = new Intent(ctx, DeletedEventList.class);
        deleteIntent.setAction("");
        deleteIntent.putExtra("delete",0);
        PendingIntent deletePendingIntent = PendingIntent.getActivity(ctx, 0, deleteIntent, 0);

        Intent tapIntent = new Intent(ctx, MainActivity.class);
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingTapIntent = PendingIntent.getActivity(ctx, 0, tapIntent, 0);

        Notification notification_sample = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_access_alarm_24)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(big_text))
                .setContentIntent(pendingTapIntent)
                .addAction(R.drawable.ic_record, String.valueOf(R.string.record),recordPendingIntent)
                .addAction(R.drawable.ic_delete, String.valueOf(R.string.delete),deletePendingIntent)
                .build();

        notificationManager.notify(1,notification_sample);
    }


}
