package io.werescuecats.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdoptionRequestDto {
    private Long userId;
    private Long catId;
    private String notes;
}
