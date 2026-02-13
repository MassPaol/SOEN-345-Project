package com.team_one.soen_345_project.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.team_one.soen_345_project.databinding.ActivityRegisterBinding;
import com.team_one.soen_345_project.viewmodel.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;

    // ViewModel object for interaction with ViewModel layer
    private final RegisterViewModel registerViewModel = new RegisterViewModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // View Binding: Replaces findViewById to ensure type-safe access to layout views
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // UI/UX: Extends the layout into system status/navigation bars for a modern edge-to-edge look
        EdgeToEdge.enable(this);

        // Initialize Observers early so we don't miss any state emissions from the ViewModel
        setupObservers();

        // ----- UI Listeners -----

        binding.btnRegister.setOnClickListener(v -> {
            // Business Logic: Collect data first, then hand it off to the ViewModel for processing
            String[] registrationFields = fetchRegisterInputs();

            // TODO: Validate inputs and add conditional logic, right now it only returns true
            if (registerViewModel.registerValidation(registrationFields)) {
                // Register user if validated
                registerViewModel.registerUser(registrationFields);
            } else {
                // TODO: conditional logic
            }
        });
    }

    /**
     * Data Extraction: Acts as a bridge between the XML UI components and the raw data
     * needed by the Repository layer.
     */
    private String[] fetchRegisterInputs() {
        String[] registrationFields = new String[5];

        // Fetch each field
        registrationFields[0] = binding.editFirstName.getText().toString();
        registrationFields[1] = binding.editLastName.getText().toString();
        registrationFields[2] = binding.editEmail.getText().toString();
        registrationFields[3] = binding.editPhone.getText().toString();
        registrationFields[4] = binding.editPassword.getText().toString();

        return registrationFields;
    }

    /**
     * Reactive Programming: The View 'listens' for state changes rather than
     * asking for updates, allowing for a decoupled, event-driven UI.
     */
    private void setupObservers() {
        // Watch for any errors on registration
        registerViewModel.getErrorMessage().observe(this, error -> {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });

        // Watch for the signal that tells the app to navigate to the main screen
        registerViewModel.getNavigateToMain().observe(this, navigateToMain -> {
            if (navigateToMain) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("REGISTER_SUCCESS", "Account successfully created!");
                startActivity(intent);
                finish();
            }
        });
    }
}