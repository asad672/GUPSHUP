package com.example.appgupshup.Models;

public class Users {
    private String Uid,name,number,profileimage;

    public Users()
    {
    }
    public Users(String uid, String name, String number, String profileimage) {
        Uid = uid;
        this.name = name;
        this.number = number;
        this.profileimage = profileimage;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }
}
