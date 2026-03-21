package com.team_one.soen_345_project.model.repository.impl;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.team_one.soen_345_project.model.entity.Event;
import com.team_one.soen_345_project.model.repository.IFirestoreSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreSource implements IFirestoreSource {
    private final FirebaseFirestore db;

    public FirestoreSource(FirebaseFirestore db) {
        this.db = db;
    }

    @Override
    public void isUserAdmin(String uid, OnSuccessListener<Boolean> onSuccess,
                            OnFailureListener onFailure) {
        db.collection("user").document(uid).get()
                .addOnSuccessListener(snapshot ->
                        onSuccess.onSuccess(snapshot.exists() &&
                                Boolean.TRUE.equals(snapshot.getBoolean("isAdmin"))))
                .addOnFailureListener(onFailure);
    }

    @Override
    public void addEvent(Map<String, Object> eventData, OnSuccessListener<Void> onSuccess,
                         OnFailureListener onFailure) {
        db.collection("event").add(eventData)
                .addOnSuccessListener(ref -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }

    @Override
    public void deleteEvent(String eventId, OnSuccessListener<Void> onSuccess,
                            OnFailureListener onFailure) {
        db.collection("event").document(eventId).delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    @Override
    public void updateEvent(String eventId, HashMap<String, Object> fields,
                            OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("event").document(eventId).update(fields)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    @Override
    public void getAllEvents(OnSuccessListener<List<Event>> onSuccess,
                             OnFailureListener onFailure) {
        db.collection("event").get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        try {
                            events.add(doc.toObject(Event.class));
                        } catch (Exception e) {
                            android.util.Log.e("FirestoreSource",
                                    "Failed to parse event: " + doc.getId(), e);
                            // Skip invalid events
                        }
                    }
                    onSuccess.onSuccess(events);
                })
                .addOnFailureListener(onFailure);
    }
}