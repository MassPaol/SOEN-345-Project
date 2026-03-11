package com.team_one.soen_345_project.model.util.filter;

import com.google.firebase.Timestamp;

public class FilterState {
    private FilterOption category;       // CategoryFilterOption
    private FilterOption location;       // LocationFilterOption
    private Timestamp dateFrom;
    private Timestamp dateTo;
    private boolean availableOnly;       // capacity checkbox
    private Double minPrice;
    private Double maxPrice;

    public FilterState() {
        this.category = CategoryFilterOption.ALL;
        this.location = null;
        this.dateFrom = null;
        this.dateTo = null;
        this.availableOnly = false;
        this.minPrice = 0.0;
        this. maxPrice = 1000.0;
    }

    @Override
    public String toString() {
        return "FilterState{" +
                "category=" + (category != null ? category.getLabel() : "All Categories") +
                ", location=" + (location != null ? location.getLabel() : "All Locations") +
                ", dateFrom=" + (dateFrom != null ? dateFrom.toDate().toString() : "Any") +
                ", dateTo=" + (dateTo != null ? dateTo.toDate().toString() : "Any") +
                ", availableOnly=" + availableOnly +
                ", minPrice=" + (minPrice != null ? minPrice : "Any") +
                ", maxPrice=" + (maxPrice != null ? maxPrice : "Any") +
                '}';
    }

    public FilterOption getCategory() {
        return category;
    }

    public void setCategory(FilterOption category) {
        this.category = category;
    }

    public FilterOption getLocation() {
        return location;
    }

    public void setLocation(FilterOption location) {
        this.location = location;
    }

    public Timestamp getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Timestamp dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Timestamp getDateTo() {
        return dateTo;
    }

    public void setDateTo(Timestamp dateTo) {
        this.dateTo = dateTo;
    }

    public boolean isAvailableOnly() {
        return availableOnly;
    }

    public void setAvailableOnly(boolean availableOnly) {
        this.availableOnly = availableOnly;
    }

    public Double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Double minPrice) {
        this.minPrice = minPrice;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
    }
}
