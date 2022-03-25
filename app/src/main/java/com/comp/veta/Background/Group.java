package com.comp.veta.Background;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Group {



    private boolean hasPassword;
    private String password;
    private String photoURL;
    private String name;
    private int numPeople;
    private String groupID;
    private ArrayList<Announcement> announcements= new ArrayList<>() ;
    private ArrayList<Event> events= new ArrayList<>() ;
    private String creatorID;
    private boolean allowAllAccess;

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }




    public Group() {

    }

    public Group(String theName, String pass) {
        name = theName;
        password = pass;
        numPeople = 0;


    }

    public Group(String thename, Boolean hasPass, String pass, Boolean access, String id) {
        name = thename;
        hasPassword = hasPass;
        password = pass;
        creatorID = id;
        allowAllAccess = access;

    }
    public ArrayList<Announcement> getAnnouncements() {
        return announcements;
    }

    public void setAnnouncements(ArrayList<Announcement> announcements) {
        this.announcements = announcements;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setNumPeople(int numPeople) {
        this.numPeople = numPeople;
    }

    public int getNumPeople() {
        return numPeople;
    }

    public boolean isAllowAllAccess() {
        return allowAllAccess;
    }

    public void setAllowAllAccess(boolean allowAllAccess) {
        this.allowAllAccess = allowAllAccess;
    }

    public void addPerson() {
      //  userIDs.add(ID);
        numPeople++;
    }

    public void removePerson() {
      //  userIDs.remove(ID);
        numPeople--;
    }

    public String getCreatorID() {
        return creatorID;
    }

    public void setCreatorID(String creatorID) {
        this.creatorID = creatorID;
    }

    public boolean isHasPassword() {
        return hasPassword;
    }

    public void setHasPassword(boolean hasPassword) {
        this.hasPassword = hasPassword;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public ArrayList<Event> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<Event> events) {
        this.events = events;
    }
}


