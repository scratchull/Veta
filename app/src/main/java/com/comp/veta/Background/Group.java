package com.comp.veta.Background;

import java.util.ArrayList;

public class Group {

    private String name;
    private int numPeople;
    private ArrayList<String> peopleID = new ArrayList<>();
    private String password;
    private int numComplete;
    private int numBadComplete;

    public int getNumBadComplete() {
        return numBadComplete;
    }

    public void setNumBadComplete(int numBadComplete) {
        this.numBadComplete = numBadComplete;
    }

    public void setNumComplete(int numComplete) {
        this.numComplete = numComplete;
    }

    public int getNumComplete() {
        return numComplete;
    }

    public Group (){

    }
    public Group(String theName, String pass){
        name = theName;
        password =  pass;
        numPeople = 0;

    }

    public String getPassword() {
        return password;
    }



    public int getNumPeople(){
        return numPeople;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getPeopleID() {
        return peopleID;
    }

    public void addPerson(String ID){
        peopleID.add(ID);
        numPeople++;

    }
    public void removePerson(String ID){
        peopleID.remove(ID);
        numPeople--;
    }




}
