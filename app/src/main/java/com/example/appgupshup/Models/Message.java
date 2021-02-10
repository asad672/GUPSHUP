package com.example.appgupshup.Models;

public class Message {
    private String messageId,message,senderId;
    public String timestamp,type;
    private String callerid;
    private int feeling = -1;

    public Message() {
    }

    public Message(String message, String senderId, String timestamp,String type,String callerid) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.type=type;
        this.callerid=callerid;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getFeeling() {
        return feeling;
    }

    public void setFeeling(int feeling) {
        this.feeling = feeling;
    }

    public String getCallerid() {
        return callerid;
    }

    public void setCallerid(String callerid) {
        this.callerid = callerid;
    }
}
