package com.team_one.soen_345_project.viewmodel.allevents;

import com.team_one.soen_345_project.model.entity.Event;
import com.team_one.soen_345_project.model.util.filter.FilterState;

import java.util.ArrayList;
import java.util.List;

public class AllEventsUiState {
    private final String message;
    private final List<Event> events;
    private final FilterState filterState;

    private AllEventsUiState(Builder builder) {
        this.message = builder.message;
        this.events = builder.events;
        this.filterState = builder.filterState;
    }

    public String getMessage() { return message; }

    public List<Event> getEvents() { return events; }

    public FilterState getFilterState() { return filterState; }

    public static class Builder {
        private final String message;
        private List<Event> events = new ArrayList<>();
        private FilterState filterState = new FilterState();

        public Builder(String message) {
            this.message = message;
        }

        public Builder events(List<Event> events) {
            this.events = events != null ? events : new ArrayList<>();
            return this;
        }

        public Builder filterState(FilterState filterState) {
            this.filterState = filterState != null ? filterState : new FilterState();
            return this;
        }

        public AllEventsUiState build() {
            return new AllEventsUiState(this);
        }
    }
}

