/**
 * Copyright notice: all rights reserved to Kun Xie.
 */
package com.example.notificationtest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Class: MainActivity. This is the home page class of the Badmintime mobile app. This class stores the input data for club events right now
 * for the lack of front end design of the app. In the first run, this class will ask user for read and write permission of the Android Calendar.
 * User can view incoming club events in the next two weeks on the main display. Two buttons on the bottom can head to other activity pages
 * including Club Events List and Recycle Bin.
 */
public class MainActivity extends AppCompatActivity {
    DataManager DM;
    String[][] events1 = {{"2021-02-02","18:00:00","21:00:00","UVM Badminton"}, //Tues
                         {"2021-02-03","18:00:00","21:00:00","UVM Badminton"},  //Wed
                         {"2021-02-04","19:00:00","22:00:00","UVM Badminton"},  //Thur
                         {"2021-02-09","19:00:00","22:00:00","UVM Badminton"},  //Tues
                         {"2021-02-10","19:00:00","22:00:00","UVM Badminton"}}; //Wed

    String[][] events2 = {{"2021-02-11","18:00:00","21:00:00","UVM Badminton"},  //Thur
                          {"2021-02-16","18:00:00","21:00:00","UVM Badminton"},  //Tues
                          {"2021-02-17","19:00:00","22:00:00","UVM Badminton"},  //Wed
                          {"2021-02-18","19:00:00","22:00:00","UVM Badminton"},  //Thur
                          {"2021-02-23","18:00:00","21:00:00","UVM Badminton"},  //Tues
                          {"2021-03-02","18:00:00","21:00:00","UVM Badminton"}}; //Tues


    String[][] events3 = {{"2021-03-08","18:00:00","22:00:00","UVM Badminton"},  //party
                          {"2021-03-15","18:00:00","22:00:00","UVM Badminton"},  //meeting
                          {"2021-03-22","18:00:00","22:00:00","UVM Badminton"},  //party
                          {"2021-03-29","18:00:00","22:00:00","UVM Badminton"},  //meeting
                          {"2021-04-12","18:00:00","22:00:00","UVM Badminton"},  //party
                          {"2021-04-19","18:00:00","22:00:00","UVM Badminton"},  //party
                          {"2021-04-26","18:00:00","22:00:00","UVM Badminton"}}; //meeting

    String[][] events4 = {{"2021-04-11","19:00:00","22:00:00","UVM Badminton"},
                          {"2021-04-12","19:00:00","22:00:00","UVM Badminton"},
                          {"2021-04-13","19:00:00","22:00:00","UVM Badminton"},
                          {"2021-04-14","19:00:00","22:00:00","UVM Badminton"},
                          {"2021-04-15","19:00:00","22:00:00","UVM Badminton"},
                          {"2021-04-16","19:00:00","22:00:00","UVM Badminton"},
                          {"2021-04-17","19:00:00","22:00:00","UVM Badminton"},
                          {"2021-04-18","19:00:00","22:00:00","UVM Badminton"},
                          {"2021-04-19","19:00:00","22:00:00","UVM Badminton"},
                          {"2021-04-20","19:00:00","22:00:00","UVM Badminton"},
                          {"2021-04-21","19:00:00","22:00:00","UVM Badminton"},
                          {"2021-04-22","19:00:00","22:00:00","UVM Badminton"},
                          {"2021-04-23","19:00:00","22:00:00","UVM Badminton"},
                          {"2021-04-24","19:00:00","22:00:00","UVM Badminton"},
                          {"2021-04-25","19:00:00","22:00:00","UVM Badminton"},
                          {"2021-04-26","19:00:00","22:00:00","UVM Badminton"},
                          {"2021-04-27","19:00:00","22:00:00","UVM Badminton"},
                          {"2021-04-28","19:00:00","22:00:00","UVM Badminton"},
                          {"2021-04-29","19:00:00","22:00:00","UVM Badminton"},
                          {"2021-04-30","19:00:00","22:00:00","UVM Badminton"},
                          {"2021-05-01","19:00:00","22:00:00","UVM Badminton"},
                          {"2021-05-02","19:00:00","22:00:00","UVM Badminton"}};

    static final int callBackID = 52;
    private static WorkManager workManager;
    private ArrayList<String> columns;
    private CursorAdapter cursorAdapter1;
    private CursorAdapter cursorAdapter2;
    private ContentResolver cr;
    private Uri uri;
    private ListView list1;
    private ListView list2;
    private LocalDate today;

    /**
     * This is the onCreate method which triggers initialization of the view and lists
     * @param savedInstanceState saved instance state of main activity
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions(callBackID, Manifest.permission.READ_CALENDAR,Manifest.permission.WRITE_CALENDAR);
        list1 = findViewById(R.id.weekList1);
        list2 = findViewById(R.id.weekList2);
        findList1(list1);
        findList2(list2);
    }

    /**
     * This method fills the work manager with a set of club events from array events1 and starts the work manager to simulate club event injection.
     */
    public void start1(){
        DM = new DataManager();
        List<WorkRequest> workers = new ArrayList<>();
        for (int i = 0; i < events1.length; i++) {
            Data data = new Data.Builder().putStringArray("event", events1[i]).build();
            WorkRequest eventWorkRequest;
            if (i == 0) {
                eventWorkRequest = new OneTimeWorkRequest.Builder(ClubEventWorker.class).setInputData(data).build();
            }
            else{
                eventWorkRequest = new OneTimeWorkRequest.Builder(ClubEventWorker.class).setInputData(data).setInitialDelay(i * 10, TimeUnit.SECONDS).build();
            }
            workers.add(eventWorkRequest);
        }
        workManager = WorkManager.getInstance(this);
        workManager.enqueue(workers);
    }

    /**
     * This method fills the work manager with a set of club events from array events2 and starts the work manager to simulate club event injection
     */
    public void start2(){
        DM = new DataManager();
        List<WorkRequest> workers = new ArrayList<>();
        for (int i = 0; i < events2.length; i++) {
            Data data = new Data.Builder().putStringArray("event", events2[i]).build();
            WorkRequest eventWorkRequest;
            if (i == 0) {
                eventWorkRequest = new OneTimeWorkRequest.Builder(ClubEventWorker.class).setInputData(data).build();
            }
            else{
                eventWorkRequest = new OneTimeWorkRequest.Builder(ClubEventWorker.class).setInputData(data).setInitialDelay(i * 10, TimeUnit.SECONDS).build();
            }
            workers.add(eventWorkRequest);
        }
        workManager = WorkManager.getInstance(this);
        workManager.enqueue(workers);
    }

    /**
     * This method fills the work manager with a set of club events from array events3 and starts the work manager to simulate club event injection
     */
    public void start3(){
        DM = new DataManager();
        List<WorkRequest> workers = new ArrayList<>();
        for (int i = 0; i < events3.length; i++) {
            Data data = new Data.Builder().putStringArray("event", events3[i]).build();
            WorkRequest eventWorkRequest;
            if (i == 0) {
                eventWorkRequest = new OneTimeWorkRequest.Builder(ClubEventWorker.class).setInputData(data).build();
            }
            else{
                eventWorkRequest = new OneTimeWorkRequest.Builder(ClubEventWorker.class).setInputData(data).setInitialDelay(i * 10, TimeUnit.SECONDS).build();
            }
            workers.add(eventWorkRequest);
        }
        workManager = WorkManager.getInstance(this);
        workManager.enqueue(workers);
    }

    /**
     * This method fills the work manager with a set of club events from array events4 and starts the work manager to simulate club event injection
     */
    public void start4(){
        DM = new DataManager();
        List<WorkRequest> workers = new ArrayList<>();
        for (int i = 0; i < events4.length; i++) {
            Data data = new Data.Builder().putStringArray("event", events4[i]).build();
            WorkRequest eventWorkRequest;
            if (i == 0) {
                eventWorkRequest = new OneTimeWorkRequest.Builder(ClubEventWorker.class).setInputData(data).build();
            }
            else{
                eventWorkRequest = new OneTimeWorkRequest.Builder(ClubEventWorker.class).setInputData(data).setInitialDelay(i * 10, TimeUnit.SECONDS).build();
            }
            workers.add(eventWorkRequest);
        }
        workManager = WorkManager.getInstance(this);
        workManager.enqueue(workers);
    }

    /**
     * This is the onclick method that change the activity to event list
     * @param v target view
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void toEventlist(View v) {
        Intent toEventList = new Intent(this, RecordedEventList.class);
        startActivity(toEventList);
    }

    /**
     * This is the onclick method that change the activity to recycle bin
     * @param v target view
     */
    public void toDeletedlist(View v) {
        Intent toDeletedlist = new Intent(this, DeletedEventList.class);
        startActivity(toDeletedlist);
    }

    /**
     * This method checks for application permission and ask user for permission if not already granted
     * @param callbackId call back id of permission request
     * @param permissionsId permission id for permission request
     */
    private void checkPermissions(int callbackId, String... permissionsId) {
        if (permissionsId[0] .equals(Manifest.permission.READ_CALENDAR)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissionsId, callbackId);
            }
            else if (permissionsId[1] .equals(Manifest.permission.WRITE_CALENDAR)) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, permissionsId, callbackId + 1);
                }
            }
        }
    }

    /**
     * This method creates the permission request window to ask user for Android Calendar permission
     * @param requestCode request code for permission
     * @param permissions the permissions to be requested
     * @param grantResults results from the user and phone
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case callBackID:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    Log.i("Permission granted","Perm granted" + Arrays.toString(permissions) + " " + Arrays.toString(grantResults));
                    ContentResolver cr = this.getContentResolver();
                    Cursor cursor = cr.query(CalendarContract.Calendars.CONTENT_URI,new String[]{CalendarContract.Calendars._ID,CalendarContract.Calendars.NAME},null,null);
                    while (cursor.moveToNext()){
                        Log.i("KunCalQuery","ID = " + cursor.getInt(0) + " name = " + cursor.getString(1));
                    }
                    DatabaseUtils.dumpCursorToString(cursor);
                }  else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    Toast.makeText(this,"Cannot read Calendar",Toast.LENGTH_LONG).show();
                }
                return;
            case callBackID + 1:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.

                }  else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    Toast.makeText(this,"Cannot write Calendar",Toast.LENGTH_LONG).show();
                }
                return;
        }

        // Other 'case' lines to check for other
        // permissions this app might request.
    }

    /**
     * This method creates the menu bar on the top right corner
     * @param menu menu object
     * @return creation result
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * This method resets all event lists and removes all club events inserted by this app from Android Calendar for testing purpose
     */
    public void reset(){
        Uri uriAll = Uri.parse("content://com.example.notificationtest.DataManager/ClubEvent");
        ContentResolver cr = getContentResolver();
        cr.delete(uriAll,null,null);
    }

    /**
     * This method stops work manager inserting more works for testing purpose
     */
    public void stop(){
        if (workManager != null) {
            workManager.cancelAllWork();
        }
    }

    /**
     * The onStop method from super class activity
     */
    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * This method creates a list for all club events of the current week displaying on the main screen after querying the database
     * @param list the list view to fill up
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void findList1(ListView list) {
        today = LocalDate.now();
        DM = new DataManager();
        cr = this.getContentResolver();
        LocalDate minDate = today;
        LocalDate maxDate = today.plusDays(6);
        uri = Uri.parse("content://com.example.notificationtest.DataManager/ClubTreeActive");
        Cursor cursor = cr.query(uri,null,"minDate >= " + minDate + " AND maxDate <= " + maxDate,null,null);

        //List of all columns
        columns = new ArrayList<>();
        columns.add(DataManager.ID);
        columns.add(DataManager.clubName);
        columns.add(DataManager.date);
        columns.add(DataManager.startTime);
        columns.add(DataManager.endTime);
        //Horizontal linear layout for column display
        //Listview for row display
        //for each text view entry

        if (cursor != null){
            cursorAdapter1 = new DeletedListAdapter(this,R.layout.week_list1,cursor, columns.toArray(new String[columns.size()]) ,
                    new int []{R.id.w1Eventid,R.id.W1C1,R.id.W1C2,R.id.W1C3,R.id.W1C4},0);
            list.setAdapter(cursorAdapter1);
            TextView label = findViewById(R.id.thisWeek);
            if (cursor.moveToFirst()){
                label.setText("This week's events");
            }
            else{
                label.setText("No events this week");
            }
        }

    }

    /**
     * This method creates a list for all club events of the following week displaying on the main screen after querying the database
     * @param list the list to fill
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void findList2(ListView list) {
        today = LocalDate.now();
        DM = new DataManager();
        cr = this.getContentResolver();
        LocalDate minDate = today.plusDays(7);
        LocalDate maxDate = today.plusDays(14);
        uri = Uri.parse("content://com.example.notificationtest.DataManager/ClubTreeActive");
        Cursor cursor = cr.query(uri,null,"minDate >= " + minDate + " AND maxDate <= " + maxDate,null,null);

        //List of all columns
        columns = new ArrayList<>();
        columns.add(DataManager.ID);
        columns.add(DataManager.clubName);
        columns.add(DataManager.date);
        columns.add(DataManager.startTime);
        columns.add(DataManager.endTime);
        //Horizontal linear layout for column display
        //Listview for row display
        //for each text view entry

        if (cursor != null) {
            cursorAdapter2 = new DeletedListAdapter(this, R.layout.week_list2, cursor, columns.toArray(new String[columns.size()]),
                    new int[]{R.id.w2Eventid, R.id.W2C1, R.id.W2C2, R.id.W2C3, R.id.W2C4}, 0);
            list.setAdapter(cursorAdapter2);
            TextView label = findViewById(R.id.nextWeek);
            if (cursor.moveToFirst()){
                label.setText("Next week's events");
            }
            else{
                label.setText("No events next week");
            }
        }
    }

    /**
     * This method dumps the result tree into the logcat for testing purpose
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void dumpClubTree(){
        cr = this.getContentResolver();
        Uri uriClubTree = uri.parse("content://com.example.notificationtest.DataManager/ClubTree");
        Uri uriCalTree = uri.parse("content://com.example.notificationtest.DataManager/CalendarTree");
        cr.query(uriClubTree,null,null,null);
        cr.query(uriCalTree,null,null,null);
    }

    /**
     * This method fills the menu bar on the top right corner
     * @param item the item of the menu
     * @return menu creation result
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) { switch(item.getItemId()) {
        case R.id.start1:
            //add the function to perform here
            this.start1();
            return true;
        case R.id.start2:
            //add the function to perform here
            this.start2();
            return true;
        case R.id.start3:
            this.start3();
            return true;
        case R.id.start4:
            this.start4();
            return true;
        case R.id.dumpClub:
            this.dumpClubTree();
            return true;
        case R.id.stop:
            //add the function to perform here
            this.stop();
            return true;
        case R.id.reset:
            //add the function to perform here
            this.reset();
            return true;
    }
        return(super.onOptionsItemSelected(item));
    }

}

/**
 * Read Me: The club events tree now handles only two conflicts from Android Calendar for each club event. Meaning that if there are three events in Android Calendar,
 * the third Android Calendar event is ignored. This maybe an issue if two events overlaps with the club event while a all day holiday happens on the same day. In this case,
 * the second overlap event from Android Calendar is ignored by the calendar tree.
 **/