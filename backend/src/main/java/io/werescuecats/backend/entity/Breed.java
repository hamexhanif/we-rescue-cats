package io.werescuecats.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "breeds")
public class Breed {
    
    @Id
    @Getter
    @Setter
    @Column(name = "breed_id")
    private String id;

    @Getter
    @Setter
    @NotBlank(message = "Breed name is required")
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Getter
    @Setter
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Getter
    @Setter
    @Column(name = "origin", length = 100)
    private String origin;
    
    @Getter
    @Setter
    @Min(value = 1) @Max(value = 5)
    @Column(name = "adaptability")
    private Integer adaptability;
    
    @Getter
    @Setter
    @Min(value = 1) @Max(value = 5)
    @Column(name = "affection_level")
    private Integer affectionLevel;
    
    @Getter
    @Setter
    @Min(value = 1) @Max(value = 5)
    @Column(name = "child_friendly")
    private Integer childFriendly;
    
    @Getter
    @Setter
    @Min(value = 1) @Max(value = 5)
    @Column(name = "dog_friendly")
    private Integer dogFriendly;
    
    @Getter
    @Setter
    @Min(value = 1) @Max(value = 5)
    @Column(name = "energy_level")
    private Integer energyLevel;
    
    @Getter
    @Setter
    @Min(value = 1) @Max(value = 5)
    @Column(name = "grooming")
    private Integer grooming;
    
    @Getter
    @Setter
    @Min(value = 1) @Max(value = 5)
    @Column(name = "health_issues")
    private Integer healthIssues;
    
    @Getter
    @Setter
    @Min(value = 1) @Max(value = 5)
    @Column(name = "intelligence")
    private Integer intelligence;
    
    @Getter
    @Setter
    @Min(value = 1) @Max(value = 5)
    @Column(name = "social_needs")
    private Integer socialNeeds;
    
    @Getter
    @Setter
    @Min(value = 1) @Max(value = 5)
    @Column(name = "stranger_friendly")
    private Integer strangerFriendly;
    
    @Getter
    @Setter
    @Column(name = "wikipedia_url", length = 500)
    private String wikipediaUrl;
    
    @Getter
    @Setter
    @Column(name = "reference_image_id", length = 100)
    private String referenceImageId;
    
    @Getter
    @Setter
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Getter
    @Setter
    @OneToMany(mappedBy = "breed", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Cat> cats;
    
    public Breed() {}
    
    public Breed(String breedId, String name) {
        this.id = breedId;
        this.name = name;
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
