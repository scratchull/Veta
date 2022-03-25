package com.comp.veta.Background;

import java.util.Date;

public class Announcement {

    private String message;
    private String sender;
    private long time;


    public Announcement(String m, String s, long t){
        message = m;
        sender = s;
        time = t;
    }

    public Announcement(String m, String s){
        message = m;
        sender = s;
        time  = new Date().getTime();;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }


    public Announcement() {

    }






}
