package com.team_one.soen_345_project.viewmodel.register;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.team_one.soen_345_project.model.repository.IAuthRepository;
import com.team_one.soen_345_project.model.util.callback.Callback;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

public class RegisterViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule(); // Fast synchronous LiveData operations

    @Mock
    private IAuthRepository mockRepository;

    private RegisterViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new RegisterViewModel(mockRepository);
    }

    @Test
    public void testRegistrationFailureDueToValidation() {
        // Missing lastName and password mismatch
        String[] invalidFields = {"John", "", "john@example.com", "1234567890", "pass123", "pass456"};
        
        viewModel.onRegisterClicked(invalidFields);
        
        RegisterUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        
        Map<String, String> errors = state.getValidationErrors();
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
        // Verify mock wasn't called because validation blocked it
        verify(mockRepository, never()).createUser(any(String[].class), any(Callback.class));
    }

    @Test
    public void testRegistrationSuccess() {
        String[] validFields = {"John", "Doe", "john@example.com", "1234567890", "Pass123!", "Pass123!"};
        
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            // Simulate success
            callback.onResult("User Created", true, false);
            return null;
        }).when(mockRepository).createUser(any(String[].class), any(Callback.class));
        
        viewModel.onRegisterClicked(validFields);
        
        // Output expects navigateToMain == true
        Boolean navigate = viewModel.getNavigateToMain().getValue();
        assertNotNull(navigate);
        assertTrue(navigate);
        
        // Verify repository captured 5 elements (no confirmPassword)
        verify(mockRepository, times(1)).createUser(any(String[].class), any(Callback.class));
    }

    @Test
    public void testRegistrationFailureFromRepository() {
        String[] validFields = {"John", "Doe", "john@example.com", "1234567890", "Pass123!", "Pass123!"};
        
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            // Simulate Firebase error
            callback.onResult("Email already exists", false, false);
            return null;
        }).when(mockRepository).createUser(any(String[].class), any(Callback.class));
        
        viewModel.onRegisterClicked(validFields);
        
        // Expected navigate is false
        Boolean navigate = viewModel.getNavigateToMain().getValue();
        assertNotNull(navigate);
        assertFalse(navigate);
        
        // Expected UI state to have generalError
        RegisterUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertEquals("Email already exists", state.getGeneralError());
    }
}
