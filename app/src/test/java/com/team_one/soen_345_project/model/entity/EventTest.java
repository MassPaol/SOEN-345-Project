package com.team_one.soen_345_project.model.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.google.firebase.Timestamp;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class EventTest {

    // Test primary constructor with all parameters
    @Test
    public void constructor_withAllParameters_setsFieldsCorrectly() {
        String eventId = "event123";
        String title = "Test Event Title";
        String description = "This is a test event description.";
        Timestamp date = new Timestamp(new Date(1740441600000L)); // Feb 25, 2025
        String location = "Montreal";
        String category = "Test";
        int capacity = 500;
        double price = 99.99;

        Event event = new Event(eventId, title, description, date, location, category, capacity, price, 0, "", "");

        assertEquals(eventId, event.getEventId());
        assertEquals(title, event.getTitle());
        assertEquals(description, event.getDescription());
        assertEquals(date, event.getDate());
        assertEquals(location, event.getLocation());
        assertEquals(category, event.getCategory());
        assertEquals(capacity, event.getCapacity());
        assertEquals(price, event.getPrice(), 0.01);
    }

    // Test constructor with HashMap
    @Test
    public void constructor_withHashMap_parsesFieldsCorrectly() {
        HashMap<String, String> eventInfo = new HashMap<>();
        eventInfo.put("title", "Music Festival");
        eventInfo.put("disc", "Summer music event");
        eventInfo.put("date", "1740441600000"); // Date in milliseconds
        eventInfo.put("time", "14:30"); // Time as HH:mm
        eventInfo.put("location", "Central Park");
        eventInfo.put("category", "Music");
        eventInfo.put("capacity", "1000");
        eventInfo.put("price", "75.50");

        Event event = new Event(eventInfo);

        assertEquals("Music Festival", event.getTitle());
        assertEquals("Summer music event", event.getDescription());
        assertNotNull(event.getDate());
        assertEquals("Central Park", event.getLocation());
        assertEquals("Music", event.getCategory());
        assertEquals(1000, event.getCapacity());
        assertEquals(75.50, event.getPrice(), 0.01);
    }

    // Test constructor with HashMap - verify timestamp conversion
    @Test
    public void constructor_withHashMap_convertsDateAndTimeToTimestamp() {
        HashMap<String, String> eventInfo = new HashMap<>();
        eventInfo.put("title", "Event");
        eventInfo.put("disc", "Description");
        eventInfo.put("date", "1740441600000"); // Base date
        eventInfo.put("time", "10:00"); // 10 hours
        eventInfo.put("location", "Location");
        eventInfo.put("category", "Category");
        eventInfo.put("capacity", "100");
        eventInfo.put("price", "0");

        Event event = new Event(eventInfo);

        // Assert
        // The Event constructor extracts year/month/day in UTC from the date millis,
        // then creates a local-timezone Calendar with those date components + the given time.
        // We must replicate that logic to get the expected value.
        Calendar utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcCal.setTimeInMillis(1740441600000L);
        int year = utcCal.get(Calendar.YEAR);
        int month = utcCal.get(Calendar.MONTH);
        int day = utcCal.get(Calendar.DAY_OF_MONTH);

        Calendar localCal = Calendar.getInstance();
        localCal.set(year, month, day, 10, 0, 0);
        localCal.set(Calendar.MILLISECOND, 0);
        long expectedTimeMs = localCal.getTimeInMillis();
        assertEquals(expectedTimeMs, event.getDate().toDate().getTime());
    }

    @Test
    public void setTitle_updatesTitle() {
        Event event = createDefaultEvent();
        String newTitle = "Updated Conference Title";

        event.setTitle(newTitle);

        assertEquals(newTitle, event.getTitle());
    }

    @Test
    public void setDescription_updatesDescription() {
        Event event = createDefaultEvent();
        String newDescription = "This is an updated description";

        event.setDescription(newDescription);

        assertEquals(newDescription, event.getDescription());
    }

    @Test
    public void setDate_updatesDate() {
        Event event = createDefaultEvent();
        Timestamp newDate = new Timestamp(new Date(1750000000000L));

        event.setDate(newDate);

        assertEquals(newDate, event.getDate());
    }

    @Test
    public void setLocation_updatesLocation() {
        Event event = createDefaultEvent();
        String newLocation = "New York City";

        event.setLocation(newLocation);

        assertEquals(newLocation, event.getLocation());
    }

    @Test
    public void setCategory_updatesCategory() {
        Event event = createDefaultEvent();
        String newCategory = "Sports";

        event.setCategory(newCategory);

        assertEquals(newCategory, event.getCategory());
    }

    @Test
    public void setCapacity_updatesCapacity() {
        Event event = createDefaultEvent();
        int newCapacity = 2000;

        event.setCapacity(newCapacity);

        assertEquals(newCapacity, event.getCapacity());
    }

    @Test
    public void setPrice_updatesPrice() {
        Event event = createDefaultEvent();
        double newPrice = 149.99;

        event.setPrice(newPrice);

        assertEquals(newPrice, event.getPrice(), 0.01);
    }

    @Test
    public void constructor_withZeroCapacity_setsCapacityToZero() {
        Event event = new Event("id", "Title", "Desc", new Timestamp(new Date()), "Loc", "Cat", 0, 50.0, 0, "", "");

        assertEquals(0, event.getCapacity());
    }

    @Test
    public void constructor_withZeroPrice_setsPriceToZero() {
        Event event = new Event("id", "Title", "Desc", new Timestamp(new Date()), "Loc", "Cat", 100, 0.0, 0, "", "");

        assertEquals(0.0, event.getPrice(), 0.01);
    }

    @Test
    public void setCapacity_withNegativeValue_setsNegativeCapacity() {
        Event event = createDefaultEvent();

        event.setCapacity(-10);

        assertEquals(-10, event.getCapacity());
    }

    @Test
    public void setPrice_withNegativeValue_setsNegativePrice() {
        Event event = createDefaultEvent();

        event.setPrice(-50.0);

        assertEquals(-50.0, event.getPrice(), 0.01);
    }

    @Test
    public void constructor_withNullEventId_setsEventIdToNull() {
        Event event = new Event(null, "Title", "Desc", new Timestamp(new Date()), "Loc", "Cat", 100, 50.0, 0, "", "");

        assertNull(event.getEventId());
    }

    @Test
    public void constructor_withEmptyStrings_storesEmptyStrings() {
        Event event = new Event("", "", "", new Timestamp(new Date()), "", "", 0, 0.0, 0, "", "");

        assertEquals("", event.getEventId());
        assertEquals("", event.getTitle());
        assertEquals("", event.getDescription());
        assertEquals("", event.getLocation());
        assertEquals("", event.getCategory());
    }

    @Test
    public void constructor_withAllNullValues_setsFieldsToNull() {
        Event event = new Event(null, null, null, null, null, null, 0, 0.0, 0, null, null);

        assertNull(event.getEventId());
        assertNull(event.getTitle());
        assertNull(event.getDescription());
        assertNull(event.getDate());
        assertNull(event.getLocation());
        assertNull(event.getCategory());
    }

    private Event createDefaultEvent() {
        return new Event(
                "event123",
                "Default Event",
                "Default Description",
                new Timestamp(new Date(1740441600000L)),
                "Default Location",
                "Default Category",
                100,
                50.0,
                0,
                "",
                ""
        );
    }
}