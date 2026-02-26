package com.team_one.soen_345_project.viewmodel.admindash;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.team_one.soen_345_project.di.Injection;
import com.team_one.soen_345_project.model.entity.Event;
import com.team_one.soen_345_project.model.repository.IEventRepository;
import com.team_one.soen_345_project.model.util.callback.EventListCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdminDashViewModel {
    private final MutableLiveData<AdminDashUiState> _uiState =
            new MutableLiveData<>(new AdminDashUiState(null,false));
    IEventRepository iEventRepository = Injection.provideEventRepository();

    // Store all events for filtering
    private List<Event> allEvents = new ArrayList<>();

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

                // Store all events for filtering
                allEvents = new ArrayList<>(events);

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

    // Method to filter events by title search query
    public void searchEvents(String query) {
        android.util.Log.d("AdminDashViewModel", "searchEvents() called with query: " + query);

        List<Event> filteredEvents;

        if (query == null || query.trim().isEmpty()) {
            // Show all events if search is empty
            filteredEvents = new ArrayList<>(allEvents);
            android.util.Log.d("AdminDashViewModel", "Empty query, showing all " + filteredEvents.size() + " events");
        } else {
            // Filter events by title (case-insensitive)
            String lowerCaseQuery = query.toLowerCase().trim();
            filteredEvents = new ArrayList<>();

            for (Event event : allEvents) {
                if (event.getTitle() != null &&
                    event.getTitle().toLowerCase().contains(lowerCaseQuery)) {
                    filteredEvents.add(event);
                }
            }

            android.util.Log.d("AdminDashViewModel", "Filtered to " + filteredEvents.size() + " events");
        }

        // Update UI state with filtered events
        // Keep total count as all events, but show filtered list
        AdminDashUiState currentState = _uiState.getValue();
        String message = currentState != null ? currentState.getMessage() : null;
        boolean isActionComplete = currentState != null && currentState.isActionComplete();
        int totalCount = allEvents.size(); // Keep showing total count

        _uiState.postValue(new AdminDashUiState(message, isActionComplete, totalCount, filteredEvents));
    }

    public LiveData<AdminDashUiState> getUiState() {
        return _uiState;
    }
}
