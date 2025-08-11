package io.werescuecats.backend.controller;

import io.werescuecats.backend.dto.DashboardStatsDto;
import io.werescuecats.backend.entity.AdoptionStatus;
import io.werescuecats.backend.entity.CatStatus;
import io.werescuecats.backend.service.AdoptionService;
import io.werescuecats.backend.service.CatService;
import io.werescuecats.backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
@Slf4j
public class DashboardController {
    
    @Autowired
    private CatService catService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AdoptionService adoptionService;
    
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        log.info("Fetching dashboard statistics");
        
        DashboardStatsDto stats = new DashboardStatsDto();
        
        stats.setTotalCats(catService.getAllCats().size());
        stats.setAvailableCats(catService.getAvailableCats().size());
        stats.setAdoptedCats((int) catService.getAllCats().stream()
                .filter(cat -> cat.getStatus() == CatStatus.ADOPTED)
                .count());
        
        stats.setTotalUsers(userService.getAllUsers().size());
        stats.setAdminUsers(userService.getAdminUsers().size());
        
        stats.setTotalAdoptions(adoptionService.getAllAdoptions().size());
        stats.setPendingAdoptions((int) adoptionService.getAllAdoptions().stream()
                .filter(adoption -> adoption.getStatus() == AdoptionStatus.PENDING)
                .count());
        stats.setCompletedAdoptions((int) adoptionService.getAllAdoptions().stream()
                .filter(adoption -> adoption.getStatus() == AdoptionStatus.COMPLETED)
                .count());
        
        return ResponseEntity.ok(stats);
    }
}
