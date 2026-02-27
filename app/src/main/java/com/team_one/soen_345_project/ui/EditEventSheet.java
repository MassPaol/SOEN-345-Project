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
import com.google.firebase.Timestamp;
import com.team_one.soen_345_project.R;
import com.team_one.soen_345_project.model.entity.Event;
import com.team_one.soen_345_project.viewmodel.admindash.AdminDashViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class EditEventSheet extends BottomSheetDialogFragment {
    // Calendar attributes
    private long selectedDate;
    private String selectedTime;
    private AdminDashViewModel adminDashViewModel;
    private Event event;
    private static final String TAG = "EditEventSheet";

    // Setter method to inject the ViewModel from the Activity
    public void setViewModel(AdminDashViewModel viewModel) {
        this.adminDashViewModel = viewModel;
    }

    // Setter method to pass the event to be edited
    public void setEvent(Event event) {
        this.event = event;
    }

    // Connect XML to this class
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_editeventsheet, container, false);
    }

    // On view inflation add the listeners for on button clicking behaviour
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Pre-populate fields with existing event data
        if (event != null) {
            populateFields(view);
        }

        // Setup listeners
        setupListeners(view);
    }

    // Pre-populate the form fields with existing event data
    private void populateFields(View view) {
        EditText etTitle = view.findViewById(R.id.et_title);
        EditText etDescription = view.findViewById(R.id.et_description);
        EditText etLocation = view.findViewById(R.id.et_location);
        EditText etCapacity = view.findViewById(R.id.et_capacity);
        EditText etPrice = view.findViewById(R.id.et_price);
        EditText etCategory = view.findViewById(R.id.et_category);
        Button btnDateTime = view.findViewById(R.id.btn_pick_date);

        // Set text fields
        etTitle.setText(event.getTitle());
        etDescription.setText(event.getDescription());
        etLocation.setText(event.getLocation());
        etCapacity.setText(String.valueOf(event.getCapacity()));
        etPrice.setText(String.valueOf(event.getPrice()));
        etCategory.setText(event.getCategory());

        // Set date and time
        if (event.getDate() != null) {
            Date date = event.getDate().toDate();

            // Store the date timestamp in milliseconds
            selectedDate = date.getTime();

            // Extract time from the date
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            selectedTime = String.format("%02d:%02d", hour, minute);

            // Format and display on button
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String dateString = dateFormat.format(date);
            String timeString = timeFormat.format(date);

            btnDateTime.setText(String.format("%s at %s", dateString, timeString));
        }
    }

    private HashMap<String, Object> fetchUpdatedFields(View view) {
        HashMap<String, Object> updatedFields = new HashMap<>();

        EditText etTitle = view.findViewById(R.id.et_title);
        EditText etDescription = view.findViewById(R.id.et_description);
        EditText etLocation = view.findViewById(R.id.et_location);
        EditText etCapacity = view.findViewById(R.id.et_capacity);
        EditText etPrice = view.findViewById(R.id.et_price);
        EditText etCategory = view.findViewById(R.id.et_category);

        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String capacityStr = etCapacity.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String category = etCategory.getText().toString().trim();

        // Add all fields
        updatedFields.put("title", title);
        updatedFields.put("description", description);
        updatedFields.put("location", location);
        updatedFields.put("category", category);

        // Parse capacity
        try {
            int capacity = Integer.parseInt(capacityStr);
            updatedFields.put("capacity", capacity);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid capacity format");
        }

        // Parse price
        try {
            double price = Double.parseDouble(priceStr);
            updatedFields.put("price", price);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid price format");
        }

        // Add date/time
        if (selectedDate != 0 && selectedTime != null) {
            // MaterialDatePicker returns UTC midnight for selected date
            // Extract year/month/day using UTC calendar to get correct date
            Calendar utcCalendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
            utcCalendar.setTimeInMillis(selectedDate);

            int year = utcCalendar.get(Calendar.YEAR);
            int month = utcCalendar.get(Calendar.MONTH);
            int day = utcCalendar.get(Calendar.DAY_OF_MONTH);

            // Parse the selected time
            int hour = Integer.parseInt(selectedTime.substring(0, 2));
            int minute = Integer.parseInt(selectedTime.substring(3));

            // Create a new calendar in local timezone with the selected date and time
            Calendar localCalendar = Calendar.getInstance();
            localCalendar.set(year, month, day, hour, minute, 0);
            localCalendar.set(Calendar.MILLISECOND, 0);

            Timestamp newTimestamp = new Timestamp(localCalendar.getTime());
            updatedFields.put("date", newTimestamp);
        }

        return updatedFields;
    }

    private void setupListeners(View view) {
        // Get all relevant buttons
        Button btnDateTime = view.findViewById(R.id.btn_pick_date);
        Button btnSaveEvent = view.findViewById(R.id.btn_save_event);

        // When the date time button is clicked get the choice from the user
        btnDateTime.setOnClickListener(v -> {
            // Initialize Date Picker with current date if available
            MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("1. Select Date");

            if (selectedDate != 0) {
                builder.setSelection(selectedDate);
            }

            MaterialDatePicker<Long> datePicker = builder.build();
            datePicker.show(getChildFragmentManager(), "DATE_PICKER");

            // When Date is picked, immediately launch Time Picker
            datePicker.addOnPositiveButtonClickListener(selection -> {
                selectedDate = selection;
                String dateString = datePicker.getHeaderText();

                // Initialize Time Picker with current time if available
                MaterialTimePicker.Builder timeBuilder = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_12H)
                        .setTitleText("2. Select Time");

                if (selectedTime != null) {
                    int hour = Integer.parseInt(selectedTime.substring(0, 2));
                    int minute = Integer.parseInt(selectedTime.substring(3));
                    timeBuilder.setHour(hour).setMinute(minute);
                }

                MaterialTimePicker timePicker = timeBuilder.build();
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

        // When the save changes button is clicked, collect updates and send to ViewModel
        btnSaveEvent.setOnClickListener(v -> {
            if (adminDashViewModel == null) {
                Log.e(TAG, "ViewModel is null! Cannot update event.");
                Toast.makeText(getContext(), "Error: ViewModel not initialized", Toast.LENGTH_SHORT).show();
                return;
            }

            if (event == null || event.getEventId() == null) {
                Log.e(TAG, "Event or EventId is null! Cannot update event.");
                Toast.makeText(getContext(), "Error: Event not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Collect updated data
            HashMap<String, Object> updatedFields = fetchUpdatedFields(view);


            Log.d(TAG, "Updating event " + event.getEventId() + " with " + updatedFields.size() + " fields");

            // Send update to ViewModel
            adminDashViewModel.updateEvent(event.getEventId(), updatedFields);
        });

        // Observe state changes
        if (adminDashViewModel != null) {
            adminDashViewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
                if (state == null) return;

                // Show message (Toast)
                if (state.getMessage() != null) {
                    if (state.isActionComplete()) {
                        Log.i(TAG, "Successfully updated event!");
                        Toast.makeText(getContext(), state.getMessage(), Toast.LENGTH_SHORT).show();
                    } else if (state.getMessage().contains("Failed")) {
                        Log.e(TAG, "Failed to update event.");
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
}

