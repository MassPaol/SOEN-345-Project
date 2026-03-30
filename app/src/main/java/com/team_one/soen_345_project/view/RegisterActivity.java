package com.team_one.soen_345_project.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.team_one.soen_345_project.databinding.ActivityRegisterBinding;
import com.team_one.soen_345_project.viewmodel.register.RegisterViewModel;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private static final String EXTRA_REGISTER_SUCCESS = "REGISTER_SUCCESS";
    private ActivityRegisterBinding binding;

    // ViewModel object for interaction with ViewModel layer
    private final RegisterViewModel registerViewModel = new RegisterViewModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called - RegisterActivity instance created");

        // View Binding: Replaces findViewById to ensure type-safe access to layout views
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // UI/UX: Extends the layout into system status/navigation bars for a modern edge-to-edge look
        EdgeToEdge.enable(this);

        // Initialize Observers early so we don't miss any state emissions from the ViewModel
        setupObservers();

        // ----- UI Listeners -----

        binding.btnRegister.setOnClickListener(v -> {
            Log.i(TAG, "Starting validation + registration process for new user");

            // Collect data first
            String[] registrationFields = fetchRegisterInputs();

            // Then send to ViewModel for processing (validation + registration)
            registerViewModel.onRegisterClicked(registrationFields);

        });

        binding.textViewLoginLink.setOnClickListener(v -> {
            Log.i(TAG, "Navigating to LoginActivity from RegisterActivity");
            finish(); // Just go back to LoginActivity instead of creating a new instance
        });
    }

    /**
     * Data Extraction: Acts as a bridge between the XML UI components and the raw data
     * needed by the Repository layer.
     * Array includes confirmPassword for validation purposes.
     */
    private String[] fetchRegisterInputs() {
        String[] registrationFields = new String[6]; // Changed to 6 to include confirmPassword

        // Fetch each field
        registrationFields[0] = binding.editFirstName.getText().toString();
        registrationFields[1] = binding.editLastName.getText().toString();
        registrationFields[2] = binding.editEmail.getText().toString();
        registrationFields[3] = binding.editPhone.getText().toString();
        registrationFields[4] = binding.editPassword.getText().toString();
        registrationFields[5] = binding.editConfirmPassword.getText().toString();

        return registrationFields;
    }

    /**
     * Clear all error messages from input fields
     */
    private void clearErrors() {
        binding.layoutFirstName.setError(null);
        binding.layoutLastName.setError(null);
        binding.layoutEmail.setError(null);
        binding.layoutPhone.setError(null);
        binding.layoutPassword.setError(null);
        binding.layoutConfirmPassword.setError(null);
    }

    /**
     * Display validation errors from the ValidationResult.
     */
    private void showValidationErrors(Map<String, String> validationResult) {
        Map<String, com.google.android.material.textfield.TextInputLayout> fields = new HashMap<>();
        fields.put("firstName", binding.layoutFirstName);
        fields.put("lastName", binding.layoutLastName);
        fields.put("email", binding.layoutEmail);
        fields.put("phone", binding.layoutPhone);
        fields.put("password", binding.layoutPassword);
        fields.put("confirmPassword", binding.layoutConfirmPassword);

        for (Map.Entry<String, String> e : validationResult.entrySet()) {
            TextInputLayout field = fields.get(e.getKey());
            if (field != null) {
                field.setError(e.getValue());
                Log.e(TAG, "Validation error on " + e.getKey() + ": " + e.getValue());
            }
        }
    }

    /**
     * Reactive Programming: The View 'listens' for state changes rather than
     * asking for updates, allowing for a decoupled, event-driven UI.
     */
    private void setupObservers() {

        // Watch for the signal that tells the app to return to login after successful registration
        registerViewModel.getNavigateToMain().observe(this, navigateToMain -> {
            if (navigateToMain) {
                Log.i(TAG, "Registration successful - navigating back to LoginActivity");
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra(EXTRA_REGISTER_SUCCESS, "Account successfully created!");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });

        registerViewModel.getUiState().observe(this, state -> {

            clearErrors();

            if (state.getValidationErrors() != null && !state.getValidationErrors().isEmpty()) {
                Log.i(TAG, "Invalid registration input - showing validation errors");
                showValidationErrors(state.getValidationErrors());
                Toast.makeText(this, "Please fix the errors above", Toast.LENGTH_LONG).show();

            }

            if (state.getGeneralError() != null && !state.getGeneralError().isEmpty()) {
                Toast.makeText(RegisterActivity.this, state.getGeneralError(), Toast.LENGTH_LONG).show();
            }
        });
    }
}