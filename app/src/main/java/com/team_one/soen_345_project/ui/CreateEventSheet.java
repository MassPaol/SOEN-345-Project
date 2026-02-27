package com.team_one.soen_345_project.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.team_one.soen_345_project.R;
import com.team_one.soen_345_project.viewmodel.admindash.AdminDashViewModel;

import java.util.HashMap;

public class CreateEventSheet extends BottomSheetDialogFragment {
    // Calendar attributes
    private long selectedDate;
    private String selectedTime;
    private final AdminDashViewModel adminDashViewModel = new AdminDashViewModel();
    private static final String TAG = "CreateEventSheet";


    // Connect XMl to this class
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_eventsheet, container, false);
    }

    // On view inflation add the listeners for on button clicking behaviour
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup listeners
        setupListeners(view);
    }

    public HashMap<String, String> fetchEventInputs(View view) {
        HashMap<String, String> eventFields = new HashMap<>();

        EditText eventTitleInput = view.findViewById(R.id.et_title);
        EditText eventDiscInput = view.findViewById(R.id.et_description);
        EditText eventLocationInput = view.findViewById(R.id.et_location);
        EditText eventCapacityInput = view.findViewById(R.id.et_capacity);
        EditText eventPriceInput = view.findViewById(R.id.et_price);
        EditText eventCategoryInput = view.findViewById(R.id.et_category);

        eventFields.put("title", eventTitleInput.getText().toString());
        eventFields.put("disc", eventDiscInput.getText().toString());
        eventFields.put("location", eventLocationInput.getText().toString());
        eventFields.put("capacity", eventCapacityInput.getText().toString());
        eventFields.put("price", eventPriceInput.getText().toString());
        eventFields.put("category", eventCategoryInput.getText().toString());

        return eventFields;
    }

    public void setupListeners(View view) {
        // Get all relevant buttons
        Button btnDateTime = view.findViewById(R.id.btn_pick_date);
        Button btnSaveEvent = view.findViewById(R.id.btn_save_event);

        // When the date time button is clicked get the choice from the user
        btnDateTime.setOnClickListener(v -> {
            // Initialize Date Picker
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("1. Select Date")
                    .build();

            datePicker.show(getChildFragmentManager(), "DATE_PICKER");

            // When Date is picked, immediately launch Time Picker
            datePicker.addOnPositiveButtonClickListener(selection -> {
                selectedDate = selection;
                String dateString = datePicker.getHeaderText();

                // Initialize Time Picker
                MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_12H)
                        .setTitleText("2. Select Time")
                        .build();

                timePicker.show(getChildFragmentManager(), "TIME_PICKER");

                // When Time is picked, update the Button text
                timePicker.addOnPositiveButtonClickListener(v2 -> {
                    int hour = timePicker.getHour();
                    int minute = timePicker.getMinute();
                    selectedTime = String.format("%02d:%02d", hour, minute);

                    // Update the button to show both
                    btnDateTime.setText(String.format("%s at %s", dateString, selectedTime));
                });
            });
        });

        // When the save event button is clicked get all info from the form and send it to ViewModel
        btnSaveEvent.setOnClickListener(v -> {
            // Collect data
            HashMap<String, String> eventFields = fetchEventInputs(view);

            // Add the date & time to the hashmap
            eventFields.put("date", String.valueOf(selectedDate));
            eventFields.put("time", selectedTime);

            // Send info to ViewModel
            adminDashViewModel.saveEvent(eventFields);
        });

        adminDashViewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            // Show message (Toast)
            if (state.getMessage() != null) {
                if (state.isActionComplete()) {
                    Log.i(TAG, "Successfully created a new event!");

                    Toast.makeText(getContext(), state.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Failed to create a new event.");

                    Toast.makeText(getContext(), state.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            // Check if we should close the sheet
            if (state.isActionComplete()) {
                dismiss();
            }
        });
    }
}
