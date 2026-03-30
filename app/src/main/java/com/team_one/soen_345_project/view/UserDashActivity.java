package com.team_one.soen_345_project.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.team_one.soen_345_project.databinding.ActivityUserdashBinding;
import com.team_one.soen_345_project.ui.FilterReserveEventSheet;
import com.team_one.soen_345_project.viewmodel.userdash.UserDashViewModel;

public class UserDashActivity extends AppCompatActivity {
    private static final String TAG = "UserDashActivity";

    private ActivityUserdashBinding binding;
    private final UserDashViewModel userDashViewModel = new UserDashViewModel();
    private UserEventAdapter userEventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called - UserDashActivity instance created");

        // View Binding: type-safe access to layout views
        binding = ActivityUserdashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Extend layout into system bars for edge-to-edge look
        EdgeToEdge.enable(this);

        binding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(UserDashActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        setupRecyclerView();
        setupObservers();

        // Load only the user's booked events for the Upcoming Events list
        userDashViewModel.refreshBookedUpcomingEvents();

        // ----- UI Listeners -----

        // Live search as user types
        binding.etSearchEvents.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                Log.d(TAG, "Search query: " + query);
                userDashViewModel.searchEvents(query);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // "See All Events" – navigates to the full browsable events screen
        binding.btnSeeAllEvents.setOnClickListener(v -> {
            Log.i(TAG, "See All Events clicked – launching AllEventsActivity");
            Intent intent = new Intent(this, AllEventsActivity.class);
            startActivity(intent);
        });

        binding.btnFilter.setOnClickListener(v -> {
            FilterReserveEventSheet sheet = FilterReserveEventSheet.newInstance(
                    filterState -> {
                        Log.d(TAG, "Filter applied: " + filterState.getCategory().getLabel());
                        userDashViewModel.applyFilter(filterState);
                    }
            );
            sheet.show(getSupportFragmentManager(), "FilterBottomSheet");
        });
    }

    private void setupRecyclerView() {
        userEventAdapter = new UserEventAdapter();
        userEventAdapter.setShowStatus(false);
        binding.rvUpcomingEvents.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUpcomingEvents.setAdapter(userEventAdapter);

        userEventAdapter.setOnItemClickListener(event -> {
            if (event == null || event.getEventId() == null) return;

            ReserveEventSheet bottomSheet = ReserveEventSheet.newInstance(event.getEventId());
            bottomSheet.setEventProvider(eventId -> {
                if (userDashViewModel.getUiState().getValue() == null || userDashViewModel.getUiState().getValue().getEvents() == null) {
                    return null;
                }
                for (com.team_one.soen_345_project.model.entity.Event e : userDashViewModel.getUiState().getValue().getEvents()) {
                    if (e != null && eventId.equals(e.getEventId())) {
                        return e;
                    }
                }
                return null;
            });
            bottomSheet.setOnBookingSuccessListener(changedEventId ->
                    userDashViewModel.refreshBookedUpcomingEvents());
            bottomSheet.show(getSupportFragmentManager(), "EventDetailsBottomSheetFragment");
        });
    }

    private void setupObservers() {
        userDashViewModel.getUiState().observe(this, uiState -> {
            if (uiState == null) return;

            Log.d(TAG, "UI State updated - TotalEvents: " + uiState.getTotalEventCount() +
                    ", Displayed: " + (uiState.getEvents() != null ? uiState.getEvents().size() : 0));

            // Update stats card
            binding.tvTotalEvents.setText(String.valueOf(uiState.getTotalEventCount()));
            // "Upcoming" counter mirrors visible list size (booking logic comes in Sprint 3)
            binding.tvUpcomingCount.setText(
                    String.valueOf(uiState.getEvents() != null ? uiState.getEvents().size() : 0));

            // Populate RecyclerView
            if (uiState.getEvents() != null) {
                userEventAdapter.setEvents(uiState.getEvents());
            }

            // Show error/info messages
            if (uiState.getMessage() != null && uiState.getMessage().contains("Failed")) {
                Log.e(TAG, "Error from ViewModel: " + uiState.getMessage());
                Toast.makeText(this, uiState.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // Logs applied filter state
        userDashViewModel.getUiState().observe(this, uiState -> {
            if (uiState == null || uiState.getFilterState() == null) return;
            Log.d(TAG, "Active filter - " +
                    uiState.getFilterState().toString());
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Force refresh from repository when returning from other screens (e.g., All Events booking)
        userDashViewModel.refreshBookedUpcomingEvents();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
