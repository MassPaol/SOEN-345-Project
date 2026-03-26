package com.team_one.soen_345_project.viewmodel.reserve;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.team_one.soen_345_project.di.Injection;
import com.team_one.soen_345_project.model.repository.IReservationRepository;
import com.team_one.soen_345_project.model.util.callback.ReservationCallback;

public class ReserveEventViewModel {

    private final MutableLiveData<ReserveEventUiState> _uiState = new MutableLiveData<>(new ReserveEventUiState.Builder().build());
    private final IReservationRepository reservationRepository;

    public ReserveEventViewModel() {
        this(Injection.provideReservationRepository());
    }

    public ReserveEventViewModel(IReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public LiveData<ReserveEventUiState> getUiState() {
        return _uiState;
    }

    public void checkBookingStatus(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) return;

        reservationRepository.isEventBookedByCurrentUser(eventId, (message, booked) -> {
            _uiState.postValue(new ReserveEventUiState.Builder()
                    .isAlreadyBooked(booked)
                    .build());
        });
    }

    public void bookEvent(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            _uiState.postValue(new ReserveEventUiState.Builder()
                    .message("Invalid event")
                    .isSuccess(false)
                    .build());
            return;
        }

        reservationRepository.bookEvent(eventId, (message, success) -> {
            ReserveEventUiState currentState = _uiState.getValue();
            boolean stillBooked = currentState != null && currentState.isAlreadyBooked();
            
            _uiState.postValue(new ReserveEventUiState.Builder()
                    .message(message != null ? message : (success ? "Event booked successfully" : "Failed to book event"))
                    .isSuccess(success)
                    .isAlreadyBooked(success || stillBooked)
                    .build());
        });
    }

    public void cancelBooking(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            _uiState.postValue(new ReserveEventUiState.Builder()
                    .message("Invalid event")
                    .isSuccess(false)
                    .build());
            return;
        }

        reservationRepository.cancelEvent(eventId, (message, success) -> {
            _uiState.postValue(new ReserveEventUiState.Builder()
                    .message(message != null ? message : (success ? "Reservation cancelled" : "Failed to cancel reservation"))
                    .isSuccess(success)
                    .isAlreadyBooked(!success) // true if failed, so still booked
                    .build());
        });
    }
}
