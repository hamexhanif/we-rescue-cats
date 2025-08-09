package io.werescuecats.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterResponseDto {
    private boolean success;
    private String message;
    private UserDto user;
}
