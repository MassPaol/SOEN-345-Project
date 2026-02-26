package com.team_one.soen_345_project.viewmodel.admindash;

public class AdminDashUiState {
    private final String message;
    private final boolean isActionComplete;
    private final int eventCount;

    public AdminDashUiState(String message, boolean isActionComplete) {
        this.message = message;
        this.isActionComplete = isActionComplete;
        this.eventCount = 0;
    }

    public AdminDashUiState(String message, boolean isActionComplete, int eventCount) {
        this.message = message;
        this.isActionComplete = isActionComplete;
        this.eventCount = eventCount;
    }

    public boolean isActionComplete() { return isActionComplete; }

    public String getMessage() {
        return message;
    }

    public int getEventCount() {
        return eventCount;
    }
}
