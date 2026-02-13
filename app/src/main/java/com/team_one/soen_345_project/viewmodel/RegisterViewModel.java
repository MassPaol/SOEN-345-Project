package com.team_one.soen_345_project.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.team_one.soen_345_project.di.Injection;
import com.team_one.soen_345_project.model.repository.IAuthRepository;

public class RegisterViewModel extends ViewModel {

    // Live data keeping track of state changes
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navigateToMain = new MutableLiveData<>(false);

    // Repository interface instantiation
    private final IAuthRepository iAuthRepository = Injection.provideAuthRepository();

    // TODO: Implement registering input validation
    public boolean registerValidation(String[] registrationFields) {
        return true;
    }

    // Send user registration info to Model for registering with firebase
    public void registerUser(String[] registrationFields) {
        iAuthRepository.createUser(registrationFields, ((message, isSuccess) -> {
            if(isSuccess) {
                // Change screen on success
                navigateToMain.setValue(true);
            } else {
                // Display error message on screen if necessary
                errorMessage.setValue(message);
            }
        }));
    }


    // GETTERS AND SETTERS

    public MutableLiveData<Boolean> getNavigateToMain() {
        return navigateToMain;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
