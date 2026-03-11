package com.team_one.soen_345_project.viewmodel.userdash;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.team_one.soen_345_project.di.Injection;
import com.team_one.soen_345_project.model.entity.Event;
import com.team_one.soen_345_project.model.repository.IEventRepository;
import com.team_one.soen_345_project.model.util.callback.EventListCallback;

import java.util.ArrayList;
import java.util.List;

public class UserDashViewModel {
    private static final String TAG = "UserDashViewModel";

    private final MutableLiveData<UserDashUiState> _uiState =
            new MutableLiveData<>(new UserDashUiState.Builder(null, false).build());

    private final IEventRepository iEventRepository = Injection.provideEventRepository();

    // Cache of all events for filtering
    private List<Event> allEvents = new ArrayList<>();

    public LiveData<UserDashUiState> getUiState() {
        return _uiState;
    }

    // Load all available events from Firebase
    public void loadAllEvents() {
        android.util.Log.d(TAG, "loadAllEvents() called");
        iEventRepository.getAllEvents(new EventListCallback() {
            @Override
            public void onEventsReceived(List<Event> events) {
                android.util.Log.d(TAG, "onEventsReceived: " + events.size() + " events");
                allEvents = new ArrayList<>(events);
                int totalCount = events.size();
                _uiState.postValue(new UserDashUiState.Builder(null, false)
                        .totalEventCount(totalCount)
                        .events(events)
                        .build());
            }

            @Override
            public void onError(String errorMessage) {
                android.util.Log.e(TAG, "loadAllEvents error: " + errorMessage);
                _uiState.postValue(new UserDashUiState.Builder(errorMessage, false).build());
            }
        });
    }

    // Filter events by title search query
    public void searchEvents(String query) {
        android.util.Log.d(TAG, "searchEvents() called with query: " + query);

        List<Event> filteredEvents;

        if (query == null || query.trim().isEmpty()) {
            filteredEvents = new ArrayList<>(allEvents);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            filteredEvents = new ArrayList<>();
            for (Event event : allEvents) {
                if (event.getTitle() != null &&
                        event.getTitle().toLowerCase().contains(lowerCaseQuery)) {
                    filteredEvents.add(event);
                }
            }
        }

        android.util.Log.d(TAG, "Filtered to " + filteredEvents.size() + " events");

        UserDashUiState currentState = _uiState.getValue();
        String message = currentState != null ? currentState.getMessage() : null;
        boolean isActionComplete = currentState != null && currentState.isActionComplete();
        int totalCount = allEvents.size();

        _uiState.postValue(new UserDashUiState.Builder(message, isActionComplete)
                    .totalEventCount(totalCount)
                    .events(filteredEvents)
                    .build());
    }
}

