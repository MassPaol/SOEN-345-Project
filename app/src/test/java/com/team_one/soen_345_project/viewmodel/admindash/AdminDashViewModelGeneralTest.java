package com.team_one.soen_345_project.viewmodel.admindash;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.team_one.soen_345_project.model.entity.Event;
import com.team_one.soen_345_project.model.repository.IEventRepository;
import com.team_one.soen_345_project.model.repository.IReservationRepository;
import com.team_one.soen_345_project.model.util.callback.Callback;
import com.team_one.soen_345_project.model.util.callback.DeleteEventCallback;
import com.team_one.soen_345_project.model.util.callback.EventListCallback;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AdminDashViewModelGeneralTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private IEventRepository mockEventRepository;

    @Mock
    private IReservationRepository mockReservationRepository;

    private AdminDashViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new AdminDashViewModel(mockEventRepository, mockReservationRepository);
    }

    @Test
    public void testLoadAllEventsSuccess() {
        Event e1 = new Event(); e1.setEventId("e1"); e1.setTitle("Title 1");
        Event e2 = new Event(); e2.setEventId("e2"); e2.setTitle("Title 2");
        List<Event> mockEvents = Arrays.asList(e1, e2);

        doAnswer(invocation -> {
            EventListCallback callback = invocation.getArgument(0);
            callback.onEventsReceived(mockEvents);
            return null;
        }).when(mockEventRepository).getAllEvents(any(EventListCallback.class));

        viewModel.loadAllEvents();

        AdminDashUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertTrue(state.getEventCount() == 2);
        assertTrue(state.getEvents().contains(e1));
        assertTrue(state.getEvents().contains(e2));
    }

    @Test
    public void testSaveEventSuccess() {
        HashMap<String, String> eventInfo = new HashMap<>();
        eventInfo.put("title", "New Event");
        eventInfo.put("location_id", "Loc_123");

        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onResult("Event Added Successfully", true);
            return null;
        }).when(mockEventRepository).saveEvent(eq(eventInfo), any(Callback.class));
        
        doAnswer(invocation -> {
            EventListCallback callback = invocation.getArgument(0);
            callback.onEventsReceived(Arrays.asList(new Event()));
            return null;
        }).when(mockEventRepository).getAllEvents(any(EventListCallback.class));

        viewModel.saveEvent(eventInfo);

        AdminDashUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertTrue(state.isActionComplete());
        assertEquals("Event Added Successfully", state.getMessage());
    }

    @Test
    public void testDeleteEventSuccess() {
        String eventId = "test_event_id";

        doAnswer(invocation -> {
            com.team_one.soen_345_project.model.util.callback.ReservationCallback callback = invocation.getArgument(1);
            callback.onResult("Reservations deleted", true);
            return null;
        }).when(mockReservationRepository).deleteAllReservationsForEvent(eq(eventId), any(com.team_one.soen_345_project.model.util.callback.ReservationCallback.class));

        doAnswer(invocation -> {
            DeleteEventCallback callback = invocation.getArgument(1);
            callback.onResult("Event Deleted Successfully", true);
            return null;
        }).when(mockEventRepository).deleteEvent(eq(eventId), any(DeleteEventCallback.class));

        doAnswer(invocation -> {
            EventListCallback callback = invocation.getArgument(0);
            callback.onEventsReceived(Arrays.asList());
            return null;
        }).when(mockEventRepository).getAllEvents(any(EventListCallback.class));

        viewModel.deleteEvent(eventId);

        AdminDashUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertEquals("Event Deleted Successfully", state.getMessage());
        assertTrue(state.isActionComplete());
    }

    @Test
    public void testSearchEvents() {
        Event e1 = new Event(); e1.setEventId("e1"); e1.setTitle("Alpha Search Title");
        Event e2 = new Event(); e2.setEventId("e2"); e2.setTitle("Gamma Title");
        Event e3 = new Event(); e3.setEventId("e3"); e3.setTitle("Alpha Omega");

        List<Event> mockEvents = Arrays.asList(e1, e2, e3);

        doAnswer(invocation -> {
            EventListCallback callback = invocation.getArgument(0);
            callback.onEventsReceived(mockEvents);
            return null;
        }).when(mockEventRepository).getAllEvents(any(EventListCallback.class));

        viewModel.loadAllEvents();

        viewModel.searchEvents("Alpha");

        AdminDashUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertEquals(2, state.getEvents().size());
        
        boolean hasE1 = state.getEvents().stream().anyMatch(e -> e.getEventId().equals("e1"));
        boolean hasE3 = state.getEvents().stream().anyMatch(e -> e.getEventId().equals("e3"));
        assertTrue(hasE1 && hasE3);
    }
}