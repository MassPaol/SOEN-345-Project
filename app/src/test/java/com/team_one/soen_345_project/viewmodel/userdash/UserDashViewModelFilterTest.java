// UserDashViewModelFilterTest.java
package com.team_one.soen_345_project.viewmodel.userdash;

import static org.junit.Assert.*;

import com.google.firebase.Timestamp;
import com.team_one.soen_345_project.model.entity.Event;
import com.team_one.soen_345_project.model.repository.IEventRepository;
import com.team_one.soen_345_project.model.util.filter.CategoryFilterOption;
import com.team_one.soen_345_project.model.util.filter.FilterState;
import com.team_one.soen_345_project.model.util.filter.LocationFilterOption;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class UserDashViewModelFilterTest {

    @Mock
    private IEventRepository mockRepository;

    private UserDashViewModel viewModel;

    private Event techEvent;
    private Event sportsEvent;
    private Event freeEvent;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        viewModel = new UserDashViewModel(mockRepository);

        techEvent   = buildEvent("1", "Tech Talk",    CategoryFilterOption.TECH,   LocationFilterOption.MONTREAL, 20.0, false, new Timestamp(new Date(125, 4, 10)));
        sportsEvent = buildEvent("2", "Sports Night", CategoryFilterOption.SPORTS, LocationFilterOption.TORONTO,  50.0, true,  new Timestamp(new Date(125, 5, 15)));
        freeEvent   = buildEvent("3", "Free Meetup",  CategoryFilterOption.TECH,   LocationFilterOption.MONTREAL,  0.0, false, new Timestamp(new Date(125, 6, 20)));

        injectAllEvents(viewModel, Arrays.asList(techEvent, sportsEvent, freeEvent));
    }

    @Test
    public void filterEvents_categoryAll_returnsAll() {
        assertEquals(3, viewModel.filterEvents(new FilterState()).size());
    }

    @Test
    public void filterEvents_specificCategory_returnsMatchingOnly() {
        FilterState state = new FilterState();
        state.setCategory(CategoryFilterOption.TECH);

        List<Event> result = viewModel.filterEvents(state);

        assertEquals(2, result.size());
        assertFalse(result.contains(sportsEvent));
    }

    @Test
    public void filterEvents_availableOnly_excludesFullEvents() {
        FilterState state = new FilterState();
        state.setAvailableOnly(true);

        assertFalse(viewModel.filterEvents(state).contains(sportsEvent));
    }

    @Test
    public void filterEvents_priceRange_narrowsResults() {
        FilterState state = new FilterState();
        state.setMinPrice(10.0);
        state.setMaxPrice(30.0);

        List<Event> result = viewModel.filterEvents(state);

        assertEquals(1, result.size());
        assertTrue(result.contains(techEvent));
    }

    @Test
    public void filterEvents_locationFilter_returnsMatchingOnly() {
        FilterState state = new FilterState();
        state.setLocation(LocationFilterOption.TORONTO);

        List<Event> result = viewModel.filterEvents(state);

        assertEquals(1, result.size());
        assertTrue(result.contains(sportsEvent));
    }

    @Test
    public void filterEvents_allFiltersActive_returnsCorrectSingleResult() {
        FilterState state = new FilterState();
        state.setCategory(CategoryFilterOption.TECH);
        state.setLocation(LocationFilterOption.MONTREAL);
        state.setMinPrice(10.0);
        state.setAvailableOnly(true);

        List<Event> result = viewModel.filterEvents(state);

        assertEquals(1, result.size());
        assertTrue(result.contains(techEvent));
    }

    private void injectAllEvents(UserDashViewModel vm, List<Event> events) throws Exception {
        Field field = UserDashViewModel.class.getDeclaredField("allEvents");
        field.setAccessible(true);
        field.set(vm, events);
    }

    private Event buildEvent(String id, String title,
                             CategoryFilterOption category, LocationFilterOption location,
                             double price, boolean isFull, Timestamp date) {
        Event event = new Event();
        event.setEventId(id);
        event.setTitle(title);
        event.setCategory_id(category.getId());
        event.setLocation_id(location.getId());
        event.setPrice(price);
        event.setDate(date);

        // isFull() = reservations >= capacity, so drive it with capacity/reservations
        if (isFull) {
            event.setCapacity(10);
            event.setReservations(10);  // full: reservations == capacity
        } else {
            event.setCapacity(10);
            event.setReservations(0);   // not full
        }

        return event;
    }
}