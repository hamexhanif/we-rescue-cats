package io.werescuecats.backend.dto;

import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@Builder
public class AnonymousAdoptionData {
    private LocalDateTime adoptionDate;
    private String catBreed;
    private Integer catAge;
    private String locationRegion;
    private String status;
    private String tenantId;
}