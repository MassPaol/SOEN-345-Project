package com.team_one.soen_345_project.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.team_one.soen_345_project.databinding.ActivityAdmindashBinding;
import com.team_one.soen_345_project.model.entity.Event;
import com.team_one.soen_345_project.ui.CreateEventSheet;
import com.team_one.soen_345_project.ui.EditEventSheet;
import com.team_one.soen_345_project.viewmodel.admindash.AdminDashViewModel;

public class AdminDashActivity extends AppCompatActivity {
    private static final String TAG = "AdminDashActivity";
    private ActivityAdmindashBinding binding;

    // ViewModel object for interaction with ViewModel layer
    private final AdminDashViewModel adminDashViewModel = new AdminDashViewModel();

    // RecyclerView adapter for displaying events
    private EventAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called - AdminDashActivity instance created");

        // View Binding: Replaces findViewById to ensure type-safe access to layout views
        binding = ActivityAdmindashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // UI/UX: Extends the layout into system status/navigation bars for a modern edge-to-edge look
        EdgeToEdge.enable(this);

        // Setup RecyclerView
        setupRecyclerView();

        // Initialize Observers to listen for UI state changes
        setupObservers();

        // Load all events when activity starts (this also updates the count)
        adminDashViewModel.loadAllEvents();

        // ----- UI Listeners -----

        // Search functionality
        binding.etSearchEvents.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter events as user types
                String query = s.toString();
                Log.d(TAG, "Search query: " + query);
                adminDashViewModel.searchEvents(query);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        binding.btnCreateEvent.setOnClickListener(v -> {
            Log.i(TAG, "Opening 'Create Event' Form");

            // Event Add popup
            openEventSheet();
        });
    }

    // Setup RecyclerView with adapter and layout manager
    private void setupRecyclerView() {
        eventAdapter = new EventAdapter();
        eventAdapter.setViewModel(adminDashViewModel); // Pass ViewModel to adapter
        binding.rvUpcomingEvents.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUpcomingEvents.setAdapter(eventAdapter);

        // Set up edit event listener
        eventAdapter.setOnEditEventListener(event -> {
            Log.i(TAG, "Edit event clicked: " + event.getTitle());
            openEditEventSheet(event);
        });
    }

    // Setup observers for LiveData from ViewModel
    private void setupObservers() {
        adminDashViewModel.getUiState().observe(this, uiState -> {
            if (uiState != null) {
                Log.d(TAG, "UI State updated - Count: " + uiState.getEventCount() +
                          ", Events: " + (uiState.getEvents() != null ? uiState.getEvents().size() : 0) +
                          ", Message: " + uiState.getMessage());

                // Update the event count TextView
                binding.tvTotalEvents.setText(String.valueOf(uiState.getEventCount()));

                // Update the RecyclerView with events list
                if (uiState.getEvents() != null && !uiState.getEvents().isEmpty()) {
                    eventAdapter.setEvents(uiState.getEvents());
                    Log.d(TAG, "Loaded " + uiState.getEvents().size() + " events");
                } else {
                    Log.d(TAG, "No events to display");
                }

                // Handle action completion (e.g., event created successfully)
                if (uiState.isActionComplete() && uiState.getMessage() != null) {
                    Toast.makeText(this, uiState.getMessage(), Toast.LENGTH_SHORT).show();
                    // Clear the action state after showing the message to prevent re-showing
                    adminDashViewModel.clearActionState();
                }

                // Handle error messages - show all error messages
                if (uiState.getMessage() != null && uiState.getMessage().contains("Failed")) {
                    Log.e(TAG, "Error from ViewModel: " + uiState.getMessage());
                    Toast.makeText(this, uiState.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Event sheet slide up form
    public void openEventSheet() {
        CreateEventSheet bottomSheet = new CreateEventSheet();
        // Pass the ViewModel instance to the sheet so they share the same instance
        bottomSheet.setViewModel(adminDashViewModel);
        // 'getSupportFragmentManager' is the manager that handles fragment transactions
        bottomSheet.show(getSupportFragmentManager(), "CreateEventTag");
    }

    // Edit event sheet slide up form
    public void openEditEventSheet(Event event) {
        EditEventSheet bottomSheet = new EditEventSheet();
        // Pass the ViewModel instance and the event to be edited
        bottomSheet.setViewModel(adminDashViewModel);
        bottomSheet.setEvent(event);
        // Show the edit sheet
        bottomSheet.show(getSupportFragmentManager(), "EditEventTag");
    }

}
