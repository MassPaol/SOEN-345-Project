package com.team_one.soen_345_project.model.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ReservationTest {

    @Test
    public void testReservationConstructorAndGetters() {
        Reservation res = new Reservation("res1", "user1", "event1", null, "BOOKED");
        
        assertEquals("res1", res.getReservationId());
        assertEquals("user1", res.getUserId());
        assertEquals("event1", res.getEventId());
        assertNull(res.getTimestamp());
        assertEquals("BOOKED", res.getStatus());
    }

    @Test
    public void testReservationSetters() {
        Reservation res = new Reservation();
        res.setReservationId("res2");
        res.setUserId("user2");
        res.setEventId("event2");
        res.setStatus("CANCELLED");

        assertEquals("res2", res.getReservationId());
        assertEquals("user2", res.getUserId());
        assertEquals("event2", res.getEventId());
        assertEquals("CANCELLED", res.getStatus());
    }
}
