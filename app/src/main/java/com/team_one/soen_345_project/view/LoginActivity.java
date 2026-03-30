package com.team_one.soen_345_project.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.team_one.soen_345_project.R;
import com.team_one.soen_345_project.databinding.ActivityLoginBinding;
import com.team_one.soen_345_project.viewmodel.login.LoginViewModel;

/**
 * LoginActivity - Handles user login functionality
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final String EXTRA_REGISTER_SUCCESS = "REGISTER_SUCCESS";
    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;
    private ProgressBar loadingSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize view binding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingSpinner = findViewById(R.id.loginProgress);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        showRegistrationSuccessMessage();
        setupListeners();
        observeViewModel();
    }

    private void showRegistrationSuccessMessage() {
        String message = getIntent().getStringExtra(EXTRA_REGISTER_SUCCESS);
        if (message != null && !message.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            getIntent().removeExtra(EXTRA_REGISTER_SUCCESS);
        }
    }

    private void setupListeners() {
        // Login button click listener
        binding.buttonLogin.setOnClickListener(v -> {
            String email = binding.editTextEmail.getText().toString().trim();
            String password = binding.editTextPassword.getText().toString().trim();
            viewModel.onLoginClicked(email, password);
        });

        // Register link click listener
        binding.textViewRegisterLink.setOnClickListener(v -> {
            Log.i(TAG, "Navigating to RegisterActivity from LoginActivity");
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        // Observe the state
        viewModel.getUiState().observe(this, state -> {
            if (state == null) return;

            // Handle the Spinner
            if (state.isLoading()) {
                loadingSpinner.setVisibility(View.VISIBLE);
            } else {
                loadingSpinner.setVisibility(View.GONE);
            }

            // Handle Redirection/Success
            if (state.isSuccess()) {
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();

                // Check if they are an admin, redirect accordingly
                Class<?> destination = state.isAdmin() ? AdminDashActivity.class : UserDashActivity.class;

                Intent intent = new Intent(this, destination);
                startActivity(intent);
                finish();
            }

            // Handle Errors
            if (state.getErrorMessage() != null) {
                Toast.makeText(this, state.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }

            binding.buttonLogin.setEnabled(!state.isLoading());
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
