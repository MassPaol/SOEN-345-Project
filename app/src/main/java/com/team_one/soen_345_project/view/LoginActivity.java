package com.team_one.soen_345_project.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import com.team_one.soen_345_project.databinding.ActivityLoginBinding;

/**
 * LoginActivity - Handles user login functionality
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
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
            Log.i(TAG, "Navigating to RegisterActivity from LoginActivity");
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}

