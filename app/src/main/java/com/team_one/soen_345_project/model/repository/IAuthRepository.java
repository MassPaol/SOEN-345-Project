package com.team_one.soen_345_project.model.repository;

import com.team_one.soen_345_project.model.util.Callback;
public interface IAuthRepository {
    void createUser(String[] registrationFields, Callback callback);
}
