package com.team_one.soen_345_project.viewmodel.login;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.team_one.soen_345_project.di.Injection;
import com.team_one.soen_345_project.model.repository.IAuthRepository;

public class LoginViewModel extends ViewModel {
    private static final String TAG = "LoginViewModel";
    private final MutableLiveData<LoginUiState> _uiState = new MutableLiveData<>(new LoginUiState(null, false, false, false));
    private final IAuthRepository iAuthRepository;

    public LoginViewModel() {
        this.iAuthRepository = Injection.provideAuthRepository();
    }

    public LoginViewModel(IAuthRepository authRepository) {
        this.iAuthRepository = authRepository;
    }

    public LiveData<LoginUiState> getUiState() {
        return _uiState;
    }

    public void onLoginClicked(String email, String password) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            _uiState.setValue(new LoginUiState("Email and password cannot be empty", false, false, false));
            return;
        }

        _uiState.setValue(new LoginUiState(null, false, true, false));

        iAuthRepository.loginUser(email, password, (message, isSuccess, isAdmin) -> {
            if (isSuccess) {
                if(isAdmin){
                    _uiState.postValue(new LoginUiState(null, true, false, true));
                } else {
                    _uiState.postValue(new LoginUiState(null, true, false, false));
                }
            } else {
                _uiState.postValue(new LoginUiState(message, false, false, false));
            }
        });
    }
}
