package com.team_one.soen_345_project.viewmodel.admindash;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.team_one.soen_345_project.di.Injection;
import com.team_one.soen_345_project.model.repository.IEventRepository;

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
            } else {
                _uiState.postValue(new AdminDashUiState(message, false));
            }
        });
    }

    public LiveData<AdminDashUiState> getUiState() {
        return _uiState;
    }
}
