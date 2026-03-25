package com.team_one.soen_345_project.model.repository.impl;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.team_one.soen_345_project.model.repository.IAuthRepository;
import com.team_one.soen_345_project.model.repository.IEventRepository;
import com.team_one.soen_345_project.model.repository.IFirestoreSource;
import com.team_one.soen_345_project.model.util.callback.Callback;
import com.team_one.soen_345_project.model.util.callback.DeleteEventCallback;
import com.team_one.soen_345_project.model.util.callback.EventListCallback;
import com.team_one.soen_345_project.model.util.callback.UpdateEventCallback;

import java.util.Calendar;
import java.util.HashMap;

public class FirebaseEventRepository implements IEventRepository {
    IAuthRepository authRepository;
    IFirestoreSource firestoreSource;

    // Default constructor for production use
    public FirebaseEventRepository() {
        this.authRepository = new FirebaseAuthRepository();
        this.firestoreSource = new FirestoreSource(FirebaseFirestore.getInstance());
    }

    // Constructor for testing with dependency injection
    public FirebaseEventRepository(IAuthRepository authRepository, IFirestoreSource firestoreSource) {
        this.authRepository = authRepository;
        this.firestoreSource = firestoreSource;
    }

    // Save event to firestore
    public void saveEvent(HashMap<String, String> eventInfo, Callback callback) {
        String uid = authRepository.getCurrentUserUid();
        if (uid == null) {
            callback.onResult("No user logged in", false);
            return;
        }

        firestoreSource.isUserAdmin(uid,
                isAdmin -> {
                    if (!isAdmin) {
                        callback.onResult("User does not exist or not an Admin", false);
                        return;
                    }

                    try {
                        HashMap<String, Object> data = normalizeEventForFirestore(eventInfo);
                        firestoreSource.addEvent(data,
                                unused -> callback.onResult("Event added successfully", true),
                                e -> callback.onResult("Failed to add event: " + e.getMessage(), false)
                        );
                    } catch (Exception ex) {
                        callback.onResult("Failed to add event: " + ex.getMessage(), false);
                    }
                },
                e -> callback.onResult("Failed to verify user: " + e.getMessage(), false)
        );
    }

    /**
     * Converts CreateEventSheet's string inputs into the canonical Firestore schema.
     *
     * Expected Firestore fields/types:
     * - title (string)
     * - description (string)
     * - location (string)
     * - location_id (string)
     * - category (string)
     * - category_id (string)
     * - capacity (int64)
     * - price (double)
     * - date (timestamp)
     * - reservations (int64)
     * - full (boolean)
     */
    private HashMap<String, Object> normalizeEventForFirestore(HashMap<String, String> eventInfo) {
        HashMap<String, Object> data = new HashMap<>();

        String title = safe(eventInfo.get("title"));
        String description = safe(eventInfo.get("description"));
        if (description.isEmpty()) {
            // Backwards compatibility with old key
            description = safe(eventInfo.get("disc"));
        }

        String location = safe(eventInfo.get("location"));
        String locationId = safe(eventInfo.get("location_id"));
        String category = safe(eventInfo.get("category"));
        String categoryId = safe(eventInfo.get("category_id"));

        long capacity = parseLong(eventInfo.get("capacity"), 0L);
        double price = parseDouble(eventInfo.get("price"), 0.0);

        // Convert date/time inputs to Firestore Timestamp
        String dateMillisStr = eventInfo.get("date");
        String timeStr = eventInfo.get("time");
        Timestamp timestamp = convertToTimestamp(dateMillisStr, timeStr);

        data.put("title", title);
        data.put("description", description);
        data.put("location", location);
        data.put("location_id", locationId);
        data.put("category", category);
        data.put("category_id", categoryId);
        data.put("capacity", capacity);
        data.put("price", price);
        data.put("date", timestamp);

        // Defaults
        data.put("reservations", 0L);
        data.put("full", false);

        return data;
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static long parseLong(String s, long fallback) {
        try {
            if (s == null) return fallback;
            return Long.parseLong(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static double parseDouble(String s, double fallback) {
        try {
            if (s == null) return fallback;
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    // Date parser to turn two Strings i.e. "1771977600000" (Long date in ms) and "14:30" into a timestamp object
    private static Timestamp convertToTimestamp(String date, String time) {
        if (date == null || time == null) {
            throw new IllegalArgumentException("Date/time not set");
        }

        int hour = Integer.parseInt(time.substring(0, 2));
        int minute = Integer.parseInt(time.substring(3));
        long dateLong = Long.parseLong(date);

        // MaterialDatePicker returns UTC midnight for selected date
        Calendar utcCalendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
        utcCalendar.setTimeInMillis(dateLong);

        int year = utcCalendar.get(Calendar.YEAR);
        int month = utcCalendar.get(Calendar.MONTH);
        int day = utcCalendar.get(Calendar.DAY_OF_MONTH);

        Calendar localCalendar = Calendar.getInstance();
        localCalendar.set(year, month, day, hour, minute, 0);
        localCalendar.set(Calendar.MILLISECOND, 0);

        return new Timestamp(localCalendar.getTime());
    }

    // Get all events from Firestore sorted by date (chronological order)
    @Override
    public void getAllEvents(EventListCallback callback) {
        android.util.Log.d("FirebaseEventRepo", "getAllEvents() called");
        firestoreSource.getAllEvents(
                events -> {
                    // Sort events by date (chronological order - earliest first)
                    events.sort((e1, e2) -> {
                        if (e1.getDate() == null || e2.getDate() == null) {
                            return 0;
                        }
                        return e1.getDate().compareTo(e2.getDate());
                    });
                    android.util.Log.d("FirebaseEventRepo", "Returning " + events.size() + " valid events");
                    callback.onEventsReceived(events);
                },
                e -> {
                    android.util.Log.e("FirebaseEventRepo", "Failed to get all events", e);
                    callback.onError("Failed to fetch events: " + e.getMessage());
                }
        );
    }

    @Override
    public void deleteEvent(String eventId, DeleteEventCallback callback) {
        String uid = authRepository.getCurrentUserUid();
        if (uid == null) {
            callback.onResult("No user logged in", false);
            return;
        }

        // Backup check ensuring that user is an admin
        firestoreSource.isUserAdmin(uid,
                isAdmin -> {
                    if (isAdmin) {
                        firestoreSource.deleteEvent(eventId,
                                aVoid -> callback.onResult("Event deleted successfully", true),
                                e     -> callback.onResult("Failed to delete event: " + e.getMessage(), false)
                        );
                    } else {
                        callback.onResult("User does not exist or not an Admin", false);
                    }
                },
                e -> callback.onResult("Failed to verify user: " + e.getMessage(), false)
        );
    }

    @Override
    public void updateEvent(String eventId, HashMap<String, Object> updatedFields, UpdateEventCallback callback) {
        String uid = authRepository.getCurrentUserUid();
        if (uid == null) {
            callback.onResult("No user logged in", false);
            return;
        }

        // Backup check ensuring that user is an admin
        firestoreSource.isUserAdmin(uid,
                isAdmin -> {
                    if (isAdmin) {
                        // Update the event document with the new fields
                        firestoreSource.updateEvent(eventId, updatedFields,
                                aVoid -> callback.onResult("Event updated successfully", true),
                                e     -> callback.onResult("Failed to update event: " + e.getMessage(), false)
                        );
                    } else {
                        callback.onResult("User does not exist or not an Admin", false);
                    }
                },
                e -> callback.onResult("Failed to verify admin status: " + e.getMessage(), false)
        );
    }
}