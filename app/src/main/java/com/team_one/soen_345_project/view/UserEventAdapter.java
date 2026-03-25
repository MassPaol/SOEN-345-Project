package com.team_one.soen_345_project.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.team_one.soen_345_project.R;
import com.team_one.soen_345_project.model.entity.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Read-only event adapter for the User Dashboard.
 */
public class UserEventAdapter extends RecyclerView.Adapter<UserEventAdapter.UserEventViewHolder> {

    private List<Event> events = new ArrayList<>();
    private Set<String> bookedEventIds = Collections.emptySet();
    private OnItemClickListener listener;
    private boolean showStatus = true;

    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setEvents(List<Event> events) {
        this.events = events != null ? events : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setBookedEventIds(Set<String> bookedEventIds) {
        this.bookedEventIds = bookedEventIds != null ? new HashSet<>(bookedEventIds) : Collections.emptySet();
        notifyDataSetChanged();
    }

    public void setShowStatus(boolean showStatus) {
        this.showStatus = showStatus;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new UserEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserEventViewHolder holder, int position) {
        Event event = events.get(position);
        boolean isBooked = event != null && event.getEventId() != null && bookedEventIds.contains(event.getEventId());
        holder.bind(event, isBooked, showStatus);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class UserEventViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEventTitle;
        private final TextView tvEventDate;
        private final TextView tvEventLocation;
        private final TextView tvEventDescription;
        private final TextView tvEventCategory;
        private final TextView tvEventCapacity;
        private final TextView tvEventPrice;
        private final ImageButton btnEventMenu;
        private final TextView tvEventStatusPill;

        public UserEventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventTitle       = itemView.findViewById(R.id.tvEventTitle);
            tvEventDate        = itemView.findViewById(R.id.tvEventDate);
            tvEventLocation    = itemView.findViewById(R.id.tvEventLocation);
            tvEventDescription = itemView.findViewById(R.id.tvEventDescription);
            tvEventCategory    = itemView.findViewById(R.id.tvEventCategory);
            tvEventCapacity    = itemView.findViewById(R.id.tvEventCapacity);
            tvEventPrice       = itemView.findViewById(R.id.tvEventPrice);
            btnEventMenu       = itemView.findViewById(R.id.btnEventMenu);
            tvEventStatusPill  = itemView.findViewById(R.id.tvEventStatusPill);

            // Users cannot edit or delete events – hide the admin menu button
            btnEventMenu.setVisibility(View.GONE);
        }

        public void bind(Event event, boolean isBooked, boolean showStatus) {
            if (event == null) return;

            boolean isFull = event.getReservations() >= event.getCapacity();

            if (!showStatus) {
                tvEventStatusPill.setVisibility(View.GONE);
            } else {
                tvEventStatusPill.setVisibility(View.VISIBLE);

                if (isBooked) {
                    tvEventStatusPill.setText("BOOKED");
                    tvEventStatusPill.setBackgroundResource(R.drawable.bg_booked_badge);
                    itemView.setAlpha(0.90f);
                } else if (isFull) {
                    tvEventStatusPill.setText("FULL");
                    tvEventStatusPill.setBackgroundResource(R.drawable.bg_status_full);
                    itemView.setAlpha(1.0f);
                } else {
                    tvEventStatusPill.setText("AVAILABLE");
                    tvEventStatusPill.setBackgroundResource(R.drawable.bg_status_available);
                    itemView.setAlpha(1.0f);
                }
            }

            tvEventTitle.setText(event.getTitle());

            if (event.getDate() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(
                        "MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
                Date date = event.getDate().toDate();
                tvEventDate.setText(dateFormat.format(date));
            } else {
                tvEventDate.setText("Date not set");
            }

            tvEventLocation.setText(event.getLocation());
            tvEventDescription.setText(event.getDescription());
            tvEventCategory.setText(event.getCategory());
            tvEventCapacity.setText(
                    String.format(Locale.getDefault(), "Capacity: %d", event.getCapacity()));
            tvEventPrice.setText(
                    String.format(Locale.getDefault(), "$%.2f", event.getPrice()));
        }

        // Backwards compatible call sites (if any)
        public void bind(Event event, boolean isBooked) {
            bind(event, isBooked, true);
        }
    }
}
