package io.werescuecats.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "cats")
public class Cat {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    @Column(name = "cat_id")
    private Long id;
    
    @Getter @Setter
    @NotBlank(message = "Cat name is required")
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Getter @Setter
    @Column(name = "age")
    private Integer age;
    
    @Getter @Setter
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Getter @Setter
    @NotNull(message = "Breed is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "breed_id", nullable = false)
    private Breed breed;
    
    @Getter @Setter
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Getter @Setter
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Column(name = "latitude")
    private Double latitude;
    
    @Getter @Setter
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Column(name = "longitude")
    private Double longitude;
    
    @Getter @Setter
    @Column(name = "address", length = 500)
    private String address;
    
    @Getter @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CatStatus status = CatStatus.AVAILABLE;
    
    @Getter @Setter
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Getter @Setter
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public Cat() {}
    
    public Cat(String name, Integer age, String description, Breed breed, Double latitude, Double longitude, String address) {
        this.name = name;
        this.age = age;
        this.description = description;
        this.breed = breed;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.status = CatStatus.AVAILABLE;
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
    
    public boolean isAvailable() {
        return this.status == CatStatus.AVAILABLE;
    }
    
    public boolean isAdopted() {
        return this.status == CatStatus.ADOPTED;
    }
    
    @Override
    public String toString() {
        return "Cat{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", status=" + status +
                ", breed=" + (breed != null ? breed.getName() : "null") +
                '}';
    }
}
