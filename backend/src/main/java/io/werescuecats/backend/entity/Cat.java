package io.werescuecats.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
public class Cat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;
    
    @Getter
    @Setter
    private String name;
    
    @Getter
    @Setter
    private Integer age;
    
    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private Breed breed;
    
    @Getter
    @Setter
    private String imageUrl;
    
    @Getter
    @Setter
    private Double latitude;
    
    @Getter
    @Setter
    private Double longitude;
    
    @Getter
    @Setter
    private String address;
    
    @Getter
    @Setter
    private CatStatus status = CatStatus.AVAILABLE;
    
    @Getter
    @Setter
    private LocalDateTime createdAt;
    
    @Getter
    @Setter
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
