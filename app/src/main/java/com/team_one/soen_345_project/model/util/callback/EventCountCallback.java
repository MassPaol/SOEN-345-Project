package com.team_one.soen_345_project.model.util.callback;

public interface EventCountCallback {
    void onCountReceived(int count);
    void onError(String errorMessage);
}
