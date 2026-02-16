package com.team_one.soen_345_project.viewmodel.register;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Validator class for validating user input fields.
 * Provides methods to validate email format, password strength, and other input fields.
 */
public class RegisterInputValidator {

    // Email regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final int MIN_PASSWORD_LENGTH = 6;
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return password.length() >= MIN_PASSWORD_LENGTH;
    }

    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String phonePattern = "^\\+?[0-9]{7,15}$";
        return Pattern.compile(phonePattern).matcher(phone.trim()).matches();
    }

    public static boolean isNotEmpty(String field) {
        return field != null && !field.trim().isEmpty();
    }

    /**
     * This method validates all fields and returns specific error messages for the invalid fields.
     *
     * @param registrationFields Array containing [firstName, lastName, email, phone, password, confirmPassword]
     * @return Map containing field names as keys and error messages as values (empty if all valid)
     */
    public static Map<String, String> validateRegistrationFields(String[] registrationFields) {
        Map<String, String> validationStatusMap = new HashMap<>();

        // Validate array structure
        if (registrationFields == null || registrationFields.length != 6) {
            validationStatusMap.put("general", "Invalid registration data");
            return validationStatusMap;
        }

        String firstName = registrationFields[0];
        String lastName = registrationFields[1];
        String email = registrationFields[2];
        String phone = registrationFields[3];
        String password = registrationFields[4];
        String confirmPassword = registrationFields[5];

        // Validate each field and collect validationStatusMap
        if (!isNotEmpty(firstName)) {
            validationStatusMap.put("firstName", "First Name is required");
        }

        if (!isNotEmpty(lastName)) {
            validationStatusMap.put("lastName", "Last Name is required");
        }

        if (!isNotEmpty(email)) {
            validationStatusMap.put("email", "Email is required");
        } else if (!isValidEmail(email)) {
            validationStatusMap.put("email", "Invalid email format");
        }

        if (!isNotEmpty(phone)) {
            validationStatusMap.put("phone", "Phone Number is required");
        } else if (!isValidPhoneNumber(phone)) {
            validationStatusMap.put("phone", "Invalid phone format (7-15 digits)");
        }

        if (!isNotEmpty(password)) {
            validationStatusMap.put("password", "Password is required");
        } else if (!isValidPassword(password)) {
            validationStatusMap.put("password", "Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }

        if(!isNotEmpty(confirmPassword)) {
            validationStatusMap.put("confirmPassword", "Confirm Password is required");
        } else if (!password.equals(confirmPassword)) {
            validationStatusMap.put("confirmPassword", "Passwords do not match");
        }

        return validationStatusMap;
    }

    public int getMinPasswordLength() {
        return MIN_PASSWORD_LENGTH;
    }
}

