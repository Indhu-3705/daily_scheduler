package com.example.dailyscheduler;

public class Event {
    private String title;
    private String time; // Format "HH:mm"
    private String date; // Format "yyyy-MM-dd"

    public Event(String title, String time, String date) {
        this.title = title;
        this.time = time;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }
}