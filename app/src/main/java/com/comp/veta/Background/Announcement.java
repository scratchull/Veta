package com.comp.veta.Background;

public class Announcement {

    private String message;
    private String sender;
    private String time;

    public Announcement(String m, String s, String t){
        message = m;
        sender = s;
        time = t;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


    public Announcement() {

    }






}
