package com.example.notificationtest;

public class CalendarEvent extends Event {
    int ID;
    static int time = 0;
    int timeStamp;
    String attendee = "", eventTitle = "";

    public CalendarEvent(String[] attrs){
        if (attrs.length != 0) {
            this.attendee = attrs[0];
            this.eventTitle = attrs[1];

        }
        time++;
        timeStamp = time;
    }

    public String getValue(String attr) {

        String result = "";

        switch (attr) {
            case "Date":       result = this.attendee; break;
            case "Time":       result = this.eventTitle; break;
        }
        return result;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public String toString() {
        return attendee+" "+eventTitle+" ";
    }
}
