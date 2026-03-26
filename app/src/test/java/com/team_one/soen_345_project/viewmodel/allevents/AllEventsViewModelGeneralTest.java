package com.team_one.soen_345_project.viewmodel.allevents;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.team_one.soen_345_project.model.entity.Event;
import com.team_one.soen_345_project.model.repository.IEventRepository;
import com.team_one.soen_345_project.model.util.callback.EventListCallback;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

public class AllEventsViewModelGeneralTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private IEventRepository mockEventRepository;

    private AllEventsViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new AllEventsViewModel(mockEventRepository);
    }

    @Test
    public void testLoadAllEventsSuccess() {
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

        AllEventsUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertEquals(3, state.getEvents().size());
        assertTrue(state.getEvents().contains(e1));
        assertTrue(state.getEvents().contains(e2));
        assertTrue(state.getEvents().contains(e3));
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

        AllEventsUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertEquals(2, state.getEvents().size());
        
        boolean hasE1 = state.getEvents().stream().anyMatch(e -> e.getEventId().equals("e1"));
        boolean hasE3 = state.getEvents().stream().anyMatch(e -> e.getEventId().equals("e3"));
        assertTrue(hasE1 && hasE3);
    }

    @Test
    public void testLoadEventsFailure() {
        doAnswer(invocation -> {
            EventListCallback callback = invocation.getArgument(0);
            callback.onError("Connection Failed");
            return null;
        }).when(mockEventRepository).getAllEvents(any(EventListCallback.class));

        viewModel.loadAllEvents();

        AllEventsUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertEquals(0, state.getEvents().size());
        assertEquals("Connection Failed", state.getMessage());
    }
}