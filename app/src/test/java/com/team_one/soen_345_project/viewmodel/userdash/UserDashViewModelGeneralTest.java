package com.team_one.soen_345_project.viewmodel.userdash;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.team_one.soen_345_project.model.entity.Event;
import com.team_one.soen_345_project.model.repository.IEventRepository;
import com.team_one.soen_345_project.model.repository.IReservationRepository;
import com.team_one.soen_345_project.model.util.callback.BookedEventsCallback;
import com.team_one.soen_345_project.model.util.callback.EventListCallback;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class UserDashViewModelGeneralTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private IEventRepository mockEventRepository;

    @Mock
    private IReservationRepository mockReservationRepository;

    private UserDashViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new UserDashViewModel(mockEventRepository, mockReservationRepository);
    }

    @Test
    public void testLoadBookedUpcomingEventsSuccess() {
        Event e1 = new Event(); e1.setEventId("e1"); e1.setTitle("Booked 1");
        Event e2 = new Event(); e2.setEventId("e2"); e2.setTitle("Unbooked");
        Event e3 = new Event(); e3.setEventId("e3"); e3.setTitle("Booked 2");
        
        List<Event> mockEvents = Arrays.asList(e1, e2, e3);

        doAnswer(invocation -> {
            EventListCallback callback = invocation.getArgument(0);
            callback.onEventsReceived(mockEvents);
            return null;
        }).when(mockEventRepository).getAllEvents(any(EventListCallback.class));

        doAnswer(invocation -> {
            BookedEventsCallback callback = invocation.getArgument(0);
            callback.onResult(new HashSet<>(Arrays.asList("e1", "e3")));        
            return null;
        }).when(mockReservationRepository).getBookedEventIdsForCurrentUser(any(BookedEventsCallback.class));

        viewModel.loadBookedUpcomingEvents();

        UserDashUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        // The actionComplete flag is only set true on certain success cases, here it is false
        assertFalse(state.isActionComplete());
        assertNotNull(state.getEvents());
        assertEquals(2, state.getEvents().size());
        
        boolean hasE1 = state.getEvents().stream().anyMatch(e -> e.getEventId().equals("e1"));
        boolean hasE3 = state.getEvents().stream().anyMatch(e -> e.getEventId().equals("e3"));
        assertTrue(hasE1 && hasE3);
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

        UserDashUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertEquals(2, state.getEvents().size());
        
        boolean hasE1 = state.getEvents().stream().anyMatch(e -> e.getEventId().equals("e1"));
        boolean hasE3 = state.getEvents().stream().anyMatch(e -> e.getEventId().equals("e3"));
        assertTrue(hasE1 && hasE3);
    }
}
