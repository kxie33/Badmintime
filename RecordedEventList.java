package com.example.notificationtest;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;

public class RecordedEventList extends AppCompatActivity {
    ArrayList<String> eventList = new ArrayList<>();


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club_event_list__d);
        ArrayList<ClubEvent> eList = new ArrayList<>();

        Uri uri = new Uri.Builder().appendPath("content://com.example.notificationtest.DataManager/ClubEvent").build();
        Cursor cursor = getContentResolver().query(uri,null,"deleted = false",null,null);

        final ListView list = findViewById(R.id.eventList);

        CursorAdapter CursorAdapter = new SimpleCursorAdapter(this,R.id.eventList,cursor,new String[]{"id"},new int []{0},0);
        list.setAdapter(CursorAdapter);

    }


}