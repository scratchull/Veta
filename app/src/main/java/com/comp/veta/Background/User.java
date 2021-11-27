package com.comp.veta.Background;

import java.util.ArrayList;

public class User {
    private String DisplayName;
    private String email;
    private ArrayList<String> groupIDs;

    public User(){

    }
    public User(String name) {
        DisplayName = name;
    }

    public String getDisplayName() {
        return DisplayName;
    }

    public void setDisplayName(String displayName) {
        DisplayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public ArrayList<String> getGroupArray() {
        return groupIDs;
    }

    public void setGroupArray(ArrayList<String> groupArray) {
        this.groupIDs = groupArray;
    }









}
