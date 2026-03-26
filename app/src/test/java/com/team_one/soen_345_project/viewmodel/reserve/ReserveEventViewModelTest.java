package com.team_one.soen_345_project.viewmodel.reserve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import com.team_one.soen_345_project.model.repository.IReservationRepository;
import com.team_one.soen_345_project.model.util.callback.ReservationCallback;
import com.team_one.soen_345_project.util.InstantExecutorExtension;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReserveEventViewModelTest {

    @Rule
    public InstantExecutorExtension instantExecutorExtension = new InstantExecutorExtension();

    @Mock
    private IReservationRepository mockReservationRepository;

    private ReserveEventViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new ReserveEventViewModel(mockReservationRepository);
    }

    @Test
    public void bookEvent_success_updatesUiStateToSuccess() {
        // Arrange
        String eventId = "event1";
        doAnswer(invocation -> {
            ReservationCallback callback = invocation.getArgument(1);
            callback.onResult("Event booked successfully", true);
            return null;
        }).when(mockReservationRepository).bookEvent(eq(eventId), any());

        // Act
        viewModel.bookEvent(eventId);

        // Assert
        verify(mockReservationRepository).bookEvent(eq(eventId), any());
        
        ReserveEventUiState state = viewModel.getUiState().getValue();
        assertTrue(state.isSuccess());
        assertTrue(state.isAlreadyBooked());
        assertEquals("Event booked successfully", state.getMessage());
    }

    @Test
    public void bookEvent_failureDueToNoSpots_updatesUiStateToError() {
        // Arrange
        String eventId = "event2";
        doAnswer(invocation -> {
            ReservationCallback callback = invocation.getArgument(1);
            callback.onResult("No spots left", false);
            return null;
        }).when(mockReservationRepository).bookEvent(eq(eventId), any());

        // Act
        viewModel.bookEvent(eventId);

        // Assert
        verify(mockReservationRepository).bookEvent(eq(eventId), any());
        
        ReserveEventUiState state = viewModel.getUiState().getValue();
        assertFalse(state.isSuccess());
        assertEquals("No spots left", state.getMessage());
    }

    @Test
    public void cancelBooking_success_updatesUiStateAndUnbooks() {
        // Arrange
        String eventId = "event3";
        doAnswer(invocation -> {
            ReservationCallback callback = invocation.getArgument(1);
            callback.onResult("Reservation cancelled", true);
            return null;
        }).when(mockReservationRepository).cancelEvent(eq(eventId), any());

        // Act
        viewModel.cancelBooking(eventId);

        // Assert
        verify(mockReservationRepository).cancelEvent(eq(eventId), any());
        
        ReserveEventUiState state = viewModel.getUiState().getValue();
        assertTrue(state.isSuccess());
        assertFalse(state.isAlreadyBooked());
        assertEquals("Reservation cancelled", state.getMessage());
    }

    @Test
    public void cancelBooking_failure_updatesUiStateToError() {
        // Arrange
        String eventId = "event4";
        doAnswer(invocation -> {
            ReservationCallback callback = invocation.getArgument(1);
            callback.onResult("You have not booked this event", false);
            return null;
        }).when(mockReservationRepository).cancelEvent(eq(eventId), any());

        // Act
        viewModel.cancelBooking(eventId);

        // Assert
        verify(mockReservationRepository).cancelEvent(eq(eventId), any());
        
        ReserveEventUiState state = viewModel.getUiState().getValue();
        assertFalse(state.isSuccess());
        assertEquals("You have not booked this event", state.getMessage());
    }
}
