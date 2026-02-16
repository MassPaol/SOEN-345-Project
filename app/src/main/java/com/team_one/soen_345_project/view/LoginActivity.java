package com.team_one.soen_345_project.view;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.team_one.soen_345_project.R;
import com.team_one.soen_345_project.databinding.ActivityLoginBinding;

/**
 * LoginActivity - Handles user login functionality
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize view binding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // TODO: Add login logic here
        setupListeners();
    }

    private void setupListeners() {
        // Login button click listener
        binding.buttonLogin.setOnClickListener(v -> {
            // TODO: Implement login logic
        });

        // Register link click listener
        binding.textViewRegisterLink.setOnClickListener(v -> {
            // TODO: Navigate to RegisterActivity
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}

