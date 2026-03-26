package com.team_one.soen_345_project.viewmodel.reserve;

public class ReserveEventUiState {
    private final String message;
    private final boolean isSuccess;
    private final boolean isAlreadyBooked;

    private ReserveEventUiState(Builder builder) {
        this.message = builder.message;
        this.isSuccess = builder.isSuccess;
        this.isAlreadyBooked = builder.isAlreadyBooked;
    }

    public String getMessage() { return message; }
    public boolean isSuccess() { return isSuccess; }
    public boolean isAlreadyBooked() { return isAlreadyBooked; }

    public static class Builder {
        private String message;
        private boolean isSuccess = false;
        private boolean isAlreadyBooked = false;

        public Builder() {}

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder isSuccess(boolean isSuccess) {
            this.isSuccess = isSuccess;
            return this;
        }

        public Builder isAlreadyBooked(boolean isAlreadyBooked) {
            this.isAlreadyBooked = isAlreadyBooked;
            return this;
        }

        public ReserveEventUiState build() {
            return new ReserveEventUiState(this);
        }
    }
}
