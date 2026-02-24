package com.team_one.soen_345_project.model.entity;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class Event {
    @DocumentId // AutoID by Firestore
    private String eventId;
    private String title;
    private String description;
    private Timestamp date;
    private String location;
    private String category;
    private int capacity;
    private double price;

    public Event(String eventId, String title, String description, Timestamp date, String location, String category, int capacity, double price) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.date = date;
        this.location = location;
        this.category = category;
        this.capacity = capacity;
        this.price = price;
    }

    public Event(HashMap<String, String> eventInfo) {
        this.title = eventInfo.get("title");
        this.description = eventInfo.get("disc");
        this.date = convertToTimestamp(eventInfo.get("date"), eventInfo.get("time"));
        this.location = eventInfo.get("location");
        this.category = eventInfo.get("category");
        this.capacity = Integer.parseInt(eventInfo.get("capacity"));
        this.price = Double.parseDouble(eventInfo.get("price"));
    }

    // Date parser to turn two Strings i.e. "1771977600000" (Long date in ms) and "14:30" into a timestamp object
    public Timestamp convertToTimestamp(String date, String time) {
        long hours = Long.parseLong(time.substring(0, 2)) * 3600000;
        long minutes = Long.parseLong(time.substring(3)) * 60000;
        long dateLong = Long.parseLong(date);

        long dateInMs = dateLong + hours + minutes;

        return new Timestamp(new Date(dateInMs));
    }

    // GETTERS AND SETTERS

    public String getEventId() {
        return eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
