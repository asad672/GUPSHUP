package com.example.appgupshup.Models;

public class Status {
    private String imageUrl;
    private String timeStamp;

    public Status() {
    }

    public Status(String imageUrl, String timeStamp) {
        this.imageUrl = imageUrl;
        this.timeStamp = timeStamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
