package com.team_one.soen_345_project.model.repository;

import com.team_one.soen_345_project.model.util.callback.BookedEventsCallback;
import com.team_one.soen_345_project.model.util.callback.ReservationCallback;

public interface IReservationRepository {
    void bookEvent(String eventId, ReservationCallback callback);

    /**
     * Load the set of eventIds already booked by the currently logged-in user.
     */
    void getBookedEventIdsForCurrentUser(BookedEventsCallback callback);

    void isEventBookedByCurrentUser(String eventId, ReservationCallback callback);
}
