package com.comp.veta.Background;

import java.util.ArrayList;

public class User {
    private String DisplayName;

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

    public ArrayList<String> getGroupIDs() {
        return groupIDs;
    }

    public void setGroupIDs(ArrayList<String> groupIDs) {
        this.groupIDs = groupIDs;
    }









}
