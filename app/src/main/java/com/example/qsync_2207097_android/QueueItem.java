package com.example.qsync_2207097_android;

public class QueueItem {
    public String doctor;
    public String tag;
    public String token;
    public String position;
    public String wait;
    public int progress;

    public QueueItem(String doctor, String tag, String token, String position, String wait, int progress) {
        this.doctor = doctor;
        this.tag = tag;
        this.token = token;
        this.position = position;
        this.wait = wait;
        this.progress = progress;
    }
}
