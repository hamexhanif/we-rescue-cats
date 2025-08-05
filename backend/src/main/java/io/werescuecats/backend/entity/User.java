package io.werescuecats.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "users")
public class User {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    @Column(name = "user_id")
    private Long id;
    
    @Getter @Setter
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Getter @Setter
    @Column(name = "password_hash", nullable = false)
    @JsonIgnore
    private String passwordHash;
    
    @Getter @Setter
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;
    
    @Getter @Setter
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;
    
    @Getter @Setter
    @Column(name = "street_address", length = 255)
    private String streetAddress;
    
    @Getter @Setter
    @Column(name = "postal_code", length = 5)
    private String postalCode;
    
    @Getter @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Getter @Setter
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
    
    @Getter @Setter
    @Column(name = "tenant_id", length = 50)
    private String tenantId;
    
    @Getter @Setter
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Getter @Setter
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Getter @Setter
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Getter @Setter
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Adoption> adoptions;
    
    public User() {}
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        if (streetAddress != null) address.append(streetAddress);
        if (postalCode != null) {
            if (address.length() > 0) address.append(" ");
            address.append(postalCode);
        }
        return address.toString();
    }
    
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }
    
    public boolean isRegularUser() {
        return this.role == UserRole.USER;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", role=" + role +
                '}';
    }
}
