package io.werescuecats.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardStatsDto {
    private int totalCats;
    private int availableCats;
    private int adoptedCats;
    private int totalUsers;
    private int adminUsers;
    private int totalAdoptions;
    private int pendingAdoptions;
    private int completedAdoptions;
}
