package com.team_one.soen_345_project.model.repository;

import com.team_one.soen_345_project.model.entity.User;
import com.team_one.soen_345_project.model.util.Callback;

public interface IAuthRepository {
    void createUser(String[] registrationFields, Callback callback);
    void loginUser(String email, String password, Callback callback);
}
