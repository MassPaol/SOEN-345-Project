package com.team_one.soen_345_project.model.repository.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.team_one.soen_345_project.model.entity.Event;
import com.team_one.soen_345_project.model.repository.IEventRepository;
import com.team_one.soen_345_project.model.util.callback.Callback;
import com.team_one.soen_345_project.model.util.callback.EventCountCallback;

import java.util.HashMap;

public class FirebaseEventRepository implements IEventRepository {
    FirebaseAuth auth;
    FirebaseFirestore firestore;

    // Default constructor for production use
    public FirebaseEventRepository() {
        // Get the auth singleton for db authentication
        this.auth = FirebaseAuth.getInstance();
        // Get the firestore singleton for db interaction
        this.firestore = FirebaseFirestore.getInstance();
    }

    // Constructor for testing with dependency injection
    public FirebaseEventRepository(FirebaseAuth auth, FirebaseFirestore firestore) {
        this.auth = auth;
        this.firestore = firestore;
    }

    // Save event to firestore
    public void saveEvent(HashMap<String, String> eventInfo, Callback callback) {
        String uid = auth.getCurrentUser().getUid();

        // Backup check ensuring that user is an admin
        firestore.collection("user").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && Boolean.TRUE.equals(documentSnapshot.getBoolean("isAdmin"))) {
                firestore.collection("event").add(new Event(eventInfo));

                // Update callback for communication with ViewModel
                callback.onResult("Event added successfully", true);
            } else {
                callback.onResult("User does not exist or not an Admin", false);
            }
        });
    }

    // Get the count of all events in the event collection
    @Override
    public void getEventCount(EventCountCallback callback) {
        firestore.collection("event")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    callback.onCountReceived(count);
                })
                .addOnFailureListener(e -> {
                    callback.onError("Failed to fetch event count: " + e.getMessage());
                });
    }
}
