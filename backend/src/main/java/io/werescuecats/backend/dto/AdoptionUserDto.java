package io.werescuecats.backend.dto;

import lombok.Data;

@Data
public class AdoptionUserDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
}
