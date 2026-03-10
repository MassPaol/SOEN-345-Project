package com.team_one.soen_345_project.model.repository;

import com.team_one.soen_345_project.model.util.callback.Callback;

public interface IAuthRepository {
    void createUser(String[] registrationFields, Callback callback);
    void loginUser(String email, String password, Callback callback);
}
