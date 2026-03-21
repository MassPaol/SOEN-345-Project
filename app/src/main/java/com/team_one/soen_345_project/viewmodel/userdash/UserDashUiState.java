package com.team_one.soen_345_project.viewmodel.userdash;

import com.team_one.soen_345_project.model.entity.Event;
import com.team_one.soen_345_project.model.util.filter.FilterState;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Filter;

public class UserDashUiState {
    private final String message;
    private final boolean isActionComplete;
    private final int totalEventCount;
    private final List<Event> events;
    private final FilterState filterState;

    private UserDashUiState(Builder builder) {
        this.message = builder.message;
        this.isActionComplete = builder.isActionComplete;
        this.totalEventCount = builder.totalEventCount;
        this.events = builder.events;
        this.filterState = builder.filterState;
    }

    public boolean isActionComplete() { return isActionComplete; }
    public String getMessage() { return message; }
    public int getTotalEventCount() { return totalEventCount; }
    public List<Event> getEvents() { return events; }
    public FilterState getFilterState() { return filterState; }

    public static class Builder {
        // Required
        private final String message;
        private final boolean isActionComplete;
        private FilterState filterState;

        // Optional — sensible defaults
        private int totalEventCount = 0;
        private List<Event> events = new ArrayList<>();

        public Builder(String message, boolean isActionComplete) {
            this.message = message;
            this.isActionComplete = isActionComplete;
            this.filterState = new FilterState();
        }

        public Builder totalEventCount(int totalEventCount) {
            this.totalEventCount = totalEventCount;
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

        public UserDashUiState build() {
            return new UserDashUiState(this);
        }
    }
}