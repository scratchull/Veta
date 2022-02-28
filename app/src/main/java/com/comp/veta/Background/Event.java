package com.comp.veta.Background;

import com.google.type.DateTime;

import java.util.Date;

public class Event implements Comparable<Event>{

    private String eventName;
    private String eventDescription;
    private String eventStringLocation;
    private String eventCreator;
    private double LONGITUDE;
    private double LATITUDE;
    private Date eventTime;




    public Event(String eventName, String eventDescription, String eventStringLocation, String eventCreator, double LONGITUDE, double LATITUDE, Date eventTime) {
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.eventStringLocation = eventStringLocation;
        this.eventCreator = eventCreator;
        this.LONGITUDE = LONGITUDE;
        this.LATITUDE = LATITUDE;
        this.eventTime = eventTime;


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

    public double getLONGITUDE() {
        return LONGITUDE;
    }

    public void setLONGITUDE(double LONGITUDE) {
        this.LONGITUDE = LONGITUDE;
    }

    public double getLATITUDE() {
        return LATITUDE;
    }

    public void setLATITUDE(double LATITUDE) {
        this.LATITUDE = LATITUDE;
    }

    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    public Event() {

    }


    @Override
    public int compareTo(Event event) {
        return getEventTime().compareTo(event.getEventTime());
    }
}
