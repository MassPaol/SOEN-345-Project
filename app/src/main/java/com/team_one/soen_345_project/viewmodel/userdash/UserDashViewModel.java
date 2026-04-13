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
    // Source list currently used by search/filter in this screen.
    private List<Event> visibleSourceEvents = new ArrayList<>();
    // Cache of booked events shown in the user dashboard.
    private List<Event> bookedEvents = new ArrayList<>();

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
                visibleSourceEvents = new ArrayList<>(allEvents);
                bookedEvents = new ArrayList<>();
                int totalCount = events.size();
                _uiState.postValue(new UserDashUiState.Builder(null, false)
                        .totalEventCount(totalCount)
                        .events(visibleSourceEvents)
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
        List<Event> sourceEvents = visibleSourceEvents != null
                ? visibleSourceEvents
                : new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            filteredEvents = new ArrayList<>(sourceEvents);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            filteredEvents = new ArrayList<>();
            for (Event event : sourceEvents) {
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
                .events(filterEvents(filterState, visibleSourceEvents))
                .build());
    }

    // Filter all current events based on a given filter
    public List<Event> filterEvents(FilterState filterState) {
        return filterEvents(filterState, allEvents);
    }

    private List<Event> filterEvents(FilterState filterState, List<Event> sourceEvents) {
        FilterState safeFilter = filterState != null ? filterState : new FilterState();
        List<Event> safeSource = sourceEvents != null ? sourceEvents : new ArrayList<>();

        return safeSource.stream()
                .filter(event ->
                        (safeFilter.getCategory().equals(CategoryFilterOption.ALL) || event.getCategory_id().equalsIgnoreCase(safeFilter.getCategory().getId())) &&
                        (safeFilter.getLocation().equals(LocationFilterOption.ALL)  || event.getLocation_id().equalsIgnoreCase(safeFilter.getLocation().getId())) &&
                        (safeFilter.getDateFrom() == null || event.getDate().compareTo(safeFilter.getDateFrom()) >= 0) &&
                        (safeFilter.getDateTo() == null || event.getDate().compareTo(safeFilter.getDateTo()) <= 0) &&
                        (!safeFilter.isAvailableOnly() || !event.isFull()) &&
                        (safeFilter.getMinPrice() == null || event.getPrice() >= safeFilter.getMinPrice()) &&
                        (safeFilter.getMaxPrice() == null || event.getPrice() <= safeFilter.getMaxPrice())
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
                List<Event> matchedBookedEvents = allEvents.stream()
                        .filter(e -> e != null && e.getEventId() != null && booked.contains(e.getEventId()))
                        .collect(Collectors.toList());

                // Sort chronologically
                matchedBookedEvents.sort((e1, e2) -> {
                    if (e1.getDate() == null || e2.getDate() == null) return 0;
                    return e1.getDate().compareTo(e2.getDate());
                });

                bookedEvents = new ArrayList<>(matchedBookedEvents);
                visibleSourceEvents = new ArrayList<>(bookedEvents);

                _uiState.postValue(new UserDashUiState.Builder(null, false)
                        .totalEventCount(allEvents.size())
                        .events(visibleSourceEvents)
                        .build());
            }

            @Override
            public void onError(String message) {
                bookedEvents = new ArrayList<>();
                visibleSourceEvents = new ArrayList<>();
                _uiState.postValue(new UserDashUiState.Builder(message, false)
                        .totalEventCount(allEvents != null ? allEvents.size() : 0)
                        .events(new ArrayList<>())
                        .build());
            }
        });
    }

    /**
     * Forces a fresh events fetch before computing booked upcoming events.
     * Use this when returning from other screens that may have changed booking state.
     */
    public void refreshBookedUpcomingEvents() {
        loadAllEventsAndThenBooked();
    }

    private void loadAllEventsAndThenBooked() {
        iEventRepository.getAllEvents(new EventListCallback() {
            @Override
            public void onEventsReceived(List<Event> events) {
                allEvents = new ArrayList<>(events);
                visibleSourceEvents = new ArrayList<>(allEvents);
                loadBookedUpcomingEvents();
            }

            @Override
            public void onError(String errorMessage) {
                _uiState.postValue(new UserDashUiState.Builder(errorMessage, false).build());
            }
        });
    }

    public void cancelBooking(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            UserDashUiState current = _uiState.getValue();
            _uiState.postValue(new UserDashUiState.Builder("Invalid event", false)
                    .totalEventCount(current != null ? current.getTotalEventCount() : 0)
                    .events(current != null ? current.getEvents() : new ArrayList<>())
                    .build());
            return;
        }

        reservationRepository.cancelEvent(eventId, (message, success) -> {
            if (success) {
                postLocalCancellationState(eventId, message);
            } else {
                UserDashUiState current = _uiState.getValue();
                _uiState.postValue(new UserDashUiState.Builder(message, false)
                        .totalEventCount(current != null ? current.getTotalEventCount() : 0)
                        .events(current != null ? current.getEvents() : new ArrayList<>())
                        .build());
            }
        });
    }

    public void onReservationCancelledLocally(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            return;
        }
        postLocalCancellationState(eventId, "Reservation cancelled successfully");
    }

    private void postLocalCancellationState(String eventId, String message) {
        // Remove only from the currently displayed booked list.
        // Keep allEvents cache intact because it represents all available events.

        bookedEvents.removeIf(e -> e != null && eventId.equals(e.getEventId()));
        visibleSourceEvents.removeIf(e -> e != null && eventId.equals(e.getEventId()));

        UserDashUiState current = _uiState.getValue();
        List<Event> currentEvents = current != null && current.getEvents() != null
                ? new ArrayList<>(current.getEvents())
                : new ArrayList<>();
        currentEvents.removeIf(e -> e != null && eventId.equals(e.getEventId()));

        int totalCount = allEvents != null && !allEvents.isEmpty()
            ? allEvents.size()
            : (current != null ? current.getTotalEventCount() : currentEvents.size());

        _uiState.postValue(new UserDashUiState.Builder(
                message != null ? message : "Reservation cancelled successfully",
                true)
                .totalEventCount(totalCount)
                .events(currentEvents)
                .build());
    }
}
