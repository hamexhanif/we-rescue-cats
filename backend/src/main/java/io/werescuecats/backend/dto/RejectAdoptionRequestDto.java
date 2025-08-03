package io.werescuecats.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectAdoptionRequestDto {
    @NotBlank
    private String reason;
}
