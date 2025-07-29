package io.werescuecats.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Breed {
    
    @Id
    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private String name;
    
    @Getter
    @Setter
    private String description;
    
    @Getter
    @Setter
    private String origin;
    
    @Getter
    @Setter
    private Integer adaptability;
    
    @Getter
    @Setter
    private Integer affectionLevel;
    
    @Getter
    @Setter
    private Integer childFriendly;
    
    @Getter
    @Setter
    private Integer dogFriendly;
    
    @Getter
    @Setter
    private Integer energyLevel;
    
    @Getter
    @Setter
    private Integer grooming;
    
    @Getter
    @Setter
    private Integer healthIssues;
    
    @Getter
    @Setter
    private Integer intelligence;
    
    @Getter
    @Setter
    private Integer socialNeeds;
    
    @Getter
    @Setter
    private Integer strangerFriendly;
    
    @Getter
    @Setter
    private String wikipediaUrl;
    
    @Getter
    @Setter
    private String referenceImageId;
    
    @Getter
    @Setter
    private String imageUrl;
    
    @Getter
    @Setter
    private LocalDateTime createdAt;
    
    @Getter
    @Setter
    private LocalDateTime updatedAt;
    
    @Getter
    @Setter
    private List<Cat> cats;
    
    public Breed() {}
    
    public Breed(String breedId, String name) {
        this.id = breedId;
        this.name = name;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public boolean isGoodWithKids() {
        return childFriendly != null && childFriendly >= 4;
    }
    
    public boolean isGoodWithDogs() {
        return dogFriendly != null && dogFriendly >= 4;
    }
    
    public boolean isHighEnergy() {
        return energyLevel != null && energyLevel >= 4;
    }
    
    public boolean isLowMaintenance() {
        return grooming != null && grooming <= 2;
    }
    
    @Override
    public String toString() {
        return "Breed{" +
                "breedId='" + id + '\'' +
                ", name='" + name + '\'' +
                ", origin='" + origin + '\'' +
                ", childFriendly=" + (isGoodWithKids() ? "Yes" : "No") +
                ", dogFriendly=" + (isGoodWithDogs() ? "Yes" : "No") +
                '}';
    }
}