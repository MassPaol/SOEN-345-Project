package com.team_one.soen_345_project.model.repository;

import com.team_one.soen_345_project.model.util.Callback;

import java.util.HashMap;

public interface IEventRepository {
    public void saveEvent(HashMap<String, String> eventInfo, Callback callback);
}
