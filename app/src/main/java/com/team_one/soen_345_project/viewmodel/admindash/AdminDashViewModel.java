package com.team_one.soen_345_project.viewmodel.admindash;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.team_one.soen_345_project.di.Injection;
import com.team_one.soen_345_project.model.entity.Event;
import com.team_one.soen_345_project.model.repository.IEventRepository;
import com.team_one.soen_345_project.model.util.callback.EventListCallback;
import com.team_one.soen_345_project.model.util.filter.CategoryFilterOption;
import com.team_one.soen_345_project.model.util.filter.FilterState;
import com.team_one.soen_345_project.model.util.filter.LocationFilterOption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class AdminDashViewModel {
    private final MutableLiveData<AdminDashUiState> _uiState =
            new MutableLiveData<>(new AdminDashUiState.Builder(null, false).build());

    IEventRepository iEventRepository = Injection.provideEventRepository();
    private List<Event> allEvents = new ArrayList<>();

    public void saveEvent(HashMap<String, String> eventInfo) {
        iEventRepository.saveEvent(eventInfo, (message, isSuccess, isAdmin) -> {
            if (isSuccess) {
                _uiState.postValue(new AdminDashUiState.Builder(message, true).build());
                loadAllEvents();
            } else {
                _uiState.postValue(new AdminDashUiState.Builder(message, false).build());
            }
        });
    }

    public void loadAllEvents() {
        android.util.Log.d("AdminDashViewModel", "loadAllEvents() called");
        iEventRepository.getAllEvents(new EventListCallback() {
            @Override
            public void onEventsReceived(List<Event> events) {
                android.util.Log.d("AdminDashViewModel", "onEventsReceived: " + events.size() + " events");
                allEvents = new ArrayList<>(events);

                AdminDashUiState currentState = _uiState.getValue();
                String message = currentState != null ? currentState.getMessage() : null;
                boolean isActionComplete = currentState != null && currentState.isActionComplete();

                android.util.Log.d("AdminDashViewModel", "Setting count to: " + events.size());
                _uiState.postValue(new AdminDashUiState.Builder(message, isActionComplete)
                        .eventCount(events.size())
                        .events(events)
                        .build());
            }

            @Override
            public void onError(String errorMessage) {
                android.util.Log.e("AdminDashViewModel", "loadAllEvents error: " + errorMessage);
                _uiState.postValue(new AdminDashUiState.Builder(errorMessage, false).build());
            }
        });
    }

    public void searchEvents(String query) {
        android.util.Log.d("AdminDashViewModel", "searchEvents() called with query: " + query);

        List<Event> filteredEvents;
        if (query == null || query.trim().isEmpty()) {
            filteredEvents = new ArrayList<>(allEvents);
            android.util.Log.d("AdminDashViewModel", "Empty query, showing all " + filteredEvents.size() + " events");
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            filteredEvents = new ArrayList<>();
            for (Event event : allEvents) {
                if (event.getTitle() != null && event.getTitle().toLowerCase().contains(lowerCaseQuery)) {
                    filteredEvents.add(event);
                }
            }
            android.util.Log.d("AdminDashViewModel", "Filtered to " + filteredEvents.size() + " events");
        }

        AdminDashUiState currentState = _uiState.getValue();
        String message = currentState != null ? currentState.getMessage() : null;
        boolean isActionComplete = currentState != null && currentState.isActionComplete();

        _uiState.postValue(new AdminDashUiState.Builder(message, isActionComplete)
                .eventCount(allEvents.size())
                .events(filteredEvents)
                .build());
    }

    public void deleteEvent(String eventId) {
        iEventRepository.deleteEvent(eventId, (message, isSuccess) -> {
            if (isSuccess) {
                _uiState.postValue(new AdminDashUiState.Builder(message, true).build());
                loadAllEvents();
            } else {
                _uiState.postValue(new AdminDashUiState.Builder(message, false).build());
            }
        });
    }

    public void updateEvent(String eventId, HashMap<String, Object> updatedFields) {
        iEventRepository.updateEvent(eventId, updatedFields, (message, isSuccess) -> {
            if (isSuccess) {
                _uiState.postValue(new AdminDashUiState.Builder(message, true).build());
                loadAllEvents();
            } else {
                _uiState.postValue(new AdminDashUiState.Builder(message, false).build());
            }
        });
    }

    public void clearActionState() {
        AdminDashUiState currentState = _uiState.getValue();
        if (currentState != null) {
            _uiState.postValue(new AdminDashUiState.Builder(null, false)
                    .eventCount(currentState.getEventCount())
                    .events(currentState.getEvents())
                    .build());
        }
    }

    public void applyFilter(FilterState filterState) {
        AdminDashUiState currentState = _uiState.getValue();
        String message = currentState != null ? currentState.getMessage() : null;
        boolean isActionComplete = currentState != null && currentState.isActionComplete();

        _uiState.postValue(new AdminDashUiState.Builder(message, isActionComplete)
                .eventCount(allEvents.size())
                .events(filterEvents(filterState))
                .build());
    }

    public List<Event> filterEvents(FilterState filterState) {
        return allEvents.stream()
                .filter(event ->
                        (filterState.getCategory().equals(CategoryFilterOption.ALL) || event.getCategory_id().equalsIgnoreCase(filterState.getCategory().getId())) &&
                        (filterState.getLocation().equals(LocationFilterOption.ALL)  || event.getLocation_id().equalsIgnoreCase(filterState.getLocation().getId())) &&
                        (filterState.getDateFrom() == null || event.getDate().compareTo(filterState.getDateFrom()) >= 0) &&
                        (filterState.getDateTo()   == null || event.getDate().compareTo(filterState.getDateTo()) <= 0) &&
                        (!filterState.isAvailableOnly()    || !event.isFull()) &&
                        (filterState.getMinPrice() == null || event.getPrice() >= filterState.getMinPrice()) &&
                        (filterState.getMaxPrice() == null || event.getPrice() <= filterState.getMaxPrice())
                )
                .collect(Collectors.toList());
    }

    public LiveData<AdminDashUiState> getUiState() {
        return _uiState;
    }
}
