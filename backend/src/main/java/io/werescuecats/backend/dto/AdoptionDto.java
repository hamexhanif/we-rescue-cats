package io.werescuecats.backend.dto;

import lombok.Data;

@Data
public class AdoptionDto {
    private Long id;
    private String status;
    private String adoptionDate;
    private String approvedDate;
    private String completedDate;
    private String notes;
    private String adminNotes;
    private String tenantId;

    private AdoptionUserDto user;
    private AdoptionCatDto cat;
}
