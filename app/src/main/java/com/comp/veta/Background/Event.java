package com.comp.veta.Background;

import java.util.Date;

public class Event {

    private String eventName;
    private String eventDescription;
    private String eventStringLocation;
    private String eventCreator;
    private long LONGITUDE;
    private long LATITUDE;
    private long eventTime;


    public Event(String m, String s, long t){
        eventName = m;
        eventDescription = s;
        eventTime = t;
    }

    public Event(String m, String s){
        eventName = m;
        eventDescription = s;
        eventTime  = new Date().getTime();;
    }


    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getEventStringLocation() {
        return eventStringLocation;
    }

    public void setEventStringLocation(String eventStringLocation) {
        this.eventStringLocation = eventStringLocation;
    }

    public String getEventCreator() {
        return eventCreator;
    }

    public void setEventCreator(String eventCreator) {
        this.eventCreator = eventCreator;
    }

    public long getLONGITUDE() {
        return LONGITUDE;
    }

    public void setLONGITUDE(long LONGITUDE) {
        this.LONGITUDE = LONGITUDE;
    }

    public long getLATITUDE() {
        return LATITUDE;
    }

    public void setLATITUDE(long LATITUDE) {
        this.LATITUDE = LATITUDE;
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public Event() {

    }




}
