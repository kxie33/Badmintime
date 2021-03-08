package com.example.notificationtest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import org.w3c.dom.Text;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 *
 */
public class RecordedEventList extends AppCompatActivity {
    private NotificationManagerCompat notificationManager;
    private ArrayList<String> columns;
    private CursorAdapter cursorAdapter;
    private ContentResolver cr;
    private Uri uri;
    private Boolean check;
    private ListView list;

    /**
     *
     * @param savedInstanceState
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationManager = NotificationManagerCompat.from(getApplicationContext());
        setContentView(R.layout.activity_club_event_list__d);
        list = findViewById(R.id.eventList);
        cr = getContentResolver();
        uri = Uri.parse("content://com.example.notificationtest.DataManager/ClubEvent"); //uriAll
        setTitle("Club Events");

        ContentValues newVals = new ContentValues();

        Intent i = getIntent();
        if (i != null) {
            String action = i.getStringExtra("action");
            if (action != null) {
                int id = i.getIntExtra("id", -1);
                int noteID = i.getIntExtra("noteID",-1);
                if (action.equals("record")) {
                    newVals.put(DataManager.notify,false);
                    Uri uriSingle = Uri.parse("content://com.example.notificationtest.DataManager/ClubEvent/" + id);
                    cr.update(uriSingle,newVals,null,null);
                    notificationManager.cancel(noteID);
                }
                else{
                    Log.i("Kun Recordlist ","Unexpected action");
                }
            }
        }
    }

    /**
     *
     */
    protected void onStart() {
        super.onStart();
        findEvents(list);
    }

    /**
     *
     * @param list
     */
    private void findEvents(ListView list) {

        Cursor cursor = cr.query(uri,null,"deleted = false",null,null);

        //List of all columns
        columns = new ArrayList<>();
        columns.add(DataManager.ID);
        columns.add(DataManager.clubName);
        columns.add(DataManager.date);
        columns.add(DataManager.startTime);
        columns.add(DataManager.endTime);
        columns.add(DataManager.notify);
        columns.add(DataManager.deleted);
        //Horizontal linear layout for column display
        //Listview for row display
        //for each text view entry

        cursorAdapter = new RecordedListAdapter(this,R.layout.recorded_list,cursor, columns.toArray(new String[columns.size()]) ,
                new int []{R.id.recordedid,R.id.recorded1,R.id.recorded2,R.id.recorded3,R.id.recorded4,R.id.checkRecord,R.id.checkDelete},0);
        list.setAdapter(cursorAdapter);
    }

    /**
     *
     * @param v
     */
    public void recordedClick(View v){
        ContentValues newVals = new ContentValues();
        ListView list = findViewById(R.id.eventList);
        ViewGroup parent = (ViewGroup)v.getParent();
        TextView idField = (TextView)parent.getChildAt(0);
        String id = idField.getText().toString();
        check = ((CheckBox) findViewById(R.id.checkRecord)).isChecked();
        newVals.put(DataManager.notify,check);
        Uri uriSingle = Uri.parse("content://com.example.notificationtest.DataManager/ClubEvent/" + id);
        cr.update(uriSingle,newVals,null,null);
    }

    /**
     *
     * @param v
     */
    public void deletedClick(View v) {
        ContentValues newVals = new ContentValues();
        ViewGroup parent = (ViewGroup)v.getParent();
        TextView idField = (TextView)parent.getChildAt(0);
        String id = idField.getText().toString();
        newVals.put(DataManager.deleted,true);
        newVals.put(DataManager.notify,false);
        Uri uriSingle = Uri.parse("content://com.example.notificationtest.DataManager/ClubEvent/" + id);
        cr.update(uriSingle,newVals,null,null);
    }

    /**
     *
     * @param v
     */
    public void toDeletedlist(View v) {
        Intent toDeletedlist = new Intent(this, DeletedEventList.class);
        startActivity(toDeletedlist);
    }

    /**
     *
     * @param v
     */
    public void toMain(View v) {
        Intent toMain = new Intent(this, MainActivity.class);
        startActivity(toMain);
    }

}