package com.team_one.soen_345_project.viewmodel.admindash;

import java.util.Map;

public class AdminDashUiState {
    private final String message;
    private final boolean isActionComplete;

    public AdminDashUiState(String message, boolean isActionComplete) {
        this.message = message;
        this.isActionComplete = isActionComplete;
    }

    public boolean isActionComplete() { return isActionComplete; }

    public String getMessage() {
        return message;
    }
}
