package com.team_one.soen_345_project.viewmodel.register;

import org.junit.Test;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for RegisterInputValidator class.
 * Tests all validation methods for various input scenarios.
 */
public class RegisterInputValidatorTest {

    // ========== Email Validation Tests ==========

    @Test
    public void testIsValidEmail_WithValidEmail_ReturnsTrue() {
        assertTrue(RegisterInputValidator.isValidEmail("user@example.com"));
        assertTrue(RegisterInputValidator.isValidEmail("john.doe@example.co.uk"));
        assertTrue(RegisterInputValidator.isValidEmail("user+tag@domain.com"));
        assertTrue(RegisterInputValidator.isValidEmail("firstname.lastname@example.com"));
        assertTrue(RegisterInputValidator.isValidEmail("user_name@example-domain.com"));
    }

    @Test
    public void testIsValidEmail_WithEmptyEmail_ReturnsFalse() {
        assertFalse(RegisterInputValidator.isValidEmail(""));
        assertFalse(RegisterInputValidator.isValidEmail("   "));
    }

    @Test
    public void testIsValidEmail_WithNullEmail_ReturnsFalse() {
        assertFalse(RegisterInputValidator.isValidEmail(null));
    }

    @Test
    public void testIsValidEmail_WithInvalidEmailFormat_ReturnsFalse() {
        assertFalse(RegisterInputValidator.isValidEmail("plainaddress"));
        assertFalse(RegisterInputValidator.isValidEmail("@missinglocal.com"));
        assertFalse(RegisterInputValidator.isValidEmail("user@com"));
        assertFalse(RegisterInputValidator.isValidEmail("user@.com"));
        assertFalse(RegisterInputValidator.isValidEmail("user name@example.com"));
        assertFalse(RegisterInputValidator.isValidEmail("user@domain"));
        assertFalse(RegisterInputValidator.isValidEmail("user<>@example.com"));
    }

    @Test
    public void testIsValidEmail_WithWhitespace_TrimsAndValidates() {
        assertTrue(RegisterInputValidator.isValidEmail("  user@example.com  "));
    }

    // ========== Password Validation Tests ==========

    @Test
    public void testIsValidPassword_WithValidPassword_ReturnsTrue() {
        assertTrue(RegisterInputValidator.isValidPassword("password123"));
        assertTrue(RegisterInputValidator.isValidPassword("123456"));
        assertTrue(RegisterInputValidator.isValidPassword("abcdef"));
        assertTrue(RegisterInputValidator.isValidPassword("Pass123!@#"));
    }

    @Test
    public void testIsValidPassword_WithShortPassword_ReturnsFalse() {
        assertFalse(RegisterInputValidator.isValidPassword("pass"));
        assertFalse(RegisterInputValidator.isValidPassword("12345"));
        assertFalse(RegisterInputValidator.isValidPassword("abc"));
    }

    @Test
    public void testIsValidPassword_WithEmptyPassword_ReturnsFalse() {
        assertFalse(RegisterInputValidator.isValidPassword(""));
    }

    @Test
    public void testIsValidPassword_WithNullPassword_ReturnsFalse() {
        assertFalse(RegisterInputValidator.isValidPassword(null));
    }

    @Test
    public void testIsValidPassword_WithExactMinLength_ReturnsTrue() {
        assertTrue(RegisterInputValidator.isValidPassword("123456")); // Exactly 6 characters
    }

    // ========== Phone Number Validation Tests ==========

    @Test
    public void testIsValidPhoneNumber_WithValidPhone_ReturnsTrue() {
        assertTrue(RegisterInputValidator.isValidPhoneNumber("1234567"));      // 7 digits (min)
        assertTrue(RegisterInputValidator.isValidPhoneNumber("1234567890"));   // 10 digits
        assertTrue(RegisterInputValidator.isValidPhoneNumber("123456789012345")); // 15 digits (max)
        assertTrue(RegisterInputValidator.isValidPhoneNumber("+1234567890"));  // With +
        assertTrue(RegisterInputValidator.isValidPhoneNumber("+123456789012345")); // With + (max)
    }

    @Test
    public void testIsValidPhoneNumber_WithInvalidPhone_ReturnsFalse() {
        assertFalse(RegisterInputValidator.isValidPhoneNumber("123456"));      // Too short (6 digits)
        assertFalse(RegisterInputValidator.isValidPhoneNumber("1234567890123456")); // Too long (16 digits)
        assertFalse(RegisterInputValidator.isValidPhoneNumber("123-456-7890")); // Contains dashes
        assertFalse(RegisterInputValidator.isValidPhoneNumber("(123)4567890")); // Contains parentheses
        assertFalse(RegisterInputValidator.isValidPhoneNumber("123 456 7890")); // Contains spaces
        assertFalse(RegisterInputValidator.isValidPhoneNumber("abcdefghij"));  // Contains letters
    }

    @Test
    public void testIsValidPhoneNumber_WithEmptyPhone_ReturnsFalse() {
        assertFalse(RegisterInputValidator.isValidPhoneNumber(""));
        assertFalse(RegisterInputValidator.isValidPhoneNumber("   "));
    }

    @Test
    public void testIsValidPhoneNumber_WithNullPhone_ReturnsFalse() {
        assertFalse(RegisterInputValidator.isValidPhoneNumber(null));
    }

    @Test
    public void testIsValidPhoneNumber_WithWhitespace_TrimsAndValidates() {
        assertTrue(RegisterInputValidator.isValidPhoneNumber("  1234567890  "));
    }

    // ========== isNotEmpty Tests ==========

    @Test
    public void testIsNotEmpty_WithValidString_ReturnsTrue() {
        assertTrue(RegisterInputValidator.isNotEmpty("John"));
        assertTrue(RegisterInputValidator.isNotEmpty("a"));
        assertTrue(RegisterInputValidator.isNotEmpty("  text  ")); // Whitespace around text
    }

    @Test
    public void testIsNotEmpty_WithEmptyString_ReturnsFalse() {
        assertFalse(RegisterInputValidator.isNotEmpty(""));
        assertFalse(RegisterInputValidator.isNotEmpty("   "));
        assertFalse(RegisterInputValidator.isNotEmpty("\t\n"));
    }

    @Test
    public void testIsNotEmpty_WithNullString_ReturnsFalse() {
        assertFalse(RegisterInputValidator.isNotEmpty(null));
    }

    // ========== Full Registration Validation Tests ==========

    @Test
    public void testValidateRegistrationFields_WithValidInputs_ReturnsEmptyMap() {
        String[] validFields = {
                "John",
                "Doe",
                "john@example.com",
                "1234567890",
                "password123",
                "password123"
        };

        Map<String, String> errors = RegisterInputValidator.validateRegistrationFields(validFields);
        assertTrue("Expected no validation errors", errors.isEmpty());
    }

    @Test
    public void testValidateRegistrationFields_WithEmptyFirstName_ReturnsError() {
        String[] fields = {"", "Doe", "john@example.com", "1234567890", "password123", "password123"};

        Map<String, String> errors = RegisterInputValidator.validateRegistrationFields(fields);

        assertFalse(errors.isEmpty());
        assertTrue(errors.containsKey("firstName"));
        assertEquals("First Name is required", errors.get("firstName"));
    }

    @Test
    public void testValidateRegistrationFields_WithEmptyLastName_ReturnsError() {
        String[] fields = {"John", "", "john@example.com", "1234567890", "password123", "password123"};

        Map<String, String> errors = RegisterInputValidator.validateRegistrationFields(fields);

        assertFalse(errors.isEmpty());
        assertTrue(errors.containsKey("lastName"));
        assertEquals("Last Name is required", errors.get("lastName"));
    }

    @Test
    public void testValidateRegistrationFields_WithEmptyEmail_ReturnsError() {
        String[] fields = {"John", "Doe", "", "1234567890", "password123", "password123"};

        Map<String, String> errors = RegisterInputValidator.validateRegistrationFields(fields);

        assertFalse(errors.isEmpty());
        assertTrue(errors.containsKey("email"));
        assertEquals("Email is required", errors.get("email"));
    }

    @Test
    public void testValidateRegistrationFields_WithInvalidEmailFormat_ReturnsError() {
        String[] fields = {"John", "Doe", "invalid@", "1234567890", "password123", "password123"};

        Map<String, String> errors = RegisterInputValidator.validateRegistrationFields(fields);

        assertFalse(errors.isEmpty());
        assertTrue(errors.containsKey("email"));
        assertEquals("Invalid email format", errors.get("email"));
    }

    @Test
    public void testValidateRegistrationFields_WithEmptyPhone_ReturnsError() {
        String[] fields = {"John", "Doe", "john@example.com", "", "password123", "password123"};

        Map<String, String> errors = RegisterInputValidator.validateRegistrationFields(fields);

        assertFalse(errors.isEmpty());
        assertTrue(errors.containsKey("phone"));
        assertEquals("Phone Number is required", errors.get("phone"));
    }

    @Test
    public void testValidateRegistrationFields_WithInvalidPhoneFormat_ReturnsError() {
        String[] fields = {"John", "Doe", "john@example.com", "123", "password123", "password123"};

        Map<String, String> errors = RegisterInputValidator.validateRegistrationFields(fields);

        assertFalse(errors.isEmpty());
        assertTrue(errors.containsKey("phone"));
        assertEquals("Invalid phone format (7-15 digits)", errors.get("phone"));
    }

    @Test
    public void testValidateRegistrationFields_WithEmptyPassword_ReturnsError() {
        String[] fields = {"John", "Doe", "john@example.com", "1234567890", "", ""};

        Map<String, String> errors = RegisterInputValidator.validateRegistrationFields(fields);

        assertFalse(errors.isEmpty());
        assertTrue(errors.containsKey("password"));
        assertEquals("Password is required", errors.get("password"));
    }

    @Test
    public void testValidateRegistrationFields_WithShortPassword_ReturnsError() {
        String[] fields = {"John", "Doe", "john@example.com", "1234567890", "pass", "pass"};

        Map<String, String> errors = RegisterInputValidator.validateRegistrationFields(fields);

        assertFalse(errors.isEmpty());
        assertTrue(errors.containsKey("password"));
        assertEquals("Password must be at least 6 characters", errors.get("password"));
    }

    @Test
    public void testValidateRegistrationFields_WithEmptyConfirmPassword_ReturnsError() {
        String[] fields = {"John", "Doe", "john@example.com", "1234567890", "password123", ""};

        Map<String, String> errors = RegisterInputValidator.validateRegistrationFields(fields);

        assertFalse(errors.isEmpty());
        assertTrue(errors.containsKey("confirmPassword"));
        assertEquals("Confirm Password is required", errors.get("confirmPassword"));
    }

    @Test
    public void testValidateRegistrationFields_WithMismatchedPasswords_ReturnsError() {
        String[] fields = {"John", "Doe", "john@example.com", "1234567890", "password123", "password456"};

        Map<String, String> errors = RegisterInputValidator.validateRegistrationFields(fields);

        assertFalse(errors.isEmpty());
        assertTrue(errors.containsKey("confirmPassword"));
        assertEquals("Passwords do not match", errors.get("confirmPassword"));
    }

    @Test
    public void testValidateRegistrationFields_WithMultipleErrors_ReturnsAllErrors() {
        String[] fields = {"", "", "invalid@", "123", "pass", "different"};

        Map<String, String> errors = RegisterInputValidator.validateRegistrationFields(fields);

        assertEquals(6, errors.size());
        assertTrue(errors.containsKey("firstName"));
        assertTrue(errors.containsKey("lastName"));
        assertTrue(errors.containsKey("email"));
        assertTrue(errors.containsKey("phone"));
        assertTrue(errors.containsKey("password"));
        assertTrue(errors.containsKey("confirmPassword"));
    }

    @Test
    public void testValidateRegistrationFields_WithNullArray_ReturnsGeneralError() {
        Map<String, String> errors = RegisterInputValidator.validateRegistrationFields(null);

        assertFalse(errors.isEmpty());
        assertTrue(errors.containsKey("general"));
        assertEquals("Invalid registration data", errors.get("general"));
    }

    @Test
    public void testValidateRegistrationFields_WithWrongArrayLength_ReturnsGeneralError() {
        String[] fields = {"John", "Doe", "john@example.com"}; // Only 3 elements instead of 6

        Map<String, String> errors = RegisterInputValidator.validateRegistrationFields(fields);

        assertFalse(errors.isEmpty());
        assertTrue(errors.containsKey("general"));
        assertEquals("Invalid registration data", errors.get("general"));
    }

    @Test
    public void testValidateRegistrationFields_WithAllEmptyFields_ReturnsAllFieldErrors() {
        String[] fields = {"", "", "", "", "", ""};

        Map<String, String> errors = RegisterInputValidator.validateRegistrationFields(fields);

        assertEquals(6, errors.size());
        assertTrue(errors.containsKey("firstName"));
        assertTrue(errors.containsKey("lastName"));
        assertTrue(errors.containsKey("email"));
        assertTrue(errors.containsKey("phone"));
        assertTrue(errors.containsKey("password"));
        assertTrue(errors.containsKey("confirmPassword"));
    }

    @Test
    public void testValidateRegistrationFields_WithWhitespaceFields_ReturnsErrors() {
        String[] fields = {"   ", "   ", "   ", "   ", "   ", "   "};

        Map<String, String> errors = RegisterInputValidator.validateRegistrationFields(fields);

        assertEquals(6, errors.size());
        assertTrue(errors.containsKey("firstName"));
        assertTrue(errors.containsKey("lastName"));
        assertTrue(errors.containsKey("email"));
        assertTrue(errors.containsKey("phone"));
        assertTrue(errors.containsKey("password"));
        assertTrue(errors.containsKey("confirmPassword"));
    }

    @Test
    public void testValidateRegistrationFields_WithValidFieldsAndWhitespace_TrimsAndValidates() {
        String[] fields = {
                "  John  ",
                "  Doe  ",
                "  john@example.com  ",
                "  1234567890  ",
                "password123",
                "password123"
        };

        Map<String, String> errors = RegisterInputValidator.validateRegistrationFields(fields);

        assertTrue("Expected no validation errors with trimmed whitespace", errors.isEmpty());
    }
}

