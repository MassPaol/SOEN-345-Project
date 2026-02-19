package com.team_one.soen_345_project.viewmodel.login;

public class LoginUiState {
    private final String errorMessage;
    private final boolean isSuccess;
    private final boolean isLoading;

    public LoginUiState(String errorMessage, boolean isSuccess, boolean isLoading) {
        this.errorMessage = errorMessage;
        this.isSuccess = isSuccess;
        this.isLoading = isLoading;
    }

    public String getErrorMessage() { return errorMessage; }
    public boolean isSuccess() { return isSuccess; }
    public boolean isLoading() { return isLoading; }
}
