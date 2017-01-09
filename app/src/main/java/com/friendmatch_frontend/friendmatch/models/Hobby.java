package com.friendmatch_frontend.friendmatch.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Hobby implements Parcelable {
    private int hobbyID;
    private String hobbyName;
    private int hobbyImg;
    private boolean isHobby;

    public Hobby() {
    }

    public Hobby(int hobbyID, String hobbyName) {
        this.hobbyID = hobbyID;
        this.hobbyName = hobbyName;
    }

    public Hobby(int hobbyID, String hobbyName, int hobbyImg, boolean isHobby) {
        this.hobbyID = hobbyID;
        this.hobbyName = hobbyName;
        this.hobbyImg = hobbyImg;
        this.isHobby = isHobby;
    }

    public void setHobbyID(int hobbyID) {
        this.hobbyID = hobbyID;
    }

    public void setHobbyName(String hobbyName) {
        this.hobbyName = hobbyName;
    }

    public void setHobbyImg(int hobbyImg) {
        this.hobbyImg = hobbyImg;
    }

    public int getHobbyID() {
        return hobbyID;
    }

    public String getHobbyName() {
        return hobbyName;
    }

    public int getHobbyImg() {
        return hobbyImg;
    }

    public void setHobby(boolean hobby) {
        isHobby = hobby;
    }


    public boolean isHobby() {
        return isHobby;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(hobbyID);
        parcel.writeInt(hobbyImg);
        int selected = (isHobby) ? 1 : 0;
        parcel.writeInt(selected);
        parcel.writeString(hobbyName);
    }

    // Creator
    public static final Parcelable.Creator<Hobby> CREATOR
            = new Parcelable.Creator<Hobby>() {
        public Hobby createFromParcel(Parcel in) {
            return new Hobby(in);
        }

        public Hobby[] newArray(int size) {
            return new Hobby[size];
        }
    };

    //De-parcel object
    public Hobby(Parcel in) {
        this.hobbyName = in.readString();
        this.hobbyID = in.readInt();
        this.hobbyImg = in.readInt();
        this.isHobby = (in.readInt() == 1);
    }
}
