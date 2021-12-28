package com.comp.veta.Background;

import java.util.ArrayList;

public class Group {



    private boolean hasPassword;
    private String password;
    private String photoURL;
    private String name;
    private int numPeople;
    private String groupID;

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

    public Group(String thename, Boolean hasPass, String pass) {
        name = thename;
        hasPassword = hasPass;
        password = pass;

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


    public void addPerson() {
      //  userIDs.add(ID);
        numPeople++;
    }

    public void removePerson() {
      //  userIDs.remove(ID);
        numPeople--;
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


}
