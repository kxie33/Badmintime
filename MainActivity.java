package com.example.notificationtest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.WorkerParameters;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import java.util.concurrent.TimeUnit;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    DataManager DM;
    String[] ev1 = {"2020-08-10","18:00:00","21:00:00","false",null,"UVM Badminton"};
    String[] ev2 = {"2020-08-11","19:00:00","22:00:00","false",null,"UVM Badminton"};
    String[] ev3 = {"2020-08-12","18:00:00","21:00:00","false",null,"UVM Badminton"};

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView mainView = findViewById(R.id.mainView);

        //BroadcastReceiver dm = new DataManager();
        //IntentFilter filter = new IntentFilter("com.example.notificationtest.DataManager");
        //registerReceiver(dm,filter);

        setContentView(R.layout.activity_main);

        DM = new DataManager();
        List<WorkRequest> workers = new ArrayList<>();
        Data data = new Data.Builder().putStringArray("event",ev1).build();
        WorkRequest eventWorkRequest = new OneTimeWorkRequest.Builder(ClubEventWorker.class).setInputData(data).build();
        workers.add(eventWorkRequest);
        data = new Data.Builder().putStringArray("event",ev2).build();
        eventWorkRequest = new OneTimeWorkRequest.Builder(ClubEventWorker.class).setInputData(data).setInitialDelay(1, TimeUnit.MINUTES).build();
        workers.add(eventWorkRequest);
        data = new Data.Builder().putStringArray("event",ev3).build();
        eventWorkRequest = new OneTimeWorkRequest.Builder(ClubEventWorker.class).setInputData(data).setInitialDelay(1, TimeUnit.MINUTES).build();
        workers.add(eventWorkRequest);
        WorkManager.getInstance(this).enqueue(workers);

    }



    /**
     *
     * @param v
     */
    @RequiresApi(api = Build.VERSION_CODES.O)

    public void toEventlist(View v) throws InterruptedException {
        Intent toEventList = new Intent(this, RecordedEventList.class);

        startActivity(toEventList);
    }

    public void toDeletedlist(View v) throws InterruptedException {
        Intent toDeletedlist = new Intent(this, DeletedEventList.class);
        startActivity(toDeletedlist);
    }

    /*
    ContentResolver cr = getContentResolver();
    Cursor cursor;
        if (id > 0) {
        cursor = cr.query(ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID, ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY},
                ContactsContract.RawContacts.CONTACT_ID + "= ?",
                new String[]{String.valueOf(id)},
                null);
        Log.i("Contacts","Query finished");
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY));
            nameField.setText(name);
            rawID = cursor.getInt(cursor.getColumnIndex(ContactsContract.RawContacts._ID));
            Log.i("Contacts","Found rawID="+rawID);
            break;
        }
    }
    */

    /*
    private void getEvent(){
        String[] line = null;

        String[] ev1 = {"2020-08-10","18:00:00","21:00:00","false",null,"UVM Badminton"};
        String[] ev2 = {"2020-08-11","18:00:00","21:00:00","false",null,"UVM Badminton"};
        String[] ev3 = {"2020-08-12","19:00:00","21:00:00","false",null,"UVM Badminton"};
        String[] ev4 = {"2020-08-13","19:00:00","21:00:00","false",null,"UVM Badminton"};
        String[] ev5 = {"2020-08-14","19:00:00","21:00:00","false",null,"UVM Badminton"};
        rawEventList.add(ev1);
        rawEventList.add(ev2);
        rawEventList.add(ev3);
        rawEventList.add(ev4);
        rawEventList.add(ev5);

        line = rawEventList.get(0);


        for (int i = 0; i < rawEventList.size(); i++){
            line = rawEventList.get(i);
        }
    }*/


}