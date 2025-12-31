package com.example.qsync_2207097_android;

public class Department {
    public String id;
    public String name;
    public String description;
    public long createdAt;

    public Department() {
        this.createdAt = System.currentTimeMillis();
    }

    public Department(String name, String description) {
        this.name = name;
        this.description = description;
        this.createdAt = System.currentTimeMillis();
    }

    public Department(String name, String description, String specialization) {
        this.name = name;
        this.description = description;
        this.createdAt = System.currentTimeMillis();
    }
}

