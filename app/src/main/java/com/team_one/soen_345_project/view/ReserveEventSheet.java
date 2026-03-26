package com.team_one.soen_345_project.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.team_one.soen_345_project.R;
import com.team_one.soen_345_project.di.Injection;
import com.team_one.soen_345_project.model.entity.Event;
import com.team_one.soen_345_project.model.repository.IReservationRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReserveEventSheet extends BottomSheetDialogFragment {

    private static final String ARG_EVENT_ID = "event_id";

    public interface EventProvider {
        @Nullable Event getEventById(@NonNull String eventId);
    }

    public interface OnBookingSuccessListener {
        void onBookingSuccess(@NonNull String eventId);
    }

    private EventProvider eventProvider;
    private OnBookingSuccessListener onBookingSuccessListener;

    public void setEventProvider(@Nullable EventProvider eventProvider) {
        this.eventProvider = eventProvider;
    }

    public void setOnBookingSuccessListener(@Nullable OnBookingSuccessListener listener) {
        this.onBookingSuccessListener = listener;
    }

    public static ReserveEventSheet newInstance(@NonNull String eventId) {
        ReserveEventSheet fragment = new ReserveEventSheet();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reserve_event, container, false);
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

        final Event selectedEvent = event;
        final String selectedEventId = selectedEvent.getEventId();

        TextView tvTitle               = view.findViewById(R.id.tvSheetEventTitle);
        TextView tvDate                = view.findViewById(R.id.tvSheetEventDate);
        TextView tvLocation            = view.findViewById(R.id.tvSheetEventLocation);
        TextView tvCategory            = view.findViewById(R.id.tvSheetEventCategory);
        TextView tvDescription         = view.findViewById(R.id.tvSheetEventDescription);
        TextView tvCapacity            = view.findViewById(R.id.tvSheetEventCapacity);
        TextView tvPrice               = view.findViewById(R.id.tvSheetEventPrice);
        TextView tvEventFullMessage    = view.findViewById(R.id.tvEventFullMessage);
        TextView tvAlreadyBookedMessage = view.findViewById(R.id.tvAlreadyBookedMessage);
        Button btnBookNow              = view.findViewById(R.id.btnBookNow);
        Button btnCancelBooking        = view.findViewById(R.id.btnCancelBooking);

        // --- Populate fields ---

        tvTitle.setText(event.getTitle() != null ? event.getTitle() : "");

        if (event.getDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            tvDate.setText(dateFormat.format(event.getDate().toDate()));
        } else {
            tvDate.setText("Date not set");
        }

        tvLocation.setText(event.getLocation() != null ? event.getLocation() : "");
        tvCategory.setText(event.getCategory() != null ? event.getCategory() : "");
        tvDescription.setText(event.getDescription() != null ? event.getDescription() : "");

        int availableSpots = Math.max(0, selectedEvent.getCapacity() - selectedEvent.getReservations());
        tvCapacity.setText(String.format(Locale.getDefault(),
                "Capacity: %d (%d available)", selectedEvent.getCapacity(), availableSpots));
        tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", selectedEvent.getPrice()));

        boolean isFull = availableSpots <= 0;

        // --- Default UI state (before async booking check) ---
        tvEventFullMessage.setVisibility(isFull ? View.VISIBLE : View.GONE);
        tvAlreadyBookedMessage.setVisibility(View.GONE);
        btnCancelBooking.setVisibility(View.GONE);
        btnBookNow.setVisibility(isFull ? View.GONE : View.VISIBLE);

        // --- Async: check if user already booked, then update UI ---
        if (selectedEventId != null) {
            IReservationRepository repo = Injection.provideReservationRepository();
            repo.isEventBookedByCurrentUser(selectedEventId, (message, booked) -> {
                if (!isAdded()) return;

                if (booked) {
                    tvAlreadyBookedMessage.setVisibility(View.VISIBLE);
                    btnCancelBooking.setVisibility(View.VISIBLE);
                    tvEventFullMessage.setVisibility(View.GONE);
                    btnBookNow.setVisibility(View.GONE);
                } else {
                    tvAlreadyBookedMessage.setVisibility(View.GONE);
                    btnCancelBooking.setVisibility(View.GONE);
                    tvEventFullMessage.setVisibility(isFull ? View.VISIBLE : View.GONE);
                    btnBookNow.setVisibility(isFull ? View.GONE : View.VISIBLE);
                }
            });
        }

        // --- Book button ---
        btnBookNow.setOnClickListener(v -> {
            if (selectedEventId == null || selectedEventId.trim().isEmpty()) {
                Toast.makeText(requireContext(), "Invalid event", Toast.LENGTH_LONG).show();
                return;
            }

            btnBookNow.setEnabled(false);
            IReservationRepository repo = Injection.provideReservationRepository();
            repo.bookEvent(selectedEventId, (message, success) -> {
                if (!isAdded()) return;

                Toast.makeText(requireContext(),
                        success ? "Event booked successfully"
                                : (message != null ? message : "Failed to book event"),
                        Toast.LENGTH_LONG).show();

                btnBookNow.setEnabled(true);

                if (!success && message != null) {
                    String lower = message.toLowerCase(Locale.getDefault());
                    if (lower.contains("no spots") || lower.contains("already")) {
                        btnBookNow.setVisibility(View.GONE);
                    }
                }

                if (success) {
                    if (onBookingSuccessListener != null) {
                        onBookingSuccessListener.onBookingSuccess(selectedEventId);
                    }
                    dismiss();
                }
            });
        });

        // --- Cancel button ---
        btnCancelBooking.setOnClickListener(v -> {
            btnCancelBooking.setEnabled(false);
            IReservationRepository repo = Injection.provideReservationRepository();
            repo.cancelEvent(selectedEventId, (message, success) -> {
                if (!isAdded()) return;

                Toast.makeText(requireContext(),
                        success ? "Reservation cancelled"
                                : (message != null ? message : "Failed to cancel reservation"),
                        Toast.LENGTH_LONG).show();

                btnCancelBooking.setEnabled(true);

                if (success) {
                    if (onBookingSuccessListener != null) {
                        onBookingSuccessListener.onBookingSuccess(selectedEventId);
                    }
                    dismiss();
                }
            });
        });
    }
}