package com.team_one.soen_345_project.viewmodel.register;

import java.util.Map;

public class RegisterUiState {
    private final Map<String, String> validationErrors;
    private final String generalError;

    public RegisterUiState(Map<String, String> validationErrors,
                           String generalError) {
        this.validationErrors = validationErrors;
        this.generalError = generalError;
    }

    public Map<String, String> getValidationErrors() { return validationErrors; }
    public String getGeneralError() { return generalError; }
}
