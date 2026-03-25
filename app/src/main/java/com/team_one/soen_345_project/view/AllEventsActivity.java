package com.team_one.soen_345_project.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.team_one.soen_345_project.databinding.ActivityAllEventsBinding;
import com.team_one.soen_345_project.di.Injection;
import com.team_one.soen_345_project.model.repository.IReservationRepository;
import com.team_one.soen_345_project.ui.FilterReserveEventSheet;
import com.team_one.soen_345_project.viewmodel.allevents.AllEventsViewModel;

public class AllEventsActivity extends AppCompatActivity {
    private static final String TAG = "AllEventsActivity";

    private ActivityAllEventsBinding binding;
    private final AllEventsViewModel allEventsViewModel = new AllEventsViewModel();
    private UserEventAdapter userEventAdapter;
    private final IReservationRepository reservationRepository = Injection.provideReservationRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");

        binding = ActivityAllEventsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);

        setupRecyclerView();
        setupObservers();

        // Load all events from Firebase
        allEventsViewModel.loadAllEvents();

        // Load booked events early (may complete before events load)
        loadBookedEvents();

        // Back button returns to User Dashboard
        binding.btnBack.setOnClickListener(v -> finish());

        // Live search as user types
        binding.etSearchAllEvents.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                Log.d(TAG, "Search query: " + query);
                allEventsViewModel.searchEvents(query);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        binding.btnFilter.setOnClickListener(v -> {
            FilterReserveEventSheet sheet = FilterReserveEventSheet.newInstance(
                    filterState -> {
                        Log.d(TAG, "Filter applied: " + filterState.getCategory().getLabel());
                        allEventsViewModel.applyFilter(filterState);
                    }
            );
            sheet.show(getSupportFragmentManager(), "FilterBottomSheet");
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh bookings when coming back (e.g., after booking in another screen)
        loadBookedEvents();
    }

    private void setupRecyclerView() {
        userEventAdapter = new UserEventAdapter();
        binding.rvAllEvents.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAllEvents.setAdapter(userEventAdapter);

        userEventAdapter.setOnItemClickListener(event -> {
            if (event == null || event.getEventId() == null) return;

            ReserveEventSheet bottomSheet = ReserveEventSheet.newInstance(event.getEventId());
            bottomSheet.setEventProvider(eventId -> {
                if (allEventsViewModel.getUiState().getValue() == null || allEventsViewModel.getUiState().getValue().getEvents() == null) {
                    return null;
                }
                for (com.team_one.soen_345_project.model.entity.Event e : allEventsViewModel.getUiState().getValue().getEvents()) {
                    if (e != null && eventId.equals(e.getEventId())) {
                        return e;
                    }
                }
                return null;
            });
            bottomSheet.setOnBookingSuccessListener(bookedEventId -> {
                // Refresh booked badge state immediately
                loadBookedEvents();
                // Optional: also refresh events list so capacity/reservations reflect new booking
                allEventsViewModel.loadAllEvents();
            });
            bottomSheet.show(getSupportFragmentManager(), "EventDetailsBottomSheetFragment");
        });
    }

    private void setupObservers() {
        allEventsViewModel.getUiState().observe(this, uiState -> {
            if (uiState == null) return;

            Log.d(TAG, "UI State updated - Events: " +
                    (uiState.getEvents() != null ? uiState.getEvents().size() : 0));

            // Update event count label
            int count = uiState.getEvents() != null ? uiState.getEvents().size() : 0;
            binding.tvEventCount.setText(count + " event(s) found");

            // Populate the RecyclerView
            if (uiState.getEvents() != null) {
                userEventAdapter.setEvents(uiState.getEvents());
                // Re-apply current bookings after list updates
                loadBookedEvents();
            }

            // Show error messages
            if (uiState.getMessage() != null && uiState.getMessage().contains("Failed")) {
                Log.e(TAG, "Error: " + uiState.getMessage());
                Toast.makeText(this, uiState.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadBookedEvents() {
        reservationRepository.getBookedEventIdsForCurrentUser(new com.team_one.soen_345_project.model.util.callback.BookedEventsCallback() {
            @Override
            public void onResult(java.util.Set<String> bookedEventIds) {
                userEventAdapter.setBookedEventIds(bookedEventIds);
            }

            @Override
            public void onError(String message) {
                // Silent fail: we just won't show badges.
                android.util.Log.e(TAG, "Failed to load bookings: " + message);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
