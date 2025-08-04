package io.werescuecats.backend.controller;

import io.werescuecats.backend.dto.AdoptionDto;
import io.werescuecats.backend.dto.AdoptionRequestDto;
import io.werescuecats.backend.dto.AdoptionStatsDto;
import io.werescuecats.backend.dto.AdoptionCatDto;
import io.werescuecats.backend.dto.RejectAdoptionRequestDto;
import io.werescuecats.backend.dto.AdoptionUserDto;
import io.werescuecats.backend.entity.Adoption;
import io.werescuecats.backend.entity.Cat;
import io.werescuecats.backend.entity.User;
import io.werescuecats.backend.security.CustomUserDetails;
import io.werescuecats.backend.service.AdoptionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/adoptions")
@CrossOrigin(origins = "*")
@Slf4j
public class AdoptionController {
    
    private final AdoptionService adoptionService;

    public AdoptionController(AdoptionService adoptionService){
        this.adoptionService = adoptionService;
    }
    
    @PostMapping
    public ResponseEntity<AdoptionDto> createAdoption(@RequestBody AdoptionRequestDto request) {
        log.info("Creating adoption request for user {} and cat {}", 
                request.getUserId(), request.getCatId());
            Adoption adoption = adoptionService.createAdoption(
                request.getUserId(), 
                request.getCatId(), 
                request.getNotes()
            );
            return ResponseEntity.ok(toAdoptionDto(adoption));
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdoptionDto>> getPendingAdoptions() {
        log.info("Fetching pending adoptions");
        List<Adoption> adoptions = adoptionService.getPendingAdoptions();
        List<AdoptionDto> dtos = adoptions.stream()
                                          .map(this::toAdoptionDto)
                                          .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AdoptionDto>> getAdoptionsByUser(@PathVariable Long userId) {
        log.info("Fetching adoptions for user: {}", userId);
        List<Adoption> adoptions = adoptionService.getAdoptionsByUser(userId);
        List<AdoptionDto> dtos = adoptions.stream()
                                          .map(this::toAdoptionDto)
                                          .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdoptionDto>> getAllAdoptions() {
        log.info("Fetching all adoptions");
        List<Adoption> adoptions = adoptionService.getAllAdoptions();
        List<AdoptionDto> dtos = adoptions.stream()
                                          .map(this::toAdoptionDto)
                                          .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdoptionDto> getAdoptionById(@PathVariable Long id) {
        log.info("Fetching adoption with ID: {}", id);
        Optional<Adoption> adoption = adoptionService.getAdoptionById(id);
        
        if (adoption.isPresent()) {
            return ResponseEntity.ok(toAdoptionDto(adoption.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdoptionDto> approveAdoption(@PathVariable Long id, 
                                                       @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        log.info("Approving adoption {} by admin {}", id, customUserDetails.getUser().getFullName());
        try {
            User admin = customUserDetails.getUser();
            Adoption adoption = adoptionService.approveAdoption(id, admin);
            return ResponseEntity.ok(toAdoptionDto(adoption));
        } catch (Exception e) {
            log.error("Error approving adoption", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdoptionDto> completeAdoption(@PathVariable Long id, 
                                                        @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        log.info("Completing adoption {} by admin {}", id, customUserDetails.getUser().getFullName());
        try {
            User admin = customUserDetails.getUser();
            Adoption adoption = adoptionService.completeAdoption(id, admin);
            return ResponseEntity.ok(toAdoptionDto(adoption));
        } catch (Exception e) {
            log.error("Error completing adoption", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdoptionDto> rejectAdoption(@PathVariable Long id, 
                                                      @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                      @Valid @RequestBody RejectAdoptionRequestDto request) {
        log.info("Rejecting adoption {} by admin {} with reason: {}", 
                id, customUserDetails.getUser().getFullName(), request.getReason());
        try {
            User admin = customUserDetails.getUser();
            Adoption adoption = adoptionService.rejectAdoption(id, admin, request.getReason());
            return ResponseEntity.ok(toAdoptionDto(adoption));
        } catch (Exception e) {
            log.error("Error rejecting adoption", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<AdoptionStatsDto> getUserAdoptionStats(@PathVariable Long userId) {
        log.info("Fetching adoption stats for user: {}", userId);
        
        long completedAdoptions = adoptionService.getUserAdoptionCount(userId);
        List<Adoption> userAdoptions = adoptionService.getAdoptionsByUser(userId);
        
        AdoptionStatsDto stats = new AdoptionStatsDto();
        stats.setUserId(userId);
        stats.setTotalApplications(userAdoptions.size());
        stats.setCompletedAdoptions(completedAdoptions);
        stats.setPendingApplications(userAdoptions.stream()
                .mapToInt(a -> a.isPending() ? 1 : 0)
                .sum());
        
        return ResponseEntity.ok(stats);
    }

    public AdoptionDto toAdoptionDto(Adoption adoption) {
        AdoptionDto dto = new AdoptionDto();
        dto.setId(adoption.getId());
        dto.setStatus(adoption.getStatus().name());
        dto.setAdoptionDate(adoption.getAdoptionDate().toString());
        dto.setApprovedDate(adoption.getApprovedDate() != null ? adoption.getApprovedDate().toString() : null);
        dto.setCompletedDate(adoption.getCompletedDate() != null ? adoption.getCompletedDate().toString() : null);
        dto.setNotes(adoption.getNotes());
        dto.setAdminNotes(adoption.getAdminNotes());
        dto.setTenantId(adoption.getTenantId());

        // Map user
        User user = adoption.getUser();
        if (user != null) {
            AdoptionUserDto userDto = new AdoptionUserDto();
            userDto.setId(user.getId());
            userDto.setEmail(user.getEmail());
            userDto.setFirstName(user.getFirstName());
            userDto.setLastName(user.getLastName());
            dto.setUser(userDto);
        }

        // Map cat
        Cat cat = adoption.getCat();
        if (cat != null) {
            AdoptionCatDto catDto = new AdoptionCatDto();
            catDto.setId(cat.getId());
            catDto.setName(cat.getName());
            catDto.setBreed(cat.getBreed() != null ? cat.getBreed().getName() : null);
            dto.setCat(catDto);
        }

        return dto;
    }
}
