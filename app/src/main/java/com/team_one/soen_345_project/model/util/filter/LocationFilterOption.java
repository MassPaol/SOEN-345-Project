package com.team_one.soen_345_project.model.util.filter;

public enum LocationFilterOption implements FilterOption{
    ALL("all", "All Locations"),
    MONTREAL("mtl", "Montreal, QC"),
    LAVAL("lav", "Laval, QC"),
    LONGUEUIL("lon", "Longueuil, QC"),
    QUEBEC_CITY("qc", "Quebec City, QC"),
    GATINEAU("gat", "Gatineau, QC"),
    SHERBROOKE("she", "Sherbrooke, QC"),
    TROIS_RIVIERES("tri", "Trois-Rivieres, QC"),
    OTTAWA("ott", "Ottawa, ON"),
    TORONTO("tor", "Toronto, ON");


    private final String id;
    private final String label;

    LocationFilterOption (String id, String label) {
        this.id = id;
        this.label = label;
    }

    @Override public String getLabel() { return label; } // what the dropdown renders
    @Override public String getId() { return id; }       // what you filter by later
}
