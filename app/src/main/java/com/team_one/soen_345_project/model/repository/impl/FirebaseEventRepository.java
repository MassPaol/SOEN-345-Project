package com.team_one.soen_345_project.model.repository.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.team_one.soen_345_project.model.entity.Event;
import com.team_one.soen_345_project.model.repository.IEventRepository;
import com.team_one.soen_345_project.model.util.callback.Callback;
import com.team_one.soen_345_project.model.util.callback.EventListCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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


    // Get all events from Firestore sorted by date (chronological order)
    @Override
    public void getAllEvents(EventListCallback callback) {
        android.util.Log.d("FirebaseEventRepo", "getAllEvents() called");
        firestore.collection("event")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    android.util.Log.d("FirebaseEventRepo", "Query successful, fetched " + querySnapshot.size() + " documents");
                    List<Event> events = new ArrayList<>();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        try {
                            Event event = document.toObject(Event.class);
                            events.add(event);
                            android.util.Log.d("FirebaseEventRepo", "Parsed event: " + event.getTitle());
                        } catch (Exception e) {
                            android.util.Log.e("FirebaseEventRepo", "Failed to parse event: " + document.getId(), e);
                            // Skip invalid events
                        }
                    }

                    // Sort events by date (chronological order - earliest first)
                    events.sort((e1, e2) -> {
                        if (e1.getDate() == null || e2.getDate() == null) {
                            return 0;
                        }
                        return e1.getDate().compareTo(e2.getDate());
                    });

                    android.util.Log.d("FirebaseEventRepo", "Returning " + events.size() + " valid events");
                    callback.onEventsReceived(events);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("FirebaseEventRepo", "Failed to get all events", e);
                    callback.onError("Failed to fetch events: " + e.getMessage());
                });
    }
}
