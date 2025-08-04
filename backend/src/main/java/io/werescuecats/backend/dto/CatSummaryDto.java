package io.werescuecats.backend.dto;

import io.werescuecats.backend.entity.CatStatus;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class CatSummaryDto {
    private Long id;
    private String name;
    private Integer age;
    private String breedName;
    private String imageUrl;
    private String address;
    private CatStatus status;
}
