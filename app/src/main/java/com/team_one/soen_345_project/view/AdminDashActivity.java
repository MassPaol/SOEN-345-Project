package com.team_one.soen_345_project.view;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.team_one.soen_345_project.databinding.ActivityAdmindashBinding;
import com.team_one.soen_345_project.databinding.ActivityRegisterBinding;
import com.team_one.soen_345_project.ui.CreateEventSheet;
import com.team_one.soen_345_project.viewmodel.admindash.AdminDashViewModel;

public class AdminDashActivity extends AppCompatActivity {
    private static final String TAG = "AdminDashActivity";
    private ActivityAdmindashBinding binding;

    // ViewModel object for interaction with ViewModel layer
    private final AdminDashViewModel adminDashViewModel = new AdminDashViewModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called - AdminDashActivity instance created");

        // View Binding: Replaces findViewById to ensure type-safe access to layout views
        binding = ActivityAdmindashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // UI/UX: Extends the layout into system status/navigation bars for a modern edge-to-edge look
        EdgeToEdge.enable(this);

        // Initialize Observers early so we don't miss any state emissions from the ViewModel
//        setupObservers();

        // ----- UI Listeners -----
        binding.btnCreateEvent.setOnClickListener(v -> {
            Log.i(TAG, "Opening 'Create Event' Form");

            // Event Add popup
            openEventSheet();
        });
    }

    // Event sheet slide up form
    public void openEventSheet() {
        CreateEventSheet bottomSheet = new CreateEventSheet();
        // 'getSupportFragmentManager' is the manager that handles fragment transactions
        bottomSheet.show(getSupportFragmentManager(), "CreateEventTag");
    }

}
