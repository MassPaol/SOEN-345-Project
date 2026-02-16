package com.team_one.soen_345_project.viewmodel.register;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.team_one.soen_345_project.di.Injection;
import com.team_one.soen_345_project.model.repository.IAuthRepository;

import java.util.Map;

public class RegisterViewModel extends ViewModel {
    private static final String TAG = "RegisterViewModel";
    // Live data keeping track of state changes
    private final MutableLiveData<Boolean> navigateToMain = new MutableLiveData<>(false);
    private final MutableLiveData<RegisterUiState> _uiState =
            new MutableLiveData<>(new RegisterUiState(null,null));

    // Repository interface instantiation
    private final IAuthRepository iAuthRepository = Injection.provideAuthRepository();

    public void onRegisterClicked(String[] registrationFields) {

        Map<String, String> errors = RegisterInputValidator.validateRegistrationFields(registrationFields);

        if (!errors.isEmpty()) {
            _uiState.setValue(new RegisterUiState(errors,null));
            return;
        }

        Log.i(TAG, "Input validation passed: " + registrationFields[2]);

        registerUser(registrationFields);
    }

    // Send user registration info to Model for registering with firebase
    private void registerUser(String[] registrationFields) {

        Log.d(TAG, "Attempting to create new user in firebasestore: " + registrationFields[2]);
        // Strip out confirmPassword (index 5) before sending to repository
        // Repository expects: [firstName, lastName, email, phone, password]
        String[] fieldsForRepository = new String[5];
        System.arraycopy(registrationFields, 0, fieldsForRepository, 0, 5);

        iAuthRepository.createUser(fieldsForRepository, ((message, isSuccess) -> {
            if(isSuccess) {
                // Change screen on success
                Log.d(TAG, "Creation successful for user: " + registrationFields[2]);
                navigateToMain.postValue(true);
            } else {
                Log.e(TAG, "Creation failed for user: " + registrationFields[2] + " | Reason: " + message);
                _uiState.postValue(new RegisterUiState(null, message != null ? message : "Registration failed"));
            }
        }));
    }


    // GETTERS AND SETTERS

    public LiveData<Boolean> getNavigateToMain() {
        return navigateToMain;
    }

    public LiveData<RegisterUiState> getUiState() {
        return _uiState;
    }
}
