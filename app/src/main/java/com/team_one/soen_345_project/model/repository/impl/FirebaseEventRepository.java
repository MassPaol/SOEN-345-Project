package com.team_one.soen_345_project.model.repository.impl;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.team_one.soen_345_project.model.entity.Event;
import com.team_one.soen_345_project.model.repository.IAuthRepository;
import com.team_one.soen_345_project.model.repository.IEventRepository;
import com.team_one.soen_345_project.model.repository.IFirestoreSource;
import com.team_one.soen_345_project.model.util.callback.Callback;
import com.team_one.soen_345_project.model.util.callback.DeleteEventCallback;
import com.team_one.soen_345_project.model.util.callback.EventListCallback;
import com.team_one.soen_345_project.model.util.callback.UpdateEventCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FirebaseEventRepository implements IEventRepository {
    IAuthRepository authRepository;
    IFirestoreSource firestoreSource;

    // Default constructor for production use
    public FirebaseEventRepository() {
        this.authRepository = new FirebaseAuthRepository();
        this.firestoreSource = new FirestoreSource(FirebaseFirestore.getInstance());
    }

    // Constructor for testing with dependency injection
    public FirebaseEventRepository(IAuthRepository authRepository, IFirestoreSource firestoreSource) {
        this.authRepository = authRepository;
        this.firestoreSource = firestoreSource;
    }

    // Save event to firestore
    public void saveEvent(HashMap<String, String> eventInfo, Callback callback) {
        String uid = authRepository.getCurrentUserUid();
        if (uid == null) {
            callback.onResult("No user logged in", false);
            return;
        }

        // Backup check ensuring that user is an admin
        firestoreSource.isUserAdmin(uid,
                isAdmin -> {
                    if (isAdmin) {
                        HashMap<String, Object> data = new HashMap<>(eventInfo);
                        firestoreSource.addEvent(data,
                                unused -> callback.onResult("Event added successfully", true),
                                e      -> callback.onResult("Failed to add event: " + e.getMessage(), false)
                        );
                    } else {
                        callback.onResult("User does not exist or not an Admin", false);
                    }
                },
                e -> callback.onResult("Failed to verify user: " + e.getMessage(), false)
        );
    }

    // Get all events from Firestore sorted by date (chronological order)
    @Override
    public void getAllEvents(EventListCallback callback) {
        android.util.Log.d("FirebaseEventRepo", "getAllEvents() called");
        firestoreSource.getAllEvents(
                events -> {
                    // Sort events by date (chronological order - earliest first)
                    events.sort((e1, e2) -> {
                        if (e1.getDate() == null || e2.getDate() == null) {
                            return 0;
                        }
                        return e1.getDate().compareTo(e2.getDate());
                    });
                    android.util.Log.d("FirebaseEventRepo", "Returning " + events.size() + " valid events");
                    callback.onEventsReceived(events);
                },
                e -> {
                    android.util.Log.e("FirebaseEventRepo", "Failed to get all events", e);
                    callback.onError("Failed to fetch events: " + e.getMessage());
                }
        );
    }

    @Override
    public void deleteEvent(String eventId, DeleteEventCallback callback) {
        String uid = authRepository.getCurrentUserUid();
        if (uid == null) {
            callback.onResult("No user logged in", false);
            return;
        }

        // Backup check ensuring that user is an admin
        firestoreSource.isUserAdmin(uid,
                isAdmin -> {
                    if (isAdmin) {
                        firestoreSource.deleteEvent(eventId,
                                aVoid -> callback.onResult("Event deleted successfully", true),
                                e     -> callback.onResult("Failed to delete event: " + e.getMessage(), false)
                        );
                    } else {
                        callback.onResult("User does not exist or not an Admin", false);
                    }
                },
                e -> callback.onResult("Failed to verify user: " + e.getMessage(), false)
        );
    }

    @Override
    public void updateEvent(String eventId, HashMap<String, Object> updatedFields, UpdateEventCallback callback) {
        String uid = authRepository.getCurrentUserUid();
        if (uid == null) {
            callback.onResult("No user logged in", false);
            return;
        }

        // Backup check ensuring that user is an admin
        firestoreSource.isUserAdmin(uid,
                isAdmin -> {
                    if (isAdmin) {
                        // Update the event document with the new fields
                        firestoreSource.updateEvent(eventId, updatedFields,
                                aVoid -> callback.onResult("Event updated successfully", true),
                                e     -> callback.onResult("Failed to update event: " + e.getMessage(), false)
                        );
                    } else {
                        callback.onResult("User does not exist or not an Admin", false);
                    }
                },
                e -> callback.onResult("Failed to verify admin status: " + e.getMessage(), false)
        );
    }
}