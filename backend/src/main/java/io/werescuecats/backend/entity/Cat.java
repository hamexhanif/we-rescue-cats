package io.werescuecats.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Getter
@Setter
@AllArgsConstructor
@Table(name = "cats")
public class Cat {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cat_id")
    private Long id;
    
    @NotBlank(message = "Cat name is required")
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "age")
    private Integer age;

    @Column(name = "gender")
    private String gender;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Breed is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "breed_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Breed breed;
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Column(name = "latitude")
    private Double latitude;
    
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "address", length = 500)
    private String address;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CatStatus status = CatStatus.AVAILABLE;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
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
