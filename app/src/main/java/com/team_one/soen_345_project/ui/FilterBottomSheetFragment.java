package com.team_one.soen_345_project.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.team_one.soen_345_project.R;
import com.team_one.soen_345_project.model.util.filter.CategoryFilterOption;
import com.team_one.soen_345_project.model.util.filter.FilterState;
import com.team_one.soen_345_project.model.util.filter.LocationFilterOption;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class FilterBottomSheetFragment extends BottomSheetDialogFragment {

    // Interfaces and Data types
    public interface OnFilterAppliedListener {
        void onFilterApplied(FilterState filterState);
    }

    private OnFilterAppliedListener listener;

    // UI Elements
    private AutoCompleteTextView actvCategory;
    private AutoCompleteTextView actvLocation;
    private TextInputEditText etDateRange;
    private CheckBox cbAvailableSpots;
    private TextInputEditText etMinPrice;
    private TextInputEditText etMaxPrice;
    private MaterialButton btnReset;
    private MaterialButton btnApply;

    // Local state for dates
    private Timestamp selectedStartDate;
    private Timestamp selectedEndDate;

    // Format for displaying selected dates
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    /**
     * Static factory method
     */
    public static FilterBottomSheetFragment newInstance(
            OnFilterAppliedListener listener) {

        FilterBottomSheetFragment fragment = new FilterBottomSheetFragment();
        // Storing directly in fields.
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the XML layout we created earlier
        return inflater.inflate(R.layout.fragment_filter_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        setupCategoryDropdown();
        setupLocationDropdown();
        setupDateRangePicker();
        setupButtons();
    }

    private void bindViews(View view) {
        actvCategory = view.findViewById(R.id.actvCategory);
        actvLocation = view.findViewById(R.id.actvLocation);
        etDateRange = view.findViewById(R.id.etDateRange);
        cbAvailableSpots = view.findViewById(R.id.cbAvailableSpots);
        etMinPrice = view.findViewById(R.id.etMinPrice);
        etMaxPrice = view.findViewById(R.id.etMaxPrice);
        btnReset = view.findViewById(R.id.btnReset);
        btnApply = view.findViewById(R.id.btnApply);
    }

    private void setupCategoryDropdown() {
        CategoryFilterOption[] categories = CategoryFilterOption.values();
        String[] categoryNames = new String[categories.length];
        for (int i = 0; i < categories.length; i++) {
            categoryNames[i] = categories[i].toString();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categoryNames
        );
        actvCategory.setAdapter(adapter);
    }

    private void setupLocationDropdown() {
        LocationFilterOption[] locations = LocationFilterOption.values();
        String[] locationNames = new String[locations.length];
        for (int i = 0; i < locations.length; i++) {
            locationNames[i] = locations[i].toString();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                locationNames
        );
        actvLocation.setAdapter(adapter);
    }

    private void setupDateRangePicker() {
        etDateRange.setOnClickListener(v -> {
            MaterialDatePicker<Pair<Long, Long>> dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Select Date Range")
                    .build();

            dateRangePicker.addOnPositiveButtonClickListener(selection -> {
                if (selection.first != null && selection.second != null) {
                    // Wrap milliseconds into Timestamps
                    selectedStartDate = convertToTimestamp(selection.first);
                    selectedEndDate = convertToTimestamp(selection.second);

                    // Update UI
                    String startDateString = dateFormat.format(selectedStartDate.toDate());
                    String endDateString = dateFormat.format(selectedEndDate.toDate());
                    etDateRange.setText(String.format("%s - %s", startDateString, endDateString));
                }
            });

            dateRangePicker.show(getParentFragmentManager(), "DATE_RANGE_PICKER");
        });
    }

    private void setupButtons() {
        btnApply.setOnClickListener(v -> {
            if (listener != null) {
                FilterState state = buildCurrentFilterState();
                listener.onFilterApplied(state);
                dismiss();
            }
        });

        btnReset.setOnClickListener(v -> {
            // Reset UI
            actvCategory.setText("", false);
            actvLocation.setText("", false);
            etDateRange.setText("");
            cbAvailableSpots.setChecked(false);
            etMinPrice.setText("");
            etMaxPrice.setText("");

            // Clear local date state
            selectedStartDate = null;
            selectedEndDate = null;

            if (listener != null) {
                // Fire listener with empty state
                listener.onFilterApplied(new FilterState());
                dismiss();
            }
        });
    }

    private FilterState buildCurrentFilterState() {
        FilterState state = new FilterState();

        // Category enum selection logic
        String cat = actvCategory.getText().toString();
        CategoryFilterOption catEnum;
        if (cat.isEmpty()) {
            catEnum = CategoryFilterOption.ALL;
        } else {
            catEnum = CategoryFilterOption.valueOf(cat);
        }

        // Location enum selection logic
        String loc = actvLocation.getText().toString();
        LocationFilterOption locEnum;
        if (loc.isEmpty()) {
            locEnum = LocationFilterOption.ALL;
        } else {
            locEnum = LocationFilterOption.valueOf(loc);
        }

        // Populate state based on current UI.
        state.setCategory(catEnum);
        state.setLocation(locEnum);
        state.setDateFrom(selectedStartDate);
        state.setDateTo(selectedEndDate);
        state.setAvailableOnly(cbAvailableSpots.isChecked());

        String minPriceStr = etMinPrice.getText() != null ? etMinPrice.getText().toString() : "";
        String maxPriceStr = etMaxPrice.getText() != null ? etMaxPrice.getText().toString() : "";

        if (!minPriceStr.isEmpty()) {
            state.setMinPrice(Double.parseDouble(minPriceStr));
        }
        if (!maxPriceStr.isEmpty()) {
            state.setMaxPrice(Double.parseDouble(maxPriceStr));
        }

        return state;
    }

    private Timestamp convertToTimestamp(long milliseconds) {
        // Picker returns UTC midnight — extract date components in UTC
        Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcCalendar.setTimeInMillis(milliseconds);

        int year = utcCalendar.get(Calendar.YEAR);
        int month = utcCalendar.get(Calendar.MONTH);
        int day = utcCalendar.get(Calendar.DAY_OF_MONTH);

        // Rebuild in local timezone at midnight
        Calendar localCalendar = Calendar.getInstance();
        localCalendar.set(year, month, day, 0, 0, 0);
        localCalendar.set(Calendar.MILLISECOND, 0);

        return new Timestamp(localCalendar.getTime());
    }
}
