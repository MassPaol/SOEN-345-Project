package com.team_one.soen_345_project.viewmodel.userdash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.team_one.soen_345_project.model.repository.IEventRepository;
import com.team_one.soen_345_project.model.repository.IReservationRepository;
import com.team_one.soen_345_project.model.util.callback.ReservationCallback;
import com.team_one.soen_345_project.util.InstantExecutorExtension;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserDashViewModelCancelReservationTest {

    @Rule
    public InstantExecutorExtension instantExecutorExtension = new InstantExecutorExtension();

    @Mock
    private IEventRepository mockEventRepository;

    @Mock
    private IReservationRepository mockReservationRepository;

    private UserDashViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new UserDashViewModel(mockEventRepository, mockReservationRepository);
    }

    @Test
    public void cancelBooking_success_updatesUiStateWithoutReloadingAllEvents() {
        // Arrange
        String eventId = "event1";
        doAnswer(invocation -> {
            ReservationCallback callback = invocation.getArgument(1);
            callback.onResult("Cancelled", true);
            return null;
        }).when(mockReservationRepository).cancelEvent(eq(eventId), any());

        // Act
        viewModel.cancelBooking(eventId);

        // Assert
        verify(mockReservationRepository).cancelEvent(eq(eventId), any());
        verify(mockEventRepository, never()).getAllEvents(any());

        UserDashUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertEquals("Cancelled", state.getMessage());
        assertTrue(state.isActionComplete());
    }

    @Test
    public void cancelBooking_failure_updatesUiStateWithError() {
        // Arrange
        String eventId = "event2";
        doAnswer(invocation -> {
            ReservationCallback callback = invocation.getArgument(1);
            callback.onResult("Failed to cancel", false);
            return null;
        }).when(mockReservationRepository).cancelEvent(eq(eventId), any());

        // Act
        viewModel.cancelBooking(eventId);

        // Assert
        verify(mockReservationRepository).cancelEvent(eq(eventId), any());
        
        UserDashUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        String message = state != null ? state.getMessage() : null;
        assertNotNull(message);
        assertEquals("Failed to cancel", message);
        assertFalse(state.isActionComplete());
    }
}
