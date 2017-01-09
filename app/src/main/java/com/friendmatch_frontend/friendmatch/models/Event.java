package com.friendmatch_frontend.friendmatch.models;

public class Event {
    private int eventID;
    private String eventName;
    private String eventCity;
    private String eventDate;
    private int eventImg;
    private boolean isAttending;

    public Event() {
    }

    public Event(int eventID, String eventName, String eventCity, String eventDate, int eventImg) {
        this.eventID = eventID;
        this.eventName = eventName;
        this.eventCity = eventCity;
        this.eventDate = eventDate;
        this.eventImg = eventImg;
    }

    public Event(int eventID, String eventName, String eventCity, String eventDate, int eventImg, boolean isAttending) {
        this.eventID = eventID;
        this.eventName = eventName;
        this.eventCity = eventCity;
        this.eventDate = eventDate;
        this.eventImg = eventImg;
        this.isAttending = isAttending;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setEventCity(String eventCity) {
        this.eventCity = eventCity;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public void setEventImg(int eventImg) {
        this.eventImg = eventImg;
    }

    public int getEventID() {
        return eventID;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventCity() {
        return eventCity;
    }

    public String getEventDate() {
        return eventDate;
    }

    public int getEventImg() {
        return eventImg;
    }

    public void setAttending(boolean attending) {
        isAttending = attending;
    }

    public boolean isAttending() {
        return isAttending;
    }

}
