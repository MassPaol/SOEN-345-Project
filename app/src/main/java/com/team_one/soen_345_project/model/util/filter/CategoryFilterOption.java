package com.team_one.soen_345_project.model.util.filter;

public enum CategoryFilterOption implements FilterOption{
    ALL("all", "All Categories"),
    SPORTS("sports", "Sports"),
    ACADEMIC("academic", "Academic"),
    SOCIAL("social", "Social"),
    MUSIC("music", "Music"),
    ARTS("arts", "Arts & Culture"),
    FOOD("food", "Food & Drink"),
    NETWORKING("networking", "Networking"),
    TECH("tech", "Technology"),
    HEALTH("health", "Health & Wellness"),
    OUTDOOR("outdoor", "Outdoor & Nature"),
    CHARITY("charity", "Charity & Volunteering"),
    GAMING("gaming", "Gaming"),
    FAMILY("family", "Family & Kids");

    private final String id;
    private final String label;

    CategoryFilterOption(String id, String label) {
        this.id = id;
        this.label = label;
    }

    @Override public String getLabel() { return label; } // what the dropdown renders
    @Override public String getId() { return id; }       // what you filter by later
}
