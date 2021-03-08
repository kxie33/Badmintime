package com.example.notificationtest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Class: CalendarEvent. CalendarEvent class contains all information from the event read from android calendar.
 * Each CalendarEvent is attached to a club event where they overlap with each other.
 * One club event can only attach two calendar events. Future implementation will add support to multiple overlapping calendar events
 * Calendar events also have Notify, Record and Delete flags.
 * Calendar event decision tree will use the words to learn user preferences by calculating the entropy for the existence of reach word.
 */
public class CalendarEvent extends Event {
    int ID; // ID of the calendar event
    LocalDateTime timeStamp; // timeStamp of the calendar event
    private boolean notify = false; // notify flag
    private boolean deleted = false; // deleted flag
    Collection<String> words; // words of from title and description

    /**
     * This is the constructor of CalendarEvent
     * @param w string input that will be separated by space and put into words field
     * @param t time of the CalendarEvent in LocalDateTime format
     * @param id id of the calendar event, the same from Android Calendar
     */
    public CalendarEvent(String w, LocalDateTime t, int id){
        words = new ArrayList<>();
        if (w != null) {
            String[] ws = w.split(" ");
            for (int i = 0; i < ws.length; i++) {
                words.add(ws[i]);
            }
            timeStamp = t;
            this.ID = id;
        }
    }

    /**
     * This is the getter for words field
     * @return collection of string for all words
     */
    public Collection<String> getWords(){
        return words;
    }

    /**
     * This is the getter for a specific word in the words field. used by the tree
     * @param word target word
     * @return result string value of the word
     */
    @Override
    public String getValue(String word) {
        return String.valueOf(words.contains(word));
    }

    /**
     * This is the getter for the ID field
     * @return value of ID field
     */
    public int getID() {
        return ID;
    }

    /**
     * This is the setter of ID field
     * @param ID ID field
     */
    public void setID(int ID) {
        this.ID = ID;
    }

    /**
     * This is the getter for the Timestamp field
     * @return the value of the TImeStamp field
     */
    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    /**
     * This is the toString method used for debugging.
     * This method will convert all known fields' values to string
     * @return the string concatenated of all known fields
     */
    public String toString() {
        return ID + " " + timeStamp +  " " + notify + ", " + deleted + "; " + words.toString();
    }

    /**
     * This method sets the notify flag to true and deleted flag to false.
     * The state of the event is notify.
     */
    public void setNotify(){
        notify = true;
        deleted = false;
    }

    /**
     * This method sets the notify flag to false and deleted flag to false.
     * The state of the event is record.
     */
    public void setRecord(){
        notify = false;
        deleted = false;
    }

    /**
     * This method sets the notify flag to false and deleted flag to true.
     * The state of the event is deleted.
     */
    public void setDeleted(){
        notify = false;
        deleted = true;
    }

    /**
     * This is the getter for the notify flag only
     * @return the value of the notify flag, either true or false
     */
    public Boolean getNotify(){
        return notify;
    }

    /**
     * This is the getter for the deleted flag only
     * @return the value of the deleted flag, either true or false
     */
    public Boolean getDeleted(){
        return deleted;
    }

    /**
     * This method will check the values of the notify flag and deleted flag to determine the state of this clalendar event.
     * Result N indicates the event is at state notify with notify flag being true and deleted flag being false.
     * Result R indicates the event is at state record with notify flag being false and deleted flag being false.
     * Result D indicates the event is at state deleted with notify flag being false and deleted flag being true.
     * @return the result string after checking flags
     */
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
}
