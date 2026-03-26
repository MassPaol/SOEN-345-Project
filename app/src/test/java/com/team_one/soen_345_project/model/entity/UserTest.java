package com.team_one.soen_345_project.model.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UserTest {

    @Test
    public void testUserConstructorAndGetters() {
        User user = new User("uid123", "John", "Doe", "john.doe@example.com", "1234567890", "password123");
        
        assertEquals("uid123", user.getUid());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("1234567890", user.getPhoneNumber());
        assertEquals("password123", user.getPassword());
        assertFalse(user.isAdmin());
    }

    @Test
    public void testUserSetters() {
        User user = new User();
        user.setUid("newUid");
        user.setFirstName("Jane");
        user.setLastName("Smith");
        user.setEmail("jane.smith@example.com");
        user.setPhoneNumber("0987654321");
        user.setPassword("newPass");
        user.setAdmin(true);

        assertEquals("newUid", user.getUid());
        assertEquals("Jane", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals("jane.smith@example.com", user.getEmail());
        assertEquals("0987654321", user.getPhoneNumber());
        assertEquals("newPass", user.getPassword());
        assertTrue(user.isAdmin());
    }
}
