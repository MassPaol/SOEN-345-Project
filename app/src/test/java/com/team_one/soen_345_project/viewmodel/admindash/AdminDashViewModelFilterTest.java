// AdminDashViewModelFilterTest.java
package com.team_one.soen_345_project.viewmodel.admindash;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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

public class AdminDashViewModelFilterTest {

    @Mock
    private IEventRepository mockRepository;

    @Mock
    private com.team_one.soen_345_project.model.repository.IReservationRepository mockReservationRepository;

    private AdminDashViewModel viewModel;

    private Event techEvent;    // category: TECH,   location: MONTREAL, price: 20, full: false
    private Event sportsEvent;  // category: SPORTS, location: TORONTO,  price: 50, full: true
    private Event freeEvent;    // category: TECH,   location: MONTREAL, price:  0, full: false

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        viewModel = new AdminDashViewModel(mockRepository, mockReservationRepository);

        techEvent   = buildEvent("1", "Tech Talk",    CategoryFilterOption.TECH,   LocationFilterOption.MONTREAL, 20.0, false, new Timestamp(new Date(125, 4, 10)));
        sportsEvent = buildEvent("2", "Sports Night", CategoryFilterOption.SPORTS, LocationFilterOption.TORONTO,  50.0, true,  new Timestamp(new Date(125, 5, 15)));
        freeEvent   = buildEvent("3", "Free Meetup",  CategoryFilterOption.TECH,   LocationFilterOption.MONTREAL,  0.0, false, new Timestamp(new Date(125, 6, 20)));

        injectAllEvents(viewModel, Arrays.asList(techEvent, sportsEvent, freeEvent));
    }

    // ------------------------------------------------------------------ //
    //  Category filter                                                     //
    // ------------------------------------------------------------------ //

    @Test
    public void filterEvents_categoryAll_returnsAllEvents() {
        FilterState state = new FilterState(); // default: ALL

        assertEquals(3, viewModel.filterEvents(state).size());
    }

    @Test
    public void filterEvents_specificCategory_returnsMatchingOnly() {
        FilterState state = new FilterState();
        state.setCategory(CategoryFilterOption.TECH);

        List<Event> result = viewModel.filterEvents(state);

        assertEquals(2, result.size());
        assertTrue(result.contains(techEvent));
        assertTrue(result.contains(freeEvent));
        assertFalse(result.contains(sportsEvent));
    }

    @Test
    public void filterEvents_categoryWithNoMatch_returnsEmpty() {
        FilterState state = new FilterState();
        state.setCategory(CategoryFilterOption.GAMING); // no events in this category

        assertTrue(viewModel.filterEvents(state).isEmpty());
    }

    // ------------------------------------------------------------------ //
    //  Location filter                                                     //
    // ------------------------------------------------------------------ //

    @Test
    public void filterEvents_locationAll_returnsAllEvents() {
        FilterState state = new FilterState();
        state.setLocation(LocationFilterOption.ALL);

        assertEquals(3, viewModel.filterEvents(state).size());
    }

    @Test
    public void filterEvents_specificLocation_returnsMatchingOnly() {
        FilterState state = new FilterState();
        state.setLocation(LocationFilterOption.TORONTO);

        List<Event> result = viewModel.filterEvents(state);

        assertEquals(1, result.size());
        assertTrue(result.contains(sportsEvent));
    }

    @Test
    public void filterEvents_locationWithNoMatch_returnsEmpty() {
        FilterState state = new FilterState();
        state.setLocation(LocationFilterOption.GATINEAU); // no events there

        assertTrue(viewModel.filterEvents(state).isEmpty());
    }

    // ------------------------------------------------------------------ //
    //  Date range filter                                                   //
    // ------------------------------------------------------------------ //

    @Test
    public void filterEvents_dateFrom_excludesEventsBeforeDate() {
        FilterState state = new FilterState();
        state.setDateFrom(new Timestamp(new Date(125, 5, 1))); // June 1 — excludes May techEvent

        List<Event> result = viewModel.filterEvents(state);

        assertFalse(result.contains(techEvent));
        assertTrue(result.contains(sportsEvent));
        assertTrue(result.contains(freeEvent));
    }

    @Test
    public void filterEvents_dateTo_excludesEventsAfterDate() {
        FilterState state = new FilterState();
        state.setDateTo(new Timestamp(new Date(125, 4, 20))); // May 20 — only techEvent qualifies

        List<Event> result = viewModel.filterEvents(state);

        assertTrue(result.contains(techEvent));
        assertFalse(result.contains(sportsEvent));
        assertFalse(result.contains(freeEvent));
    }

    @Test
    public void filterEvents_dateRange_returnsOnlyEventsWithinRange() {
        FilterState state = new FilterState();
        state.setDateFrom(new Timestamp(new Date(125, 5, 1)));  // June 1
        state.setDateTo(new Timestamp(new Date(125, 5, 30)));   // June 30 — only sportsEvent

        List<Event> result = viewModel.filterEvents(state);

        assertEquals(1, result.size());
        assertTrue(result.contains(sportsEvent));
    }

    @Test
    public void filterEvents_nullDateBounds_doesNotFilterByDate() {
        FilterState state = new FilterState(); // dateFrom/dateTo null by default

        assertEquals(3, viewModel.filterEvents(state).size());
    }

    // ------------------------------------------------------------------ //
    //  Availability filter                                                 //
    // ------------------------------------------------------------------ //

    @Test
    public void filterEvents_availableOnly_excludesFullEvents() {
        FilterState state = new FilterState();
        state.setAvailableOnly(true);

        List<Event> result = viewModel.filterEvents(state);

        assertFalse(result.contains(sportsEvent)); // sportsEvent is full
        assertTrue(result.contains(techEvent));
        assertTrue(result.contains(freeEvent));
    }

    @Test
    public void filterEvents_availableOnlyFalse_includesFullEvents() {
        FilterState state = new FilterState();
        state.setAvailableOnly(false);

        assertEquals(3, viewModel.filterEvents(state).size());
    }

    // ------------------------------------------------------------------ //
    //  Price filter                                                        //
    // ------------------------------------------------------------------ //

    @Test
    public void filterEvents_minPrice_excludesEventsBelowMinimum() {
        FilterState state = new FilterState();
        state.setMinPrice(25.0); // only sportsEvent (50) qualifies

        List<Event> result = viewModel.filterEvents(state);

        assertEquals(1, result.size());
        assertTrue(result.contains(sportsEvent));
    }

    @Test
    public void filterEvents_maxPrice_excludesEventsAboveMaximum() {
        FilterState state = new FilterState();
        state.setMaxPrice(20.0); // techEvent (20) and freeEvent (0) qualify

        List<Event> result = viewModel.filterEvents(state);

        assertEquals(2, result.size());
        assertTrue(result.contains(techEvent));
        assertTrue(result.contains(freeEvent));
    }

    @Test
    public void filterEvents_priceRange_returnsOnlyEventsWithinRange() {
        FilterState state = new FilterState();
        state.setMinPrice(10.0);
        state.setMaxPrice(30.0); // only techEvent (20) qualifies

        List<Event> result = viewModel.filterEvents(state);

        assertEquals(1, result.size());
        assertTrue(result.contains(techEvent));
    }

    @Test
    public void filterEvents_exactMinPriceBoundary_isInclusive() {
        FilterState state = new FilterState();
        state.setMinPrice(20.0); // techEvent costs exactly 20

        assertTrue(viewModel.filterEvents(state).contains(techEvent));
    }

    @Test
    public void filterEvents_exactMaxPriceBoundary_isInclusive() {
        FilterState state = new FilterState();
        state.setMaxPrice(50.0); // sportsEvent costs exactly 50

        assertTrue(viewModel.filterEvents(state).contains(sportsEvent));
    }

    @Test
    public void filterEvents_nullPriceBounds_doesNotFilterByPrice() {
        FilterState state = new FilterState(); // min/max null by default

        assertEquals(3, viewModel.filterEvents(state).size());
    }

    // ------------------------------------------------------------------ //
    //  Combined filters                                                    //
    // ------------------------------------------------------------------ //

    @Test
    public void filterEvents_categoryAndLocation_appliesBothConditions() {
        FilterState state = new FilterState();
        state.setCategory(CategoryFilterOption.TECH);
        state.setLocation(LocationFilterOption.MONTREAL);

        List<Event> result = viewModel.filterEvents(state);

        assertEquals(2, result.size());
        assertTrue(result.contains(techEvent));
        assertTrue(result.contains(freeEvent));
    }

    @Test
    public void filterEvents_allFiltersActive_returnsOnlyMatchingEvent() {
        FilterState state = new FilterState();
        state.setCategory(CategoryFilterOption.TECH);
        state.setLocation(LocationFilterOption.MONTREAL);
        state.setMinPrice(10.0);
        state.setMaxPrice(30.0);
        state.setAvailableOnly(true); // excludes freeEvent which has price 0 < 10

        List<Event> result = viewModel.filterEvents(state);

        assertEquals(1, result.size());
        assertTrue(result.contains(techEvent));
    }

    @Test
    public void filterEvents_contradictoryPriceRange_returnsEmpty() {
        FilterState state = new FilterState();
        state.setMinPrice(100.0);
        state.setMaxPrice(10.0); // impossible range

        assertTrue(viewModel.filterEvents(state).isEmpty());
    }

    // ------------------------------------------------------------------ //
    //  Edge cases                                                          //
    // ------------------------------------------------------------------ //

    @Test
    public void filterEvents_emptyEventList_returnsEmpty() throws Exception {
        injectAllEvents(viewModel, List.of());

        assertTrue(viewModel.filterEvents(new FilterState()).isEmpty());
    }

    // ------------------------------------------------------------------ //
    //  Helpers                                                             //
    // ------------------------------------------------------------------ //

    private void injectAllEvents(AdminDashViewModel vm, List<Event> events) throws Exception {
        Field field = AdminDashViewModel.class.getDeclaredField("allEvents");
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