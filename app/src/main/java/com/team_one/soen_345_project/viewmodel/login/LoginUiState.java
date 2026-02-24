package com.team_one.soen_345_project.viewmodel.login;

public class LoginUiState {
    private final String errorMessage;
    private final boolean isSuccess;
    private final boolean isLoading;
    private final boolean isAdmin;

    public LoginUiState(String errorMessage, boolean isSuccess, boolean isLoading, boolean isAdmin) {
        this.errorMessage = errorMessage;
        this.isSuccess = isSuccess;
        this.isLoading = isLoading;
        this.isAdmin = isAdmin;
    }

    public String getErrorMessage() { return errorMessage; }
    public boolean isSuccess() { return isSuccess; }
    public boolean isLoading() { return isLoading; }

    public boolean isAdmin() {
        return isAdmin;
    }
}
