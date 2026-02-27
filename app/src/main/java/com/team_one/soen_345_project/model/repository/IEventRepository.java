package com.team_one.soen_345_project.model.repository;

import com.team_one.soen_345_project.model.util.callback.Callback;
import com.team_one.soen_345_project.model.util.callback.DeleteEventCallback;
import com.team_one.soen_345_project.model.util.callback.EventListCallback;
import com.team_one.soen_345_project.model.util.callback.UpdateEventCallback;
import java.util.HashMap;

public interface IEventRepository {
    void saveEvent(HashMap<String, String> eventInfo, Callback callback);
    void getAllEvents(EventListCallback callback);
    void deleteEvent(String eventId, DeleteEventCallback callback);
    void updateEvent(String eventId, HashMap<String, Object> updatedFields, UpdateEventCallback callback);
}
