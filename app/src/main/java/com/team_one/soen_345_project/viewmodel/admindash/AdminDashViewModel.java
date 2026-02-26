package com.team_one.soen_345_project.viewmodel.admindash;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.team_one.soen_345_project.di.Injection;
import com.team_one.soen_345_project.model.entity.Event;
import com.team_one.soen_345_project.model.repository.IEventRepository;
import com.team_one.soen_345_project.model.util.callback.EventListCallback;

import java.util.HashMap;
import java.util.List;

public class AdminDashViewModel {
    private final MutableLiveData<AdminDashUiState> _uiState =
            new MutableLiveData<>(new AdminDashUiState(null,false));
    IEventRepository iEventRepository = Injection.provideEventRepository();

    // Method for communicating between the view and the model for saving an event
    public void saveEvent(HashMap<String, String> eventInfo) {
        iEventRepository.saveEvent(eventInfo, (message, isSuccess, isAdmin) -> {
            if (isSuccess) {
                _uiState.postValue(new AdminDashUiState(message, true));
                // Reload events list after successfully adding an event
                loadAllEvents();
            } else {
                _uiState.postValue(new AdminDashUiState(message, false));
            }
        });
    }

    // Method to load all events from Firebase sorted chronologically
    // Also updates the event count based on the list size
    public void loadAllEvents() {
        android.util.Log.d("AdminDashViewModel", "loadAllEvents() called");
        iEventRepository.getAllEvents(new EventListCallback() {
            @Override
            public void onEventsReceived(List<Event> events) {
                android.util.Log.d("AdminDashViewModel", "onEventsReceived: " + events.size() + " events");
                // Preserve existing state values
                AdminDashUiState currentState = _uiState.getValue();
                String message = currentState != null ? currentState.getMessage() : null;
                boolean isActionComplete = currentState != null && currentState.isActionComplete();

                // Set count from events list size
                int eventCount = events.size();
                android.util.Log.d("AdminDashViewModel", "Setting count to: " + eventCount);

                // Update with new events list and count
                _uiState.postValue(new AdminDashUiState(message, isActionComplete, eventCount, events));
            }

            @Override
            public void onError(String errorMessage) {
                android.util.Log.e("AdminDashViewModel", "loadAllEvents error: " + errorMessage);
                // Update UI state with error message
                _uiState.postValue(new AdminDashUiState(errorMessage, false, 0));
            }
        });
    }

    public LiveData<AdminDashUiState> getUiState() {
        return _uiState;
    }
}
