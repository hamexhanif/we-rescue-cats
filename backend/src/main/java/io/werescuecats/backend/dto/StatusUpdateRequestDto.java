package io.werescuecats.backend.dto;

import io.werescuecats.backend.entity.CatStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusUpdateRequestDto {
    private CatStatus status;
}
