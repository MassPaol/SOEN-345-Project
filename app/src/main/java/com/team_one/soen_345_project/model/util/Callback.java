package com.team_one.soen_345_project.model.util;

// Simple callback class for easier communication when using async methods
public interface Callback {
    void onResult(String message, boolean isSuccess);
}
