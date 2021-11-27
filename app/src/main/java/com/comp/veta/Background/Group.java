package com.comp.veta.Background;

import java.util.ArrayList;

public class Group {


    private ArrayList<String> userIDs = new ArrayList<>();
    private boolean hasPassword;
    private String password;
    private String photoURL;
    private String name;
    private int numPeople;


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

    public ArrayList<String> getUserIDs() {
        return userIDs;
    }

    public void setUserIDs(ArrayList<String> userIDs) {
        this.userIDs = userIDs;
    }


    public void addPerson(String ID) {
        userIDs.add(ID);
        numPeople++;
    }

    public void removePerson(String ID) {
        userIDs.remove(ID);
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
