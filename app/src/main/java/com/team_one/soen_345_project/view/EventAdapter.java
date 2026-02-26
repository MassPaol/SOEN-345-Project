package com.team_one.soen_345_project.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.team_one.soen_345_project.R;
import com.team_one.soen_345_project.model.entity.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<Event> events = new ArrayList<>();

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

    static class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEventTitle;
        private final TextView tvEventDate;
        private final TextView tvEventLocation;
        private final TextView tvEventDescription;
        private final TextView tvEventCategory;
        private final TextView tvEventCapacity;
        private final TextView tvEventPrice;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
            tvEventDescription = itemView.findViewById(R.id.tvEventDescription);
            tvEventCategory = itemView.findViewById(R.id.tvEventCategory);
            tvEventCapacity = itemView.findViewById(R.id.tvEventCapacity);
            tvEventPrice = itemView.findViewById(R.id.tvEventPrice);
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
        }
    }
}

