package io.werescuecats.backend.dto;

import io.werescuecats.backend.entity.CatStatus;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@Builder
public class CatDto {
    private Long id;
    private String name;
    private Integer age;
    private String gender;
    private String description;
    private String breedId;
    private String breedName;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
    private String address;
    private CatStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
