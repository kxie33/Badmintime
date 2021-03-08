package com.example.notificationtest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Class: DeletedEventList. This class setup the list UI for the deleted club events and handling the deleted intent sent from the notification.
 * In the deleted list UI, user can click restore on each of the deleted event to change the event's state from deleted to recorded.
 * When user deletes an event, the event is moved into the deleted event list and set to deleted.
 * User can restore the deleted events to record status and the events will be moved to recorded event list.
 */
public class DeletedEventList extends AppCompatActivity {
    private NotificationManagerCompat notificationManager; // NotificationManger to dismiss the notification upon receiving an intent
    private ArrayList<String> columns; // The ArrayList that contains all deleted events
    private CursorAdapter cursorAdapter; // The cursorAdapter used for displaying all events
    private ContentResolver cr; // content resolver
    private Uri uri; // The uri to query and update data in DataManager through content resolver
    private ListView list; // The list view that displays deleted events

    /**
     * This is the onCreate method inherited from the super class activity.
     * This method contains initialization for deleted events list view setup and intent handler
     * @param savedInstanceState saved instance state
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationManager = NotificationManagerCompat.from(getApplicationContext());
        setContentView(R.layout.activity_deleted_event_list);
        list = findViewById(R.id.deletedeventList);
        cr = getContentResolver();
        uri = Uri.parse("content://com.example.notificationtest.DataManager/ClubEvent"); //uriAll
        setTitle("Recycle Bin");

        ContentValues newVals = new ContentValues();

        Intent i = getIntent();
        if (i != null) {
            String action = i.getStringExtra("action");
            Log.i("KunDeleted", action + " " + i.getExtras());
            //Cursor cursor = cr.query(uri,null,"ID = "+ id,null,null);
            if (action != null) {
                int id = i.getIntExtra("id", -1);
                int noteID = i.getIntExtra("noteID",-1);
                if (action.equals("record")) {
                    newVals.put(DataManager.notify,false);
                    Uri uriSingle = Uri.parse("content://com.example.notificationtest.DataManager/ClubEvent/" + id);
                    cr.update(uriSingle,newVals,null,null);
                    notificationManager.cancel(noteID);
                }
                else if (action.equals("delete")){
                    newVals.put(DataManager.notify,false);
                    newVals.put(DataManager.deleted,true);
                    Uri uriSingle = Uri.parse("content://com.example.notificationtest.DataManager/ClubEvent/" + id);
                    cr.update(uriSingle,newVals,null,null);
                    notificationManager.cancel(noteID);
                }
            }
        }
    }

    /**
     * This is the onStart method from super class.
     * Calls the findEvents method to fill the listview
     */
    protected void onStart() {
        super.onStart();
        findEvents(list);
    }

    /**
     * This method queries the DataManager for all deleted events and fill the list with an invisible ID field and five displaying fields.
     * User can interact with the deleted field to change the state of an event
     * A restore button is displaying at the deleted field
     * @param list the list that shows all events that are not deleted
     */
    private void findEvents(ListView list) {
        Cursor cursor = cr.query(uri,null,"deleted = true",null,null);

        //List of all columns
        columns = new ArrayList<>();
        columns.add(DataManager.ID);
        columns.add(DataManager.clubName);
        columns.add(DataManager.date);
        columns.add(DataManager.startTime);
        columns.add(DataManager.endTime);
        columns.add(DataManager.deleted);
        //Horizontal linear layout for column display
        //Listview for row display
        //for each text view entry

        cursorAdapter = new DeletedListAdapter(this,R.layout.deleted_list,cursor, columns.toArray(new String[columns.size()]) ,
                new int []{R.id.deletedid,R.id.deleted1,R.id.deleted2,R.id.deleted3,R.id.deleted4,R.id.checkDDelete},0);
        list.setAdapter(cursorAdapter);
    }

    /**
     * This is the onClick method of the restore button
     * Restore will change the state of the event from deleted to record and move the event to the recorded event list
     * @param v view
     */
    public void restoreClick(View v){
        ContentValues newVals = new ContentValues();
        ListView list = findViewById(R.id.deletedeventList);
        ViewGroup parent = (ViewGroup)v.getParent();
        TextView idField = (TextView)parent.getChildAt(0);
        String id = idField.getText().toString();
        newVals.put(DataManager.notify,false);
        newVals.put(DataManager.deleted,false);
        Uri uriSingle = Uri.parse("content://com.example.notificationtest.DataManager/ClubEvent/" + id);
        cr.update(uriSingle,newVals,null,null);
    }
    
    /**
     * This is the onClick method of the Club Events button
     * This method will bring up the RecordedEventList activity
     * @param v
     */
    public void toRecordedList(View v) {
        Intent toRecordedList = new Intent(this, RecordedEventList.class);
        startActivity(toRecordedList);
    }

    /**
     * This is the onClick method of the Home button
     * This method will bring up and MainActicity
     * @param v view
     */
    public void dToMain(View v) {
        Intent toMain = new Intent(this, MainActivity.class);
        startActivity(toMain);
    }
}