package com.team_one.soen_345_project.model.util.callback;

import java.util.Set;

public interface BookedEventsCallback {
    void onResult(Set<String> bookedEventIds);
    void onError(String message);
}

