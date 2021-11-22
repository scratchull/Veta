package com.comp.veta.Background;

import java.util.ArrayList;

public class Person {

    private ArrayList<String> notifications = new ArrayList<>();
    private String email;
    private String password;

    public Person(){

    }



    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setNotifications(ArrayList<String> notifications) {
        this.notifications = notifications;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<String> getNotifications() {
        return notifications;
    }

    public void addNotification(String notif){
        notifications.add(notif);
    }
    public void removeNotification (String notif){
        notifications.remove(notif);
    }
}
