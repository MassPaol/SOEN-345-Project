package com.team_one.soen_345_project.model.repository.impl;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.team_one.soen_345_project.model.entity.User;
import com.team_one.soen_345_project.model.repository.IAuthRepository;
import com.team_one.soen_345_project.model.util.Callback;

public class FirebaseAuthRepository implements IAuthRepository {
    // Get the auth singleton for db authentication
    FirebaseAuth auth = FirebaseAuth.getInstance();

    // Get the firestore singleton for db interaction
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void createUser(String[] registrationFields, Callback callback) {
        // Deconstruct the array for easier use
        String firstName = registrationFields[0];
        String lastName = registrationFields[1];
        String email = registrationFields[2];
        String phoneNumber = registrationFields[3];
        String password = registrationFields[4];

        // First authenticates the data (e.g., email is valid, password is not weak, etc.)
        // then, it adds the user to the firestore user collection
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get unique ID if auth goes through
                        String uid = task.getResult().getUser().getUid();

                        // Create user profile in firestore
                        User newUser = new User(uid, firstName, lastName, email,
                                phoneNumber, password);
                        firestore.collection("user").document(uid).set(newUser);

                        // Update callback for communication with ViewModel
                        callback.onResult("success", true);
                    } else {
                        // Get the exception on auth failure
                        Exception e = task.getException();

                        // Get the corresponding error display message
                        String reason = getReason(e);

                        // Update callback for communication with ViewModel
                        callback.onResult(reason, false);
                    }
                });
    }

    @NonNull
    private static String getReason(Exception e) {
        String reason;

        // On auth failure check the exception
        if (e instanceof FirebaseAuthWeakPasswordException) {
            // Handle: Password too short
            reason = "Please use at least 6 characters.";

        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            // Handle: Bad email format
            reason = "That email address is invalid.";

        } else if (e instanceof FirebaseAuthUserCollisionException) {
            // Handle: Email already taken
            reason = "An account already exists with this email.";

        } else if (e instanceof FirebaseNetworkException) {
            // Handle: No internet
            reason = "Check your internet connection.";

        } else {
            // Handle: Generic/Unknown error
            reason = "An error occurred. Please try again.";
        }
        return reason;
    }
}
