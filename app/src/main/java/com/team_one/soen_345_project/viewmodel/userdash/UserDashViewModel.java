package com.team_one.soen_345_project.viewmodel.userdash;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.team_one.soen_345_project.di.Injection;
import com.team_one.soen_345_project.model.entity.Event;
import com.team_one.soen_345_project.model.repository.IEventRepository;
import com.team_one.soen_345_project.model.repository.IReservationRepository;
import com.team_one.soen_345_project.model.util.callback.BookedEventsCallback;
import com.team_one.soen_345_project.model.util.callback.EventListCallback;
import com.team_one.soen_345_project.model.util.filter.CategoryFilterOption;
import com.team_one.soen_345_project.model.util.filter.FilterState;
import com.team_one.soen_345_project.model.util.filter.LocationFilterOption;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UserDashViewModel {

    private final MutableLiveData<UserDashUiState> _uiState =
            new MutableLiveData<>(new UserDashUiState.Builder(null, false).build());

    private final IEventRepository iEventRepository;
    private final IReservationRepository reservationRepository;

    // Cache of all events for filtering
    private List<Event> allEvents = new ArrayList<>();

    public LiveData<UserDashUiState> getUiState() {
        return _uiState;
    }

    // Parametrized constructor for testing use
    public UserDashViewModel(IEventRepository repository) {
        this(repository, null);
    }

    /**
     * Test-friendly constructor that lets tests provide fake repositories.
     */
    public UserDashViewModel(IEventRepository repository, IReservationRepository reservationRepository) {
        this.iEventRepository = repository;
        this.reservationRepository = reservationRepository != null
                ? reservationRepository
                : Injection.provideReservationRepository();
    }

    // Keep the no-arg constructor for production use
    public UserDashViewModel() {
        this.iEventRepository = Injection.provideEventRepository();
        this.reservationRepository = Injection.provideReservationRepository();
    }

    // Load all available events from Firebase
    public void loadAllEvents() {
        iEventRepository.getAllEvents(new EventListCallback() {
            @Override
            public void onEventsReceived(List<Event> events) {
                allEvents = new ArrayList<>(events);
                int totalCount = events.size();
                _uiState.postValue(new UserDashUiState.Builder(null, false)
                        .totalEventCount(totalCount)
                        .events(events)
                        .build());
            }

            @Override
            public void onError(String errorMessage) {
                _uiState.postValue(new UserDashUiState.Builder(errorMessage, false).build());
            }
        });
    }

    // Filter events by title search query
    public void searchEvents(String query) {

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

        UserDashUiState currentState = _uiState.getValue();
        String message = currentState != null ? currentState.getMessage() : null;
        boolean isActionComplete = currentState != null && currentState.isActionComplete();
        int totalCount = allEvents.size();

        _uiState.postValue(new UserDashUiState.Builder(message, isActionComplete)
                    .totalEventCount(totalCount)
                    .events(filteredEvents)
                    .build());
    }

    // Method to apply the filter to the selected list of events
    public void applyFilter(FilterState filterState) {
        UserDashUiState currentState = _uiState.getValue();
        String message = currentState != null ? currentState.getMessage() : null;
        boolean isActionComplete = currentState != null && currentState.isActionComplete();

        _uiState.postValue(new UserDashUiState.Builder(message, isActionComplete)
                .totalEventCount(allEvents.size())
                .filterState(filterState)
                .events(filterEvents(filterState))
                .build());
    }

    // Filter all current events based on a given filter
    public List<Event> filterEvents(FilterState filterState) {
        return allEvents.stream()
                .filter(event ->
                        (filterState.getCategory().equals(CategoryFilterOption.ALL) || event.getCategory_id().equalsIgnoreCase(filterState.getCategory().getId())) &&
                        (filterState.getLocation().equals(LocationFilterOption.ALL)  || event.getLocation_id().equalsIgnoreCase(filterState.getLocation().getId())) &&
                        (filterState.getDateFrom() == null || event.getDate().compareTo(filterState.getDateFrom()) >= 0) &&
                        (filterState.getDateTo() == null || event.getDate().compareTo(filterState.getDateTo()) <= 0) &&
                        (!filterState.isAvailableOnly() || !event.isFull()) &&
                        (filterState.getMinPrice() == null || event.getPrice() >= filterState.getMinPrice()) &&
                        (filterState.getMaxPrice() == null || event.getPrice() <= filterState.getMaxPrice())
                )
                .collect(Collectors.toList());
    }

    /**
     * Loads and displays only the events that the current user has booked.
     * Assumes events are already loaded into allEvents; if not, it will load them first.
     */
    public void loadBookedUpcomingEvents() {
        if (allEvents == null || allEvents.isEmpty()) {
            // Load events first, then filter.
            loadAllEventsAndThenBooked();
            return;
        }

        reservationRepository.getBookedEventIdsForCurrentUser(new BookedEventsCallback() {
            @Override
            public void onResult(Set<String> bookedEventIds) {
                Set<String> booked = bookedEventIds != null ? bookedEventIds : new HashSet<>();
                List<Event> bookedEvents = allEvents.stream()
                        .filter(e -> e != null && e.getEventId() != null && booked.contains(e.getEventId()))
                        .collect(Collectors.toList());

                // Sort chronologically
                bookedEvents.sort((e1, e2) -> {
                    if (e1.getDate() == null || e2.getDate() == null) return 0;
                    return e1.getDate().compareTo(e2.getDate());
                });

                _uiState.postValue(new UserDashUiState.Builder(null, false)
                        .totalEventCount(allEvents.size())
                        .events(bookedEvents)
                        .build());
            }

            @Override
            public void onError(String message) {
                _uiState.postValue(new UserDashUiState.Builder(message, false)
                        .totalEventCount(allEvents != null ? allEvents.size() : 0)
                        .events(new ArrayList<>())
                        .build());
            }
        });
    }

    private void loadAllEventsAndThenBooked() {
        iEventRepository.getAllEvents(new EventListCallback() {
            @Override
            public void onEventsReceived(List<Event> events) {
                allEvents = new ArrayList<>(events);
                loadBookedUpcomingEvents();
            }

            @Override
            public void onError(String errorMessage) {
                _uiState.postValue(new UserDashUiState.Builder(errorMessage, false).build());
            }
        });
    }

    public void cancelBooking(String eventId) {
        reservationRepository.cancelEvent(eventId, (message, success) -> {
            if (success) {
                // Invalidate cache so the cancelled event drops from the booked list
                allEvents.clear();
                loadBookedUpcomingEvents();
            } else {
                UserDashUiState current = _uiState.getValue();
                _uiState.postValue(new UserDashUiState.Builder(message, false)
                        .totalEventCount(current != null ? current.getTotalEventCount() : 0)
                        .events(current != null ? current.getEvents() : new ArrayList<>())
                        .build());
            }
        });
    }
}
