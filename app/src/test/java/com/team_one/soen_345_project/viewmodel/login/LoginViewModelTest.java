package com.team_one.soen_345_project.viewmodel.login;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

public class LoginViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule(); // Forces LiveData to execute synchronously on the test thread

    @Mock
    private IAuthRepository mockRepository;

    private LoginViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new LoginViewModel(mockRepository);
    }

    @Test
    public void testEmptyEmailOrPassword() {
        viewModel.onLoginClicked("", "pass");
        LoginUiState state = viewModel.getUiState().getValue();
        
        assertNotNull(state);
        assertEquals("Email and password cannot be empty", state.getErrorMessage());
        assertFalse(state.isSuccess());
        assertFalse(state.isLoading());

        viewModel.onLoginClicked("email@test.com", null);
        state = viewModel.getUiState().getValue();
        assertEquals("Email and password cannot be empty", state.getErrorMessage());
    }

    @Test
    public void testLoginSuccessUser() {
        // Arrange
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(2);
            // Simulate success login as basic user (isAdmin = false)
            callback.onResult("Success", true, false);
            return null;
        }).when(mockRepository).loginUser(anyString(), anyString(), any(Callback.class));

        // Act
        viewModel.onLoginClicked("user@test.com", "password123");

        // Assert
        LoginUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertTrue(state.isSuccess());
        assertFalse(state.isAdmin());
        assertNull(state.getErrorMessage());
    }

    @Test
    public void testLoginSuccessAdmin() {
        // Arrange
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(2);
            // Simulate success login as admin (isAdmin = true)
            callback.onResult("Success", true, true);
            return null;
        }).when(mockRepository).loginUser(anyString(), anyString(), any(Callback.class));

        // Act
        viewModel.onLoginClicked("admin@test.com", "adminpass");

        // Assert
        LoginUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertTrue(state.isSuccess());
        assertTrue(state.isAdmin());
        assertNull(state.getErrorMessage());
    }

    @Test
    public void testLoginFailure() {
        // Arrange
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(2);
            // Simulate failed login
            callback.onResult("Invalid Credentials", false, false);
            return null;
        }).when(mockRepository).loginUser(anyString(), anyString(), any(Callback.class));

        // Act
        viewModel.onLoginClicked("wrong@test.com", "wrongpass");

        // Assert
        LoginUiState state = viewModel.getUiState().getValue();
        assertNotNull(state);
        assertFalse(state.isSuccess());
        assertEquals("Invalid Credentials", state.getErrorMessage());
        assertFalse(state.isLoading());
    }
}
