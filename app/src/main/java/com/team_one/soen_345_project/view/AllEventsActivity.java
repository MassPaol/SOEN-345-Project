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
import com.team_one.soen_345_project.viewmodel.userdash.UserDashViewModel;

public class AllEventsActivity extends AppCompatActivity {
    private static final String TAG = "AllEventsActivity";

    private ActivityAllEventsBinding binding;
    private final UserDashViewModel userDashViewModel = new UserDashViewModel();
    private UserEventAdapter userEventAdapter;

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
        userDashViewModel.loadAllEvents();

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
                userDashViewModel.searchEvents(query);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void setupRecyclerView() {
        userEventAdapter = new UserEventAdapter();
        binding.rvAllEvents.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAllEvents.setAdapter(userEventAdapter);
    }

    private void setupObservers() {
        userDashViewModel.getUiState().observe(this, uiState -> {
            if (uiState == null) return;

            Log.d(TAG, "UI State updated - Events: " +
                    (uiState.getEvents() != null ? uiState.getEvents().size() : 0));

            // Update event count label
            int count = uiState.getEvents() != null ? uiState.getEvents().size() : 0;
            binding.tvEventCount.setText(count + " event(s) found");

            // Populate the RecyclerView
            if (uiState.getEvents() != null) {
                userEventAdapter.setEvents(uiState.getEvents());
            }

            // Show error messages
            if (uiState.getMessage() != null && uiState.getMessage().contains("Failed")) {
                Log.e(TAG, "Error: " + uiState.getMessage());
                Toast.makeText(this, uiState.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}

