package com.team_one.soen_345_project.viewmodel.admindash;

import com.team_one.soen_345_project.model.entity.Event;
import com.team_one.soen_345_project.model.util.filter.FilterState;
import com.team_one.soen_345_project.viewmodel.userdash.UserDashUiState;

import java.util.ArrayList;
import java.util.List;

public class AdminDashUiState {
    private final String message;
    private final boolean isActionComplete;
    private final int eventCount;
    private final List<Event> events;
    private final FilterState filterState;

    private AdminDashUiState(Builder builder) {
        this.message = builder.message;
        this.isActionComplete = builder.isActionComplete;
        this.eventCount = builder.eventCount;
        this.events = builder.events;
        this.filterState = builder.filterState;
    }

    public boolean isActionComplete() { return isActionComplete; }
    public String getMessage() { return message; }
    public int getEventCount() { return eventCount; }
    public List<Event> getEvents() { return events; }
    public FilterState getFilterState() { return filterState; }

    public static class Builder {
        // Required
        private final String message;
        private final boolean isActionComplete;

        // Optional — sensible defaults
        private int eventCount = 0;
        private List<Event> events = new ArrayList<>();
        private FilterState filterState;

        public Builder(String message, boolean isActionComplete) {
            this.message = message;
            this.isActionComplete = isActionComplete;
            this.filterState = new FilterState();
        }

        public Builder eventCount(int eventCount) {
            this.eventCount = eventCount;
            return this;
        }

        public Builder events(List<Event> events) {
            this.events = events != null ? events : new ArrayList<>();
            return this;
        }

        public Builder filterState(FilterState filterState) {
            this.filterState = filterState != null ? filterState : new FilterState();
            return this;
        }

        public AdminDashUiState build() {
            return new AdminDashUiState(this);
        }
    }
}