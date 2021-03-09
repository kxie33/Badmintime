package com.example.notificationtest;

import java.time.LocalDateTime;

/**
 * Class: event. This is the super class for club event and calendar event
 */
public abstract class Event {

    /**
     * This method gets the attribute string value of the event.
     * @param attr the target attribute name
     * @return the string value of the attribute
     */
    public abstract String getValue(String attr);

    /**
     * This method gets the timestamp of the event
     * @return timestamp in LocalDateTime
     */
    public abstract LocalDateTime getTimeStamp();
}
