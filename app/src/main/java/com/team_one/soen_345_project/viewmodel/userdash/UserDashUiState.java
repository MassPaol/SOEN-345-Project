package com.team_one.soen_345_project.viewmodel.userdash;

import com.team_one.soen_345_project.model.entity.Event;

import java.util.ArrayList;
import java.util.List;

public class UserDashUiState {
    private final String message;
    private final boolean isActionComplete;
    private final int totalEventCount;
    private final List<Event> events;

    public UserDashUiState(String message, boolean isActionComplete) {
        this.message = message;
        this.isActionComplete = isActionComplete;
        this.totalEventCount = 0;
        this.events = new ArrayList<>();
    }

    public UserDashUiState(String message, boolean isActionComplete, int totalEventCount) {
        this.message = message;
        this.isActionComplete = isActionComplete;
        this.totalEventCount = totalEventCount;
        this.events = new ArrayList<>();
    }

    public UserDashUiState(String message, boolean isActionComplete, int totalEventCount, List<Event> events) {
        this.message = message;
        this.isActionComplete = isActionComplete;
        this.totalEventCount = totalEventCount;
        this.events = events != null ? events : new ArrayList<>();
    }

    public boolean isActionComplete() { return isActionComplete; }

    public String getMessage() { return message; }

    public int getTotalEventCount() { return totalEventCount; }

    public List<Event> getEvents() { return events; }
}

