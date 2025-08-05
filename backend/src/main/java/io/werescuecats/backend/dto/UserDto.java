package io.werescuecats.backend.dto;

import io.werescuecats.backend.entity.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String streetAddress;
    private String postalCode;
    private UserRole role;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}
