package com.example.qsync_2207097_android;

public class Department {
    public String id;
    public String name;
    public String description;
    public int doctorCount;
    public int activeQueues;
    public boolean isActive;
    public long createdAt;

    public Department() {
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
    }

    public Department(String id, String name) {
        this.id = id;
        this.name = name;
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.doctorCount = 0;
        this.activeQueues = 0;
    }

    public Department(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.doctorCount = 0;
        this.activeQueues = 0;
    }

    public String getStatsText() {
        if (doctorCount == 0 && activeQueues == 0) {
            return "No data available";
        }
        return doctorCount + " Doctor" + (doctorCount != 1 ? "s" : "") +
               " • " + activeQueues + " Active Queue" + (activeQueues != 1 ? "s" : "");
    }
}

