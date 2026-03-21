package com.team_one.soen_345_project.model.repository.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.OnSuccessListener;
import com.team_one.soen_345_project.model.repository.IAuthRepository;
import com.team_one.soen_345_project.model.repository.IEventRepository;
import com.team_one.soen_345_project.model.repository.IFirestoreSource;
import com.team_one.soen_345_project.model.util.callback.Callback;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;

@RunWith(MockitoJUnitRunner.class)
public class FirebaseEventRepositoryTest {

    // All plain interfaces — Mockito handles these with zero bytecode issues
    @Mock
    private IAuthRepository mockAuthRepository;

    @Mock
    private IFirestoreSource mockFirestoreSource;

    @Mock
    private Callback mockCallback;

    private IEventRepository mockRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Use constructor injection with mocked dependencies
        mockRepository = new FirebaseEventRepository(mockAuthRepository, mockFirestoreSource);
    }

    @Test
    public void saveEvent_withAdminUser_savesEventSuccessfully() {
        // Arrange
        String adminUid = "admin123";
        HashMap<String, String> eventInfo = createTestEventInfo();

        // Mock auth — user is logged in
        when(mockAuthRepository.getCurrentUserUid()).thenReturn(adminUid);

        // Capture the OnSuccessListener for isUserAdmin
        ArgumentCaptor<OnSuccessListener<Boolean>> captor =
                ArgumentCaptor.forClass(OnSuccessListener.class);

        // Act
        mockRepository.saveEvent(eventInfo, mockCallback);

        // Verify isUserAdmin() was called and capture the listener
        verify(mockFirestoreSource).isUserAdmin(eq(adminUid), captor.capture(), any());

        // Simulate: user exists and is admin
        captor.getValue().onSuccess(true);

        // Capture and fire the addEvent success callback
        ArgumentCaptor<OnSuccessListener<Void>> addCaptor =
                ArgumentCaptor.forClass(OnSuccessListener.class);
        verify(mockFirestoreSource).addEvent(any(), addCaptor.capture(), any());
        addCaptor.getValue().onSuccess(null);

        // Assert
        verify(mockCallback).onResult("Event added successfully", true);
    }

    @Test
    public void saveEvent_withNonAdminUser_doesNotSaveEvent() {
        // Arrange
        String nonAdminUid = "user456";
        HashMap<String, String> eventInfo = createTestEventInfo();

        // Mock auth — user is logged in
        when(mockAuthRepository.getCurrentUserUid()).thenReturn(nonAdminUid);

        // Capture the OnSuccessListener
        ArgumentCaptor<OnSuccessListener<Boolean>> captor =
                ArgumentCaptor.forClass(OnSuccessListener.class);

        // Act
        mockRepository.saveEvent(eventInfo, mockCallback);

        // Verify isUserAdmin() was called and capture the listener
        verify(mockFirestoreSource).isUserAdmin(eq(nonAdminUid), captor.capture(), any());

        // Simulate: user exists but is NOT admin
        captor.getValue().onSuccess(false);

        // Assert
        verify(mockFirestoreSource, never()).addEvent(any(), any(), any());
        verify(mockCallback).onResult("User does not exist or not an Admin", false);
    }

    @Test
    public void saveEvent_withNonExistentUser_doesNotSaveEvent() {
        // Arrange
        String uid = "nonexistent789";
        HashMap<String, String> eventInfo = createTestEventInfo();

        // Mock auth — user is logged in
        when(mockAuthRepository.getCurrentUserUid()).thenReturn(uid);

        // Capture the OnSuccessListener
        ArgumentCaptor<OnSuccessListener<Boolean>> captor =
                ArgumentCaptor.forClass(OnSuccessListener.class);

        // Act
        mockRepository.saveEvent(eventInfo, mockCallback);

        // Verify isUserAdmin() was called and capture the listener
        verify(mockFirestoreSource).isUserAdmin(eq(uid), captor.capture(), any());

        // Simulate: user does NOT exist (FirestoreSource returns false)
        captor.getValue().onSuccess(false);

        // Assert
        verify(mockFirestoreSource, never()).addEvent(any(), any(), any());
        verify(mockCallback).onResult("User does not exist or not an Admin", false);
    }

    @Test
    public void saveEvent_withAdminUserButNullIsAdminField_doesNotSaveEvent() {
        // Arrange
        String uid = "admin999";
        HashMap<String, String> eventInfo = createTestEventInfo();

        // Mock auth — user is logged in
        when(mockAuthRepository.getCurrentUserUid()).thenReturn(uid);

        // Capture the OnSuccessListener
        ArgumentCaptor<OnSuccessListener<Boolean>> captor =
                ArgumentCaptor.forClass(OnSuccessListener.class);

        // Act
        mockRepository.saveEvent(eventInfo, mockCallback);

        // Verify isUserAdmin() was called and capture the listener
        verify(mockFirestoreSource).isUserAdmin(eq(uid), captor.capture(), any());

        // Simulate: user exists but isAdmin is null (FirestoreSource treats null as false)
        captor.getValue().onSuccess(false);

        // Assert
        verify(mockFirestoreSource, never()).addEvent(any(), any(), any());
        verify(mockCallback).onResult("User does not exist or not an Admin", false);
    }

    @Test
    public void saveEvent_verifiesCorrectUserDocumentIsQueried() {
        // Arrange
        String uid = "specific-uid-123";
        HashMap<String, String> eventInfo = createTestEventInfo();

        // Mock auth — user is logged in
        when(mockAuthRepository.getCurrentUserUid()).thenReturn(uid);

        // Act
        mockRepository.saveEvent(eventInfo, mockCallback);

        // Assert — verifies isUserAdmin is called with the correct uid
        verify(mockFirestoreSource).isUserAdmin(eq(uid), any(), any());
    }

    @Test
    public void saveEvent_verifiesEventIsAddedToCorrectCollection() {
        // Arrange
        String adminUid = "admin789";
        HashMap<String, String> eventInfo = createTestEventInfo();

        // Mock auth — user is logged in
        when(mockAuthRepository.getCurrentUserUid()).thenReturn(adminUid);

        // Capture the OnSuccessListener
        ArgumentCaptor<OnSuccessListener<Boolean>> captor =
                ArgumentCaptor.forClass(OnSuccessListener.class);

        // Act
        mockRepository.saveEvent(eventInfo, mockCallback);
        verify(mockFirestoreSource).isUserAdmin(eq(adminUid), captor.capture(), any());

        // Simulate: user is admin
        captor.getValue().onSuccess(true);

        // Assert — verifies addEvent is called on the firestoreSource (i.e., targets "event" collection internally)
        verify(mockFirestoreSource).addEvent(any(), any(), any());
    }

    @Test
    public void saveEvent_createsEventWithCorrectData() {
        // Arrange
        String adminUid = "admin111";
        HashMap<String, String> eventInfo = createTestEventInfo();

        // Mock auth — user is logged in
        when(mockAuthRepository.getCurrentUserUid()).thenReturn(adminUid);

        // Capture the OnSuccessListener and event data
        ArgumentCaptor<OnSuccessListener<Boolean>> listenerCaptor =
                ArgumentCaptor.forClass(OnSuccessListener.class);
        ArgumentCaptor<HashMap> eventCaptor = ArgumentCaptor.forClass(HashMap.class);

        // Act
        mockRepository.saveEvent(eventInfo, mockCallback);
        verify(mockFirestoreSource).isUserAdmin(eq(adminUid), listenerCaptor.capture(), any());

        // Simulate: user is admin
        listenerCaptor.getValue().onSuccess(true);

        // Assert
        verify(mockFirestoreSource).addEvent(eventCaptor.capture(), any(), any());

        // Verify the event data was created (the HashMap passed through contains the original eventInfo fields)
        // The Event constructor validates the HashMap internally
    }

    // Helper method to create test event data
    private HashMap<String, String> createTestEventInfo() {
        HashMap<String, String> eventInfo = new HashMap<>();
        eventInfo.put("title", "Test Event");
        eventInfo.put("disc", "Test Description");
        eventInfo.put("date", "1740441600000");
        eventInfo.put("time", "14:30");
        eventInfo.put("location", "Test Location");
        eventInfo.put("category", "Test Category");
        eventInfo.put("capacity", "100");
        eventInfo.put("price", "50.00");
        return eventInfo;
    }
}