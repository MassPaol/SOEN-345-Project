package com.team_one.soen_345_project.view;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.team_one.soen_345_project.R;
import com.team_one.soen_345_project.model.entity.Event;
import com.team_one.soen_345_project.viewmodel.admindash.AdminDashViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<Event> events = new ArrayList<>();
    private AdminDashViewModel viewModel;

    // Interface for handling edit event clicks
    public interface OnEditEventListener {
        void onEditEvent(Event event);
    }

    private OnEditEventListener editEventListener;

    public void setViewModel(AdminDashViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void setOnEditEventListener(OnEditEventListener listener) {
        this.editEventListener = listener;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEventTitle;
        private final TextView tvEventDate;
        private final TextView tvEventLocation;
        private final TextView tvEventDescription;
        private final TextView tvEventCategory;
        private final TextView tvEventCapacity;
        private final TextView tvEventPrice;
        private final TextView tvEventStatusPill;
        private final ImageButton btnEventMenu;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
            tvEventDescription = itemView.findViewById(R.id.tvEventDescription);
            tvEventCategory = itemView.findViewById(R.id.tvEventCategory);
            tvEventCapacity = itemView.findViewById(R.id.tvEventCapacity);
            tvEventPrice = itemView.findViewById(R.id.tvEventPrice);
            tvEventStatusPill = itemView.findViewById(R.id.tvEventStatusPill);
            btnEventMenu = itemView.findViewById(R.id.btnEventMenu);
            
            // Admins don't need to see the status pill in the admin dashboard
            if (tvEventStatusPill != null) {
                tvEventStatusPill.setVisibility(View.GONE);
            }
        }

        public void bind(Event event) {
            tvEventTitle.setText(event.getTitle());

            // Format date
            if (event.getDate() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
                Date date = event.getDate().toDate();
                tvEventDate.setText(dateFormat.format(date));
            } else {
                tvEventDate.setText("Date not set");
            }

            tvEventLocation.setText(event.getLocation());
            tvEventDescription.setText(event.getDescription());
            tvEventCategory.setText(event.getCategory());
            tvEventCapacity.setText(String.format(Locale.getDefault(), "Capacity: %d", event.getCapacity()));
            tvEventPrice.setText(String.format(Locale.getDefault(), "$%.2f", event.getPrice()));

            // Set up three-dot menu button
            btnEventMenu.setOnClickListener(v -> showPopupMenu(v, event));
        }

        private void showPopupMenu(View view, Event event) {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.getMenuInflater().inflate(R.menu.event_item_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.action_edit_event) {
                    // Call the edit listener if set
                    if (editEventListener != null) {
                        editEventListener.onEditEvent(event);
                    }
                    return true;
                } else if (itemId == R.id.action_delete_event) {
                    // Show confirmation dialog before deleting
                    showDeleteConfirmationDialog(view, event);
                    return true;
                }

                return false;
            });

            popupMenu.show();
        }

        private void showDeleteConfirmationDialog(View view, Event event) {
            new AlertDialog.Builder(view.getContext())
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete \"" + event.getTitle() + "\"?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // Call ViewModel to delete the event
                        if (viewModel != null && event.getEventId() != null) {
                            viewModel.deleteEvent(event.getEventId());
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }
}

