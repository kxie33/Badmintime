package com.example.notificationtest;

import android.os.Build;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import androidx.annotation.RequiresApi;

public class ClubEvent extends Event {
    private int ID = 0;
    private static int nextID = 0;
    private LocalDateTime timeStamp;
    private String clubName = "";
    private LocalDate date = null;
    private LocalTime stTime= null;
    private LocalTime edTime= null;
    private String  overLap = "";
    private String calendarEvent = "";
    private boolean conf = false;
    private boolean notify = false;
    private boolean deleted = false;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public ClubEvent(String[] attrs){
        if (attrs.length != 0) {
            this.date = LocalDate.parse(attrs[0]);
            this.stTime = LocalTime.parse(attrs[1]);
            this.edTime = LocalTime.parse(attrs[2]);
            this.overLap = attrs[3];
            this.calendarEvent = attrs[4];
            this.clubName = attrs[5];
        }

        ID = nextID;
        nextID++;
        timeStamp = LocalDateTime.now();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public String getValue(String attribute) {

        String result = "";
        switch (attribute) {
            case "Start": 		result = this.stTime.toString(); break;
            case "End":         result = this.edTime.toString(); break;
            case "overLap":     result = this.overLap; break;
            case "Day" :        result = this.date.getDayOfWeek().toString(); break;
            case "CalendarEvent": result = this.calendarEvent; break;
            case "clubName" : result = this.clubName; break;
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int getTimeStamp() {
        return timeStamp.getSecond();
    }

    public String toString() {
        return stTime+" "+edTime+" "+" "+overLap+" "+calendarEvent;
    }

    public LocalDate getDate(){
        return this.date;
    }

    public LocalTime getStTime(){
        return stTime;
    }

    public LocalTime getEdTime(){
        return edTime;
    }

    public void setNotify(){
        notify = true;
        deleted = false;
    }

    public void setRecord(){
        notify = false;
        deleted = false;
    }

    public void setDeleted(){
        notify = false;
        deleted = true;
    }

    public Boolean getNotify(){
        return notify;
    }

    public Boolean getDeleted(){
        return deleted;
    }

    public String getFlags(){
        String result = null;

        if (this.notify == true && this.deleted == false){
            result = "N";
        }
        else if(this.notify == false && this.deleted == false){
            result = "R";
        }
        else if(this.notify == false && this.deleted == true){
            result = "D";
        }
        else{
            result = "Error: Notify & Deleted are both true";
        }
        return result;
    }

    public String getOverLap() {
        return overLap;
    }

    public String getCalendarEvent() {
        return calendarEvent;
    }

    public void setID(int id){
        ID = id;
    }

    public int getID(){
        return ID;
    }

    public String getClubName(){
        return clubName;
    }
}
