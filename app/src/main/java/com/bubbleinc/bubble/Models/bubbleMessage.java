package com.bubbleinc.bubble.Models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class bubbleMessage {

    private String key;
    private String name;
    private String message;
    private long timestamp;

    public bubbleMessage(String key, String name, String message, long timestamp) {
        this.key = key;
        this.name = name;
        this.message = message;
        this.timestamp = timestamp;
    }



    public bubbleMessage(){

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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "bubbleMessage{" +
                "key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
