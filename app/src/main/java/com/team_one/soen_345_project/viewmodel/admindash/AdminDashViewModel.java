package com.team_one.soen_345_project.viewmodel.admindash;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.team_one.soen_345_project.di.Injection;
import com.team_one.soen_345_project.model.repository.IEventRepository;
import com.team_one.soen_345_project.model.util.callback.EventCountCallback;

import java.util.HashMap;

public class AdminDashViewModel {
    private final MutableLiveData<AdminDashUiState> _uiState =
            new MutableLiveData<>(new AdminDashUiState(null,false));
    IEventRepository iEventRepository = Injection.provideEventRepository();

    // Method for communicating between the view and the model for saving an event
    public void saveEvent(HashMap<String, String> eventInfo) {
        iEventRepository.saveEvent(eventInfo, (message, isSuccess, isAdmin) -> {
            if (isSuccess) {
                _uiState.postValue(new AdminDashUiState(message, true));
                // Reload event count after successfully adding an event
                loadEventCount();
            } else {
                _uiState.postValue(new AdminDashUiState(message, false));
            }
        });
    }

    // Method to load the total number of events from Firebase
    public void loadEventCount() {
        iEventRepository.getEventCount(new EventCountCallback() {
            @Override
            public void onCountReceived(int count) {
                // Preserve existing state values
                AdminDashUiState currentState = _uiState.getValue();
                String message = currentState != null ? currentState.getMessage() : null;
                boolean isActionComplete = currentState != null && currentState.isActionComplete();

                // Update with new count
                _uiState.postValue(new AdminDashUiState(message, isActionComplete, count));
            }

            @Override
            public void onError(String errorMessage) {
                // Update UI state with error message
                _uiState.postValue(new AdminDashUiState(errorMessage, false, 0));
            }
        });
    }

    public LiveData<AdminDashUiState> getUiState() {
        return _uiState;
    }
}
