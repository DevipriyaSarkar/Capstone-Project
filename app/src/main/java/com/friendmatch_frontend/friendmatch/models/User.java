package com.friendmatch_frontend.friendmatch.models;

public class User {
    private int id;
    private String name;
    private String gender;
    private boolean isFriend;

    public User() {
    }

    public User(int id, String name, String gender) {
        this.id = id;
        this.name = name;
        this.gender = (gender.equals("M")) ? "Male" : "Female";
    }

    public User(int id, String name, String gender, boolean isFriend) {
        this.id = id;
        this.name = name;
        this.gender = (gender.equals("M")) ? "Male" : "Female";
        this.isFriend = isFriend;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public boolean isFriend() {
        return isFriend;
    }

}
