package com.example.qsync_2207097_android;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WaitingList {
    private int id;
    private String name;
    @JsonProperty("waiting_time")
    private String waitingTime;
    private String image;

    public WaitingList() {
    }
    public WaitingList(int id, String name, String waitingTime, String image) {
        this.id = id;
        this.name = name;
        this.waitingTime = waitingTime;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(String waitingTime) {
        this.waitingTime = waitingTime;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "WaitingList{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", waitingTime='" + waitingTime + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
