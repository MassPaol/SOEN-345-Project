package com.team_one.soen_345_project.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.team_one.soen_345_project.databinding.ActivityLoginBinding;
import com.team_one.soen_345_project.viewmodel.login.LoginViewModel;

/**
 * LoginActivity - Handles user login functionality
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize view binding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        setupListeners();
        observeViewModel();
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
        viewModel.getUiState().observe(this, state -> {
            if (state.getErrorMessage() != null) {
                Toast.makeText(this, state.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }

            if (state.isSuccess()) {
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
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
