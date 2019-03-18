package com.bubbleinc.bubble.Models;

public class bubbleMessageNotification {

    private String name;
    private String key;
    private String message;
    private double bubbleMessageLat;
    private double bubbleMessageLong;

    public bubbleMessageNotification(String key, String name, String message, double bubbleMessageLat, double bubbleMessageLong) {
        this.key = key;
        this.name = name;
        this.message = message;
        this.bubbleMessageLat = bubbleMessageLat;
        this.bubbleMessageLong = bubbleMessageLong;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public bubbleMessageNotification() {

    }

    public double getBubbleMessageLat() {
        return bubbleMessageLat;
    }

    public void setBubbleMessageLat(double bubbleMessageLat) {
        this.bubbleMessageLat = bubbleMessageLat;
    }

    public double getBubbleMessageLong() {
        return bubbleMessageLong;
    }

    public void setBubbleMessageLong(double bubbleMessageLong) {
        this.bubbleMessageLong = bubbleMessageLong;
    }
}
