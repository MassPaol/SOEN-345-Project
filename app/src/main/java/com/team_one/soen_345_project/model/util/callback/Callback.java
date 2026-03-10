package com.team_one.soen_345_project.model.util.callback;

// Simple callback class for easier communication when using async methods
public interface Callback {
    // The "Full" method abstract for lambda uses
    void onResult(String message, boolean isSuccess, boolean isAdmin);

    // The "Default" method
    // If someone calls this, it automatically passes '0' as the error code
    default void onResult(String message, boolean isSuccess) {
        onResult(message, isSuccess, false);
    }
}