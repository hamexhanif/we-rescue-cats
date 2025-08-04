package io.werescuecats.backend.entity;

public enum CatStatus {
    AVAILABLE("Available for adoption"),
    PENDING("Adoption pending"),
    ADOPTED("Successfully adopted");
    
    private final String description;
    
    CatStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
