package io.werescuecats.backend.entity;

enum UserRole {
    USER("Regular User", "Can adopt cats and view information"),
    ADMIN("Administrator", "Full access to manage cats, breeds, and adoptions");
    
    private final String role;
    private final String description;
    
    UserRole(String role, String description) {
        this.role = role;
        this.description = description;
    }
    
    public String getRole() {
        return role;
    }
    
    public String getDescription() {
        return description;
    }
}
