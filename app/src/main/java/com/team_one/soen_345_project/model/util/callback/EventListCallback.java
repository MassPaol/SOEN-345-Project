package com.team_one.soen_345_project.model.util.callback;

import com.team_one.soen_345_project.model.entity.Event;

import java.util.List;

public interface EventListCallback {
    void onEventsReceived(List<Event> events);
    void onError(String errorMessage);
}

