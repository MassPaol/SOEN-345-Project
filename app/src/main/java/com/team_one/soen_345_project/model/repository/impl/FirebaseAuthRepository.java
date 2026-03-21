package com.team_one.soen_345_project.model.repository.impl;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.team_one.soen_345_project.model.entity.User;
import com.team_one.soen_345_project.model.repository.IAuthRepository;
import com.team_one.soen_345_project.model.util.callback.Callback;

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

    // implement the loginUser contract
    @Override
    public void loginUser(String email, String password, Callback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            checkIfAdmin(user.getUid(), callback);
                        } else {
                            callback.onResult("success", true, false);
                        }
                    } else {
                        Exception e = task.getException();
                        String reason = getReason(e);
                        callback.onResult(reason, false, false);
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

        } else if (e instanceof FirebaseAuthInvalidCredentialsException || e instanceof FirebaseAuthInvalidUserException) {
            //UPDATED:  Handle: Bad email format, wrong password, or user not found for additional security ( user attempting to log in should not know if its the email or password that are wrong)
            reason = "Invalid email or password.";

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

    // Utility method for on login checking if a user is an admin or not
    private void checkIfAdmin(String uid, Callback callback) {
        firestore.collection("user")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    boolean isAdmin = Boolean.TRUE.equals(doc.getBoolean("isAdmin"));
                    callback.onResult("success", true, isAdmin);
                })
                .addOnFailureListener(e -> {
                    // Still logged in, just couldn't verify admin status
                    callback.onResult("success", true, false);
                });
    }

    @Override
    public String getCurrentUserUid() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }
}
