package io.werescuecats.backend.entity;

enum AdoptionStatus {
    PENDING("Pending Review", "Application submitted and awaiting review"),
    APPROVED("Approved", "Application approved, ready for pickup"),
    COMPLETED("Completed", "Cat successfully adopted"),
    REJECTED("Rejected", "Application was rejected"),
    CANCELLED("Cancelled", "Application was cancelled by adopter");
    
    private final String status;
    private final String description;
    
    AdoptionStatus(String status, String description) {
        this.status = status;
        this.description = description;
    }
    
    public String getStatus() {
        return status;
    }
    
    public String getDescription() {
        return description;
    }
}
