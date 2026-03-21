package com.team_one.soen_345_project.model.repository;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.team_one.soen_345_project.model.entity.Event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IFirestoreSource {
    // Returns true if user exists and isAdmin == true, false otherwise
    void isUserAdmin(String uid,
                     OnSuccessListener<Boolean> onSuccess,
                     OnFailureListener onFailure);

    void addEvent(Map<String, Object> eventData,
                  OnSuccessListener<Void> onSuccess,
                  OnFailureListener onFailure);

    void deleteEvent(String eventId,
                     OnSuccessListener<Void> onSuccess,
                     OnFailureListener onFailure);

    void updateEvent(String eventId,
                     HashMap<String, Object> fields,
                     OnSuccessListener<Void> onSuccess,
                     OnFailureListener onFailure);

    public void getAllEvents(OnSuccessListener<List<Event>> onSuccess,
                             OnFailureListener onFailure);
}