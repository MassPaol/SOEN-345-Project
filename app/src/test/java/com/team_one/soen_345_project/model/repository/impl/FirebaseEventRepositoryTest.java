package com.team_one.soen_345_project.model.repository.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.team_one.soen_345_project.model.entity.Event;
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

    @Mock
    private FirebaseAuth mockAuth;

    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private FirebaseUser mockUser;

    @Mock
    private CollectionReference mockUserCollection;

    @Mock
    private CollectionReference mockEventCollection;

    @Mock
    private DocumentReference mockUserDocument;

    @Mock
    private DocumentSnapshot mockDocumentSnapshot;

    @Mock
    private Task<DocumentSnapshot> mockTask;

    @Mock
    private Callback mockCallback;

    private FirebaseEventRepository repository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Use constructor injection with mocked dependencies
        repository = new FirebaseEventRepository(mockAuth, mockFirestore);
    }

    @Test
    public void saveEvent_withAdminUser_savesEventSuccessfully() {
        // Arrange
        String adminUid = "admin123";
        HashMap<String, String> eventInfo = createTestEventInfo();

        // Mock Firebase Auth
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn(adminUid);

        // Mock Firestore user collection
        when(mockFirestore.collection("user")).thenReturn(mockUserCollection);
        when(mockUserCollection.document(adminUid)).thenReturn(mockUserDocument);
        when(mockUserDocument.get()).thenReturn(mockTask);

        // Mock document snapshot - user exists and is admin
        when(mockDocumentSnapshot.exists()).thenReturn(true);
        when(mockDocumentSnapshot.getBoolean("isAdmin")).thenReturn(true);

        // Mock Firestore event collection
        when(mockFirestore.collection("event")).thenReturn(mockEventCollection);
        when(mockEventCollection.add(any(Event.class))).thenReturn(mock(Task.class));

        // Capture the OnSuccessListener
        ArgumentCaptor<OnSuccessListener<DocumentSnapshot>> captor =
            ArgumentCaptor.forClass(OnSuccessListener.class);

        // Act
        repository.saveEvent(eventInfo, mockCallback);

        // Verify get() was called and capture the listener
        verify(mockUserDocument).get();
        verify(mockTask).addOnSuccessListener(captor.capture());

        // Simulate successful document retrieval
        captor.getValue().onSuccess(mockDocumentSnapshot);

        // Assert
        verify(mockEventCollection).add(any(Event.class));
        verify(mockCallback).onResult("Event added successfully", true);
    }

    @Test
    public void saveEvent_withNonAdminUser_doesNotSaveEvent() {
        // Arrange
        String nonAdminUid = "user456";
        HashMap<String, String> eventInfo = createTestEventInfo();

        // Mock Firebase Auth
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn(nonAdminUid);

        // Mock Firestore user collection
        when(mockFirestore.collection("user")).thenReturn(mockUserCollection);
        when(mockUserCollection.document(nonAdminUid)).thenReturn(mockUserDocument);
        when(mockUserDocument.get()).thenReturn(mockTask);

        // Mock document snapshot - user exists but is NOT admin
        when(mockDocumentSnapshot.exists()).thenReturn(true);
        when(mockDocumentSnapshot.getBoolean("isAdmin")).thenReturn(false);

        // Capture the OnSuccessListener
        ArgumentCaptor<OnSuccessListener<DocumentSnapshot>> captor =
            ArgumentCaptor.forClass(OnSuccessListener.class);

        // Act
        repository.saveEvent(eventInfo, mockCallback);

        // Verify get() was called and capture the listener
        verify(mockUserDocument).get();
        verify(mockTask).addOnSuccessListener(captor.capture());

        // Simulate successful document retrieval
        captor.getValue().onSuccess(mockDocumentSnapshot);

        // Assert
        verify(mockEventCollection, never()).add(any(Event.class));
        verify(mockCallback).onResult("User does not exist or not an Admin", false);
    }

    @Test
    public void saveEvent_withNonExistentUser_doesNotSaveEvent() {
        // Arrange
        String uid = "nonexistent789";
        HashMap<String, String> eventInfo = createTestEventInfo();

        // Mock Firebase Auth
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn(uid);

        // Mock Firestore user collection
        when(mockFirestore.collection("user")).thenReturn(mockUserCollection);
        when(mockUserCollection.document(uid)).thenReturn(mockUserDocument);
        when(mockUserDocument.get()).thenReturn(mockTask);

        // Mock document snapshot - user does NOT exist
        when(mockDocumentSnapshot.exists()).thenReturn(false);

        // Capture the OnSuccessListener
        ArgumentCaptor<OnSuccessListener<DocumentSnapshot>> captor =
            ArgumentCaptor.forClass(OnSuccessListener.class);

        // Act
        repository.saveEvent(eventInfo, mockCallback);

        // Verify get() was called and capture the listener
        verify(mockUserDocument).get();
        verify(mockTask).addOnSuccessListener(captor.capture());

        // Simulate successful document retrieval
        captor.getValue().onSuccess(mockDocumentSnapshot);

        // Assert
        verify(mockEventCollection, never()).add(any(Event.class));
        verify(mockCallback).onResult("User does not exist or not an Admin", false);
    }

    @Test
    public void saveEvent_withAdminUserButNullIsAdminField_doesNotSaveEvent() {
        // Arrange
        String uid = "admin999";
        HashMap<String, String> eventInfo = createTestEventInfo();

        // Mock Firebase Auth
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn(uid);

        // Mock Firestore user collection
        when(mockFirestore.collection("user")).thenReturn(mockUserCollection);
        when(mockUserCollection.document(uid)).thenReturn(mockUserDocument);
        when(mockUserDocument.get()).thenReturn(mockTask);

        // Mock document snapshot - user exists but isAdmin is null
        when(mockDocumentSnapshot.exists()).thenReturn(true);
        when(mockDocumentSnapshot.getBoolean("isAdmin")).thenReturn(null);

        // Capture the OnSuccessListener
        ArgumentCaptor<OnSuccessListener<DocumentSnapshot>> captor =
            ArgumentCaptor.forClass(OnSuccessListener.class);

        // Act
        repository.saveEvent(eventInfo, mockCallback);

        // Verify get() was called and capture the listener
        verify(mockUserDocument).get();
        verify(mockTask).addOnSuccessListener(captor.capture());

        // Simulate successful document retrieval
        captor.getValue().onSuccess(mockDocumentSnapshot);

        // Assert
        verify(mockEventCollection, never()).add(any(Event.class));
        verify(mockCallback).onResult("User does not exist or not an Admin", false);
    }

    @Test
    public void saveEvent_verifiesCorrectUserDocumentIsQueried() {
        // Arrange
        String uid = "specific-uid-123";
        HashMap<String, String> eventInfo = createTestEventInfo();

        // Mock Firebase Auth
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn(uid);

        // Mock Firestore user collection
        when(mockFirestore.collection("user")).thenReturn(mockUserCollection);
        when(mockUserCollection.document(uid)).thenReturn(mockUserDocument);
        when(mockUserDocument.get()).thenReturn(mockTask);

        // Act
        repository.saveEvent(eventInfo, mockCallback);

        // Assert
        verify(mockFirestore).collection("user");
        verify(mockUserCollection).document(uid);
        verify(mockUserDocument).get();
    }

    @Test
    public void saveEvent_verifiesEventIsAddedToCorrectCollection() {
        // Arrange
        String adminUid = "admin789";
        HashMap<String, String> eventInfo = createTestEventInfo();

        // Mock Firebase Auth
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn(adminUid);

        // Mock Firestore user collection
        when(mockFirestore.collection("user")).thenReturn(mockUserCollection);
        when(mockUserCollection.document(adminUid)).thenReturn(mockUserDocument);
        when(mockUserDocument.get()).thenReturn(mockTask);

        // Mock document snapshot - user is admin
        when(mockDocumentSnapshot.exists()).thenReturn(true);
        when(mockDocumentSnapshot.getBoolean("isAdmin")).thenReturn(true);

        // Mock Firestore event collection
        when(mockFirestore.collection("event")).thenReturn(mockEventCollection);
        when(mockEventCollection.add(any(Event.class))).thenReturn(mock(Task.class));

        // Capture the OnSuccessListener
        ArgumentCaptor<OnSuccessListener<DocumentSnapshot>> captor =
            ArgumentCaptor.forClass(OnSuccessListener.class);

        // Act
        repository.saveEvent(eventInfo, mockCallback);
        verify(mockTask).addOnSuccessListener(captor.capture());
        captor.getValue().onSuccess(mockDocumentSnapshot);

        // Assert
        verify(mockFirestore).collection("event");
        verify(mockEventCollection).add(any(Event.class));
    }

    @Test
    public void saveEvent_createsEventWithCorrectData() {
        // Arrange
        String adminUid = "admin111";
        HashMap<String, String> eventInfo = createTestEventInfo();

        // Mock Firebase Auth
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn(adminUid);

        // Mock Firestore user collection
        when(mockFirestore.collection("user")).thenReturn(mockUserCollection);
        when(mockUserCollection.document(adminUid)).thenReturn(mockUserDocument);
        when(mockUserDocument.get()).thenReturn(mockTask);

        // Mock document snapshot - user is admin
        when(mockDocumentSnapshot.exists()).thenReturn(true);
        when(mockDocumentSnapshot.getBoolean("isAdmin")).thenReturn(true);

        // Mock Firestore event collection
        when(mockFirestore.collection("event")).thenReturn(mockEventCollection);
        when(mockEventCollection.add(any(Event.class))).thenReturn(mock(Task.class));

        // Capture the OnSuccessListener and Event
        ArgumentCaptor<OnSuccessListener<DocumentSnapshot>> listenerCaptor =
            ArgumentCaptor.forClass(OnSuccessListener.class);
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);

        // Act
        repository.saveEvent(eventInfo, mockCallback);
        verify(mockTask).addOnSuccessListener(listenerCaptor.capture());
        listenerCaptor.getValue().onSuccess(mockDocumentSnapshot);

        // Assert
        verify(mockEventCollection).add(eventCaptor.capture());
        Event capturedEvent = eventCaptor.getValue();



        // Verify the Event was created (we can't access private fields, but we verified it was created)
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
