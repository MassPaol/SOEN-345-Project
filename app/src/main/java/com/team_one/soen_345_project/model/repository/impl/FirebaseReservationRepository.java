package com.team_one.soen_345_project.model.repository.impl;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import com.team_one.soen_345_project.model.entity.Event;
import com.team_one.soen_345_project.model.repository.IAuthRepository;
import com.team_one.soen_345_project.model.repository.IReservationRepository;
import com.team_one.soen_345_project.model.util.callback.BookedEventsCallback;
import com.team_one.soen_345_project.model.util.callback.ReservationCallback;
import com.team_one.soen_345_project.model.util.EmailHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FirebaseReservationRepository implements IReservationRepository {

    private static final String COLLECTION_EVENT = "event";
    private static final String COLLECTION_RESERVATION = "reservation";

    private final IAuthRepository authRepository;
    private final FirebaseFirestore firestore;

    public FirebaseReservationRepository() {
        this.authRepository = new FirebaseAuthRepository();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public FirebaseReservationRepository(IAuthRepository authRepository, FirebaseFirestore firestore) {
        this.authRepository = authRepository;
        this.firestore = firestore;
    }

    @Override
    public void bookEvent(String eventId, ReservationCallback callback) {
        String uid = authRepository.getCurrentUserUid();
        if (uid == null) {
            callback.onResult("No user logged in", false);
            return;
        }
        if (eventId == null || eventId.trim().isEmpty()) {
            callback.onResult("Invalid event", false);
            return;
        }

        // If the user already booked the event, this doc will already exist.
        String reservationId = uid + "_" + eventId;

        DocumentReference eventRef = firestore.collection(COLLECTION_EVENT).document(eventId);
        DocumentReference reservationRef = firestore.collection(COLLECTION_RESERVATION).document(reservationId);

        firestore.runTransaction((Transaction.Function<Event>) transaction -> {
            // Prevent duplicates
            DocumentSnapshot existingReservation = transaction.get(reservationRef);
            if (existingReservation.exists()) {
                throw new IllegalStateException("Already booked");
            }

            //  Check capacity
            DocumentSnapshot snapshot = transaction.get(eventRef);
            Event event = snapshot.toObject(Event.class);
            if (event == null) {
                throw new IllegalStateException("Event not found");
            }
            if (event.getEventId() == null) {
                event.setEventId(snapshot.getId());
            }

            int capacity = event.getCapacity();
            int reservations = event.getReservations();
            int available = capacity - reservations;

            if (available <= 0) {
                throw new IllegalStateException("No spots left");
            }

            int newReservations = reservations + 1;
            boolean isNowFull = newReservations >= capacity;

            // Increment reservations & update full flag
            transaction.update(eventRef, "reservations", newReservations);
            transaction.update(eventRef, "full", isNowFull);

            // Create reservation doc
            Map<String, Object> reservationData = new HashMap<>();
            reservationData.put("reservationId", reservationId);
            reservationData.put("userId", uid);
            reservationData.put("eventId", eventId);
            reservationData.put("timestamp", Timestamp.now());
            reservationData.put("status", "CONFIRMED");

            transaction.set(reservationRef, reservationData);
            return event;
        }).addOnSuccessListener(event -> {
            String email = authRepository.getCurrentUserEmail();
            if (email != null) {
                EmailHelper.sendEventStatusEmail(email, event, false);
            }
            callback.onResult("Event booked successfully", true);
        })
          .addOnFailureListener(e -> {
              String msg = (e.getMessage() != null) ? e.getMessage() : "Failed to book event";
              String lower = msg.toLowerCase();

              if (lower.contains("already booked")) {
                  callback.onResult("You already booked this event", false);
                  return;
              }
              if (lower.contains("no spots") || lower.contains("no spot") || lower.contains("no space")) {
                  callback.onResult("No spots left", false);
                  return;
              }
              callback.onResult(msg, false);
          });
    }

    @Override
    public void getBookedEventIdsForCurrentUser(BookedEventsCallback callback) {
        String uid = authRepository.getCurrentUserUid();
        if (uid == null) {
            callback.onError("No user logged in");
            return;
        }

        firestore.collection(COLLECTION_RESERVATION)
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Set<String> booked = new HashSet<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String eventId = doc.getString("eventId");
                        if (eventId != null) booked.add(eventId);
                    }
                    callback.onResult(booked);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage() != null ? e.getMessage() : "Failed to load reservations"));
    }

    @Override
    public void cancelEvent(String eventId, ReservationCallback callback) {
        String uid = authRepository.getCurrentUserUid();
        if (uid == null) {
            callback.onResult("No user logged in", false);
            return;
        }
        if (eventId == null || eventId.trim().isEmpty()) {
            callback.onResult("Invalid event", false);
            return;
        }

        String reservationId = uid + "_" + eventId;

        DocumentReference eventRef       = firestore.collection(COLLECTION_EVENT).document(eventId);
        DocumentReference reservationRef = firestore.collection(COLLECTION_RESERVATION).document(reservationId);

        firestore.runTransaction((Transaction.Function<Event>) transaction -> {
                    // Confirm reservation exists
                    DocumentSnapshot reservationSnap = transaction.get(reservationRef);
                    if (!reservationSnap.exists()) {
                        throw new IllegalStateException("No booking found");
                    }

                    // Read current event state
                    DocumentSnapshot eventSnap = transaction.get(eventRef);
                    Event event = eventSnap.toObject(Event.class);
                    if (event == null) {
                        throw new IllegalStateException("Event not found");
                    }
                    if (event.getEventId() == null) {
                        event.setEventId(eventSnap.getId());
                    }

                    int newReservations = Math.max(0, event.getReservations() - 1);

                    transaction.update(eventRef, "reservations", newReservations);
                    transaction.update(eventRef, "full", false);   // can never be full after a cancellation
                    transaction.delete(reservationRef);

                    return event;
                })
                .addOnSuccessListener(event -> {
                    String email = authRepository.getCurrentUserEmail();
                    if (email != null) {
                        EmailHelper.sendEventStatusEmail(email, event, true);
                    }
                    callback.onResult("Reservation cancelled successfully", true);
                })
                .addOnFailureListener(e -> {
                    String msg = e.getMessage() != null ? e.getMessage() : "Failed to cancel reservation";
                    if (msg.toLowerCase().contains("no booking")) {
                        callback.onResult("You have not booked this event", false);
                    } else {
                        callback.onResult(msg, false);
                    }
                });
    }

    @Override
    public void isEventBookedByCurrentUser(String eventId, ReservationCallback callback) {
        getBookedEventIdsForCurrentUser(new BookedEventsCallback() {
            @Override
            public void onResult(Set<String> bookedEventIds) {
                boolean booked = bookedEventIds != null && bookedEventIds.contains(eventId);
                callback.onResult(booked ? "BOOKED" : "NOT_BOOKED", booked);
            }

            @Override
            public void onError(String message) {
                callback.onResult(message != null ? message : "Failed", false);
            }
        });
    }

    @Override
    public void deleteAllReservationsForEvent(String eventId, ReservationCallback callback) {
        if (eventId == null || eventId.trim().isEmpty()) {
            callback.onResult("Invalid event id", false);
            return;
        }

        // Fetch event details first so we can include them in the cancellation emails
        firestore.collection(COLLECTION_EVENT).document(eventId).get().addOnSuccessListener(eventDoc -> {
            Event event = eventDoc.exists() ? eventDoc.toObject(Event.class) : null;

            firestore.collection(COLLECTION_RESERVATION)
                    .whereEqualTo("eventId", eventId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (querySnapshot.isEmpty()) {
                            // No reservations exist, so consider it a success
                            callback.onResult("No reservations to delete.", true);
                            return;
                        }

                        // Use a WriteBatch wrapper or loop to delete
                        com.google.firebase.firestore.WriteBatch batch = firestore.batch();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {

                            // Send an email to the user whose reservation is being cancelled
                            String userId = doc.getString("userId");
                            if (userId != null && event != null) {
                                firestore.collection("user").document(userId).get().addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        String email = userDoc.getString("email");
                                        if (email != null && !email.isEmpty()) {
                                            EmailHelper.sendEventStatusEmail(email, event, true);
                                        }
                                    }
                                });
                            }

                            batch.delete(doc.getReference());
                        }

                        batch.commit()
                            .addOnSuccessListener(aVoid -> callback.onResult("Deleted all associated reservations.", true))
                            .addOnFailureListener(e -> callback.onResult("Failed to delete reservations", false));
                    })
                    .addOnFailureListener(e -> callback.onResult("Failed to query reservations: " + e.getMessage(), false));
        }).addOnFailureListener(e -> {
            // Fallback: If we couldn't fetch event data, we should probably still try to clean up the DB
            callback.onResult("Failed to fetch event data for email cancellation.", false);

        });
    }
}
