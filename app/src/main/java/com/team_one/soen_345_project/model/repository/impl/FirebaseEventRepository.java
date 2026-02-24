package com.team_one.soen_345_project.model.repository.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.team_one.soen_345_project.model.entity.Event;
import com.team_one.soen_345_project.model.repository.IEventRepository;
import com.team_one.soen_345_project.model.util.Callback;

import java.util.HashMap;

public class FirebaseEventRepository implements IEventRepository {
    // Get the auth singleton for db authentication
    FirebaseAuth auth = FirebaseAuth.getInstance();

    // Get the firestore singleton for db interaction
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

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
}
