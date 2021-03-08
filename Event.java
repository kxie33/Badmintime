package com.example.notificationtest;

import java.time.LocalDateTime;

/**
 *
 */
public abstract class Event {

    /**
     *
     * @param attr
     * @return
     */
    public abstract String getValue(String attr);

    /**
     *
     * @return
     */
    public abstract LocalDateTime getTimeStamp();
}
