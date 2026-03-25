package com.team_one.soen_345_project.viewmodel.allevents;

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
import java.util.List;
import java.util.stream.Collectors;

public class AllEventsViewModel {

    private final MutableLiveData<AllEventsUiState> _uiState =
            new MutableLiveData<>(new AllEventsUiState.Builder(null).build());

    private final IEventRepository eventRepository;

    // Cache of all events for filtering/search
    private List<Event> allEvents = new ArrayList<>();

    public AllEventsViewModel() {
        this.eventRepository = Injection.provideEventRepository();
    }

    public AllEventsViewModel(IEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public LiveData<AllEventsUiState> getUiState() {
        return _uiState;
    }

    public void loadAllEvents() {
        eventRepository.getAllEvents(new EventListCallback() {
            @Override
            public void onEventsReceived(List<Event> events) {
                allEvents = events != null ? new ArrayList<>(events) : new ArrayList<>();
                _uiState.postValue(new AllEventsUiState.Builder(null)
                        .events(allEvents)
                        .filterState(new FilterState())
                        .build());
            }

            @Override
            public void onError(String errorMessage) {
                _uiState.postValue(new AllEventsUiState.Builder(errorMessage)
                        .events(new ArrayList<>())
                        .build());
            }
        });
    }

    public void searchEvents(String query) {
        List<Event> filtered;
        if (query == null || query.trim().isEmpty()) {
            filtered = new ArrayList<>(allEvents);
        } else {
            String q = query.toLowerCase().trim();
            filtered = new ArrayList<>();
            for (Event e : allEvents) {
                if (e != null && e.getTitle() != null && e.getTitle().toLowerCase().contains(q)) {
                    filtered.add(e);
                }
            }
        }

        AllEventsUiState current = _uiState.getValue();
        FilterState filterState = current != null ? current.getFilterState() : new FilterState();

        _uiState.postValue(new AllEventsUiState.Builder(null)
                .filterState(filterState)
                .events(filtered)
                .build());
    }

    public void applyFilter(FilterState filterState) {
        if (filterState == null) filterState = new FilterState();
        List<Event> filtered = filterEvents(filterState);
        _uiState.postValue(new AllEventsUiState.Builder(null)
                .filterState(filterState)
                .events(filtered)
                .build());
    }

    private List<Event> filterEvents(FilterState filterState) {
        return allEvents.stream()
                .filter(event -> event != null)
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
}

