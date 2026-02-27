package com.team_one.soen_345_project.viewmodel.admindash;

import com.team_one.soen_345_project.model.entity.Event;

import java.util.ArrayList;
import java.util.List;

public class AdminDashUiState {
    private final String message;
    private final boolean isActionComplete;
    private final int eventCount;
    private final List<Event> events;

    public AdminDashUiState(String message, boolean isActionComplete) {
        this.message = message;
        this.isActionComplete = isActionComplete;
        this.eventCount = 0;
        this.events = new ArrayList<>();
    }

    public AdminDashUiState(String message, boolean isActionComplete, int eventCount) {
        this.message = message;
        this.isActionComplete = isActionComplete;
        this.eventCount = eventCount;
        this.events = new ArrayList<>();
    }

    public AdminDashUiState(String message, boolean isActionComplete, int eventCount, List<Event> events) {
        this.message = message;
        this.isActionComplete = isActionComplete;
        this.eventCount = eventCount;
        this.events = events != null ? events : new ArrayList<>();
    }

    public boolean isActionComplete() { return isActionComplete; }

    public String getMessage() {
        return message;
    }

    public int getEventCount() {
        return eventCount;
    }

    public List<Event> getEvents() {
        return events;
    }
}
