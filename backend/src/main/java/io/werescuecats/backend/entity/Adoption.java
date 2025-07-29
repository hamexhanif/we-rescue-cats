package io.werescuecats.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
public class Adoption {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private User user;
    
    @Getter
    @Setter
    private Cat cat;
    
    @Getter
    @Setter
    private AdoptionStatus status;
    
    @Getter
    @Setter
    private LocalDateTime adoptionDate;
    
    @Getter
    @Setter
    private LocalDateTime approvedDate;
    
    @Getter
    @Setter
    private LocalDateTime completedDate;
    
    @Getter
    @Setter
    private String notes;
    
    @Getter
    @Setter
    private String adminNotes;
    
    @Getter
    @Setter
    private User processedByAdmin;
    
    @Getter
    @Setter
    private String tenantId;

    @Getter
    @Setter
    private LocalDateTime createdAt;
    
    @Getter
    @Setter
    private LocalDateTime updatedAt;
    
    public Adoption() {}
    
    public Adoption(User user, Cat cat) {
        this.user = user;
        this.cat = cat;
        this.adoptionDate = LocalDateTime.now();
        this.status = AdoptionStatus.PENDING;
    }
    
    public Adoption(User user, Cat cat, String notes) {
        this.user = user;
        this.cat = cat;
        this.adoptionDate = LocalDateTime.now();
        this.status = AdoptionStatus.PENDING;
        this.notes = notes;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (adoptionDate == null) {
            adoptionDate = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public boolean isPending() {
        return this.status == AdoptionStatus.PENDING;
    }
    
    public boolean isApproved() {
        return this.status == AdoptionStatus.APPROVED;
    }
    
    public boolean isCompleted() {
        return this.status == AdoptionStatus.COMPLETED;
    }
    
    public boolean isRejected() {
        return this.status == AdoptionStatus.REJECTED;
    }
    
    public boolean isCancelled() {
        return this.status == AdoptionStatus.CANCELLED;
    }
    
    public boolean isActive() {
        return this.status == AdoptionStatus.PENDING || this.status == AdoptionStatus.APPROVED;
    }
    
    public String getAdopterName() {
        return user != null ? user.getFullName() : "Unknown";
    }
    
    public String getCatName() {
        return cat != null ? cat.getName() : "Unknown";
    }
    
    public String getCatBreed() {
        return cat != null && cat.getBreed() != null ? cat.getBreed().getName() : "Unknown";
    }
    
    public long getDaysFromApplication() {
        return java.time.temporal.ChronoUnit.DAYS.between(adoptionDate.toLocalDate(), LocalDateTime.now().toLocalDate());
    }
    
    @Override
    public String toString() {
        return "Adoption{" +
                "id=" + id +
                ", user=" + (user != null ? user.getFullName() : "null") +
                ", cat=" + (cat != null ? cat.getName() : "null") +
                ", status=" + status +
                ", adoptionDate=" + adoptionDate +
                '}';
    }
}
