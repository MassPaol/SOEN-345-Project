package com.team_one.soen_345_project.view;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.team_one.soen_345_project.R;
import com.team_one.soen_345_project.databinding.ActivityMainBinding;
import com.team_one.soen_345_project.databinding.ActivityRegisterBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // View Binding: Replaces findViewById to ensure type-safe access to layout views
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // UI/UX: Extends the layout into system status/navigation bars for a modern edge-to-edge look
        EdgeToEdge.enable(this);

        // Initialize Observers early so we don't miss any state emissions from the ViewModel
        setupObservers();

        // Show the success popup
        handleRegistrationSuccess();
    }

    private void handleRegistrationSuccess() {
        String message = getIntent().getStringExtra("REGISTER_SUCCESS");
        if (message != null) {
            // We use the binding root to anchor the Snackbar
            Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();

            // Optional: Remove the extra so it doesn't pop up again on rotation
            getIntent().removeExtra("REGISTER_SUCCESS");
        }
    }

    /**
     * Reactive Programming: The View 'listens' for state changes rather than
     * asking for updates, allowing for a decoupled, event-driven UI.
     */
    private void setupObservers() {

    }
}
