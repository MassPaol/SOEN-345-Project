package com.team_one.soen_345_project.model.entity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

/**
 * Firestore model for a reservation/booking.
 */
public class Reservation {

    @DocumentId
    private String reservationId;

    private String userId;
    private String eventId;
    private Timestamp timestamp;
    private String status;

    // Required by Firestore
    public Reservation() {}

    public Reservation(String reservationId, String userId, String eventId, Timestamp timestamp, String status) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

