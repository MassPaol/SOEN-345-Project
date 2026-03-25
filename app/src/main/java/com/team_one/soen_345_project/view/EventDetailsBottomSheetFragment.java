package com.team_one.soen_345_project.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.team_one.soen_345_project.R;
import com.team_one.soen_345_project.model.entity.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventDetailsBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_EVENT_ID = "event_id";

    public interface EventProvider {
        @Nullable Event getEventById(@NonNull String eventId);
    }

    private EventProvider eventProvider;

    public void setEventProvider(@Nullable EventProvider eventProvider) {
        this.eventProvider = eventProvider;
    }

    public static EventDetailsBottomSheetFragment newInstance(@NonNull String eventId) {
        EventDetailsBottomSheetFragment fragment = new EventDetailsBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String eventId = null;
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
        }

        Event event = null;
        if (eventId != null && eventProvider != null) {
            event = eventProvider.getEventById(eventId);
        }

        if (event == null) {
            dismiss();
            return;
        }

        TextView tvTitle = view.findViewById(R.id.tvSheetEventTitle);
        TextView tvDate = view.findViewById(R.id.tvSheetEventDate);
        TextView tvLocation = view.findViewById(R.id.tvSheetEventLocation);
        TextView tvCategory = view.findViewById(R.id.tvSheetEventCategory);
        TextView tvDescription = view.findViewById(R.id.tvSheetEventDescription);
        TextView tvCapacity = view.findViewById(R.id.tvSheetEventCapacity);
        TextView tvPrice = view.findViewById(R.id.tvSheetEventPrice);
        Button btnBookNow = view.findViewById(R.id.btnBookNow);

        tvTitle.setText(event.getTitle() != null ? event.getTitle() : "");

        if (event.getDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            Date date = event.getDate().toDate();
            tvDate.setText(dateFormat.format(date));
        } else {
            tvDate.setText("Date not set");
        }

        tvLocation.setText(event.getLocation() != null ? event.getLocation() : "");
        tvCategory.setText(event.getCategory() != null ? event.getCategory() : "");
        tvDescription.setText(event.getDescription() != null ? event.getDescription() : "");

        int availableSpots = Math.max(0, event.getCapacity() - event.getReservations());
        tvCapacity.setText(String.format(Locale.getDefault(), "Capacity: %d (%d available)", event.getCapacity(), availableSpots));
        tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", event.getPrice()));

        btnBookNow.setVisibility(availableSpots > 0 ? View.VISIBLE : View.GONE);

        // TODO: Hook booking logic to ViewModel / repository.
        btnBookNow.setOnClickListener(v -> {
            // Handle booking
        });
    }
}
