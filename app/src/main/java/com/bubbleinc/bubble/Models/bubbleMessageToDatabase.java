package com.bubbleinc.bubble.Models;

import java.util.Map;

public class bubbleMessageToDatabase {

    private String name;
    private String message;
    private Map<String, String> timestamp;

    public bubbleMessageToDatabase(String name, String message, Map<String, String> timestamp) {
        this.name = name;
        this.message = message;
        this.timestamp = timestamp;
    }

    public bubbleMessageToDatabase(){

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

    public Map<String, String> getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Map<String, String> timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "bubbleMessageToDatabase{" +
                "name='" + name + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
