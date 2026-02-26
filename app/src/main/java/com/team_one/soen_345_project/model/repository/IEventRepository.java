package com.team_one.soen_345_project.model.repository;

import com.team_one.soen_345_project.model.util.callback.Callback;
import com.team_one.soen_345_project.model.util.callback.EventListCallback;

import java.util.HashMap;

public interface IEventRepository {
    void saveEvent(HashMap<String, String> eventInfo, Callback callback);
    void getAllEvents(EventListCallback callback);
}
