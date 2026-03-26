package com.team_one.soen_345_project.viewmodel.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LoginUiStateTest {

    @Test
    public void testLoginUiStateValues() {
        LoginUiState state = new LoginUiState("Error", true, false, true);
        
        assertEquals("Error", state.getErrorMessage());
        assertTrue(state.isSuccess());
        assertFalse(state.isLoading());
        assertTrue(state.isAdmin());
    }

    @Test
    public void testLoginUiStateDefault() {
        LoginUiState state = new LoginUiState(null, false, true, false);
        
        assertEquals(null, state.getErrorMessage());
        assertFalse(state.isSuccess());
        assertTrue(state.isLoading());
        assertFalse(state.isAdmin());
    }
}
