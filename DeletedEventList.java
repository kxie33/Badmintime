package com.example.notificationtest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class DeletedEventList extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deleted_event);

        final ListView list = findViewById(R.id.deletedeventList);
        ArrayList<ClubEvent> deletedList = new ArrayList<>();

        ArrayAdapter<ClubEvent> arrayAdapter = new ArrayAdapter<ClubEvent>(this,android.R.layout.simple_list_item_1, deletedList);
        list.setAdapter(arrayAdapter);
    }
}