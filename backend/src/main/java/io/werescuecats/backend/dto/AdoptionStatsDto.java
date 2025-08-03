package io.werescuecats.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdoptionStatsDto {
    private Long userId;
    private int totalApplications;
    private long completedAdoptions;
    private int pendingApplications;
}
