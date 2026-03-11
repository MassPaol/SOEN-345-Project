package com.team_one.soen_345_project.model.util.filter;

public enum CategoryFilterOption implements FilterOption{
    ALL("", "All Categories"),
    SPORTS("Sports", "Sports"),
    ACADEMIC("Academic", "Academic"),
    SOCIAL("Social", "Social");

    private final String id;
    private final String label;

    CategoryFilterOption(String id, String label) {
        this.id = id;
        this.label = label;
    }

    @Override public String getLabel() { return label; } // what the dropdown renders
    @Override public String getId() { return id; }       // what you filter by later
}
