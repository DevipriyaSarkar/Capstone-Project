package com.friendmatch_frontend.friendmatch.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Event implements Parcelable {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(eventID);
        parcel.writeString(eventName);
        parcel.writeString(eventCity);
        parcel.writeString(eventDate);
        parcel.writeInt(eventImg);
    }

    // Creator
    public static final Parcelable.Creator<Event> CREATOR
            = new Parcelable.Creator<Event>() {
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    //De-parcel object
    public Event(Parcel in) {
        this.eventID = in.readInt();
        this.eventName = in.readString();
        this.eventCity = in.readString();
        this.eventDate = in.readString();
        this.eventImg = in.readInt();
    }
}
