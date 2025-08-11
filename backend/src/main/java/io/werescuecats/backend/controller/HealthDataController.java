package io.werescuecats.backend.controller;

import io.werescuecats.backend.dto.AnonymousAdoptionData;
import io.werescuecats.backend.service.AdoptionService;
import io.werescuecats.backend.service.ApiTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/health-data")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class HealthDataController {
    
    private final AdoptionService adoptionService;
    private final ApiTokenService apiTokenService;
    
    @GetMapping("/anonymous-adoptions")
    public ResponseEntity<List<AnonymousAdoptionData>> getAnonymousData(
            @RequestHeader("X-API-Token") String apiToken) {
        
        if (!apiTokenService.isValidToken(apiToken)) {
            log.warn("Invalid API token attempted: {}", apiToken);
            return ResponseEntity.status(401).build();
        }
        
        log.info("Anonymous data accessed with token: {}", apiToken);
        List<AnonymousAdoptionData> data = adoptionService.getAnonymousAdoptionData();
        
        return ResponseEntity.ok(data);
    }
}