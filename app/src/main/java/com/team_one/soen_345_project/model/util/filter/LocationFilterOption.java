package com.team_one.soen_345_project.model.util.filter;

public class LocationFilterOption implements FilterOption{
    private final String city;

    public LocationFilterOption(String city) {
        this.city = city;
    }

    @Override public String getLabel() { return city; }
    @Override public String getId() { return city; }
}
