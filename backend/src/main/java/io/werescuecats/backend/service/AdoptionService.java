package io.werescuecats.backend.service;

import io.werescuecats.backend.dto.AnonymousAdoptionData;
import io.werescuecats.backend.entity.Adoption;
import io.werescuecats.backend.entity.AdoptionStatus;
import io.werescuecats.backend.entity.Cat;
import io.werescuecats.backend.entity.CatStatus;
import io.werescuecats.backend.entity.User;
import io.werescuecats.backend.exception.CatNotAvailableException;
import io.werescuecats.backend.exception.ResourceNotFoundException;
import io.werescuecats.backend.repository.AdoptionRepository;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Validated
public class AdoptionService {
    
    private final AdoptionRepository adoptionRepository;
    private final CatService catService;
    private final UserService userService;

    public AdoptionService(AdoptionRepository adoptionRepository, CatService catService, UserService userService){
        this.adoptionRepository = adoptionRepository;
        this.catService = catService;
        this.userService = userService;
    }
    
    @Transactional
    public Adoption createAdoption(@NotNull Long userId, @NotNull Long catId, String notes) {
        Optional<User> userOpt = userService.getUserById(userId);
        Optional<Cat> catOpt = catService.getCatById(catId);
        
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        if (catOpt.isEmpty()) {
            throw new ResourceNotFoundException("Cat not found with id: " + catId);
        }
        
        Cat cat = catOpt.get();
        if (!cat.isAvailable()) {
            throw new CatNotAvailableException("Cat is not available for adoption: " + cat.getName());
        }
        
        Adoption adoption = new Adoption(userOpt.get(), cat, notes);

        catService.updateCatStatus(catId, CatStatus.PENDING);
        
        log.info("Creating adoption request for user {} and cat {}", 
                userOpt.get().getEmail(), cat.getName());
        
        return adoptionRepository.save(adoption);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Adoption approveAdoption(@NotNull Long adoptionId, @NotNull User admin) {
        return transitionAdoptionStatus(
            adoptionId,
            AdoptionStatus.PENDING,
            AdoptionStatus.APPROVED,
            admin,
            null,
            CatStatus.PENDING,
            false
        );
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Adoption completeAdoption(@NotNull Long adoptionId, @NotNull User admin) {
        return transitionAdoptionStatus(
            adoptionId,
            AdoptionStatus.APPROVED,
            AdoptionStatus.COMPLETED,
            admin,
            null,
            CatStatus.ADOPTED,
            true
        );
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Adoption rejectAdoption(@NotNull Long adoptionId, @NotNull User admin, String reason) {
        return transitionAdoptionStatus(
            adoptionId,
            AdoptionStatus.PENDING,
            AdoptionStatus.REJECTED,
            admin,
            reason,
            CatStatus.AVAILABLE,
            false
        );
    }
    
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<Adoption> getPendingAdoptions() {
        return adoptionRepository.findByStatus(AdoptionStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<Adoption> getAdoptionsByUser(@NotNull Long userId) {
        return adoptionRepository.findByUserId(userId);
    }
    
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<Adoption> getAllAdoptions() {
        return adoptionRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Optional<Adoption> getAdoptionById(@NotNull Long id) {
        return adoptionRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public long getUserAdoptionCount(@NotNull Long userId) {
        return adoptionRepository.countCompletedAdoptionsByUser(userId);
    }

    public List<AnonymousAdoptionData> getAnonymousAdoptionData() {
        List<Adoption> completedAdoptions = adoptionRepository.findAll().stream()
            .filter(adoption -> adoption.getStatus() == AdoptionStatus.COMPLETED)
            .collect(Collectors.toList());
        
        return completedAdoptions.stream()
            .map(adoption -> {
                return AnonymousAdoptionData.builder()
                    .adoptionDate(adoption.getAdoptionDate())
                    .catBreed(adoption.getCat().getBreed() != null ? 
                             adoption.getCat().getBreed().getName() : "Unknown")
                    .catAge(adoption.getCat().getAge())
                    .locationRegion(extractRegion(adoption.getUser().getStreetAddress()))
                    .status(adoption.getStatus().name())
                    .tenantId(adoption.getTenantId())
                    .build();
            })
            .collect(Collectors.toList());
    }

private String extractRegion(String address) {
    if (address == null || address.trim().isEmpty()) {
        return "Unknown";
    }
    String[] parts = address.split(",");
    return parts.length > 0 ? parts[parts.length - 1].trim() : "Unknown";
}
    private Adoption transitionAdoptionStatus(
        Long adoptionId,
        AdoptionStatus expectedCurrentStatus,
        AdoptionStatus newStatus,
        User admin,
        String adminNotes,
        CatStatus newCatStatus,
        boolean setCompletedDate){
        Adoption adoption = adoptionRepository.findById(adoptionId)
            .orElseThrow(() -> new RuntimeException("Adoption not found with id: " + adoptionId));

        if (adoption.getStatus() != expectedCurrentStatus) {
            throw new RuntimeException("Adoption must be " + expectedCurrentStatus + " to be transitioned to " + newStatus);
        }

        adoption.setStatus(newStatus);
        adoption.setProcessedByAdmin(admin);

        if (adminNotes != null) {
            adoption.setAdminNotes(adminNotes);
        }

        if (newStatus == AdoptionStatus.APPROVED) {
            adoption.setApprovedDate(LocalDateTime.now());
        }

        if (setCompletedDate) {
            adoption.setCompletedDate(LocalDateTime.now());
        }

        if (newCatStatus != null) {
            catService.updateCatStatus(adoption.getCat().getId(), newCatStatus);
        }

        log.info("Changed adoption {} to status {} by admin {}", adoptionId, newStatus, admin.getEmail());
        return adoptionRepository.save(adoption);
    }
}
