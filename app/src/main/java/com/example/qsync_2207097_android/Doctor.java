package com.example.qsync_2207097_android;

public class Doctor {
    public String id;
    public String name;
    public String specialty;
    public String departmentId;
    public String phone;
    public String email;
    public int yearsOfExperience;
    public boolean isAvailable;
    public int currentQueueLength;
    public int avgTimeMinutes = 15;
    public String startTime = "09:00";

    public Doctor() {}

    public Doctor(String id, String name, String specialty, String departmentId) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.departmentId = departmentId;
        this.phone = "";
        this.email = "";
        this.yearsOfExperience = 0;
        this.isAvailable = true;
        this.currentQueueLength = 0;
        this.avgTimeMinutes = 15;
        this.startTime = "09:00";
    }

    public Doctor(String id, String name, String specialty, String departmentId,
                  String phone, String email, int yearsOfExperience) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.departmentId = departmentId;
        this.phone = phone;
        this.email = email;
        this.yearsOfExperience = yearsOfExperience;
        this.isAvailable = true;
        this.currentQueueLength = 0;
        this.avgTimeMinutes = 15;
        this.startTime = "09:00";
    }

    public int getEstimatedWaitTime() {
        return currentQueueLength * avgTimeMinutes;
    }

    public int getAvgTimeMinutes() {
        return avgTimeMinutes;
    }
}
