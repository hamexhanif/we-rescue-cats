package io.werescuecats.backend.controller;

import io.werescuecats.backend.dto.CatDto;
import io.werescuecats.backend.dto.CatSummaryDto;
import io.werescuecats.backend.dto.StatusUpdateRequestDto;
import io.werescuecats.backend.entity.Cat;
import io.werescuecats.backend.service.CatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cats")
@CrossOrigin(origins = "*")
@Slf4j
public class CatController {
    
    @Autowired
    private CatService catService;
    
    @GetMapping("/available")
    public ResponseEntity<List<CatSummaryDto>> getAvailableCats() {
        log.info("Fetching available cats");
        List<Cat> cats = catService.getAvailableCats();
        List<CatSummaryDto> catDtos = cats.stream()
            .map(this::toSummaryDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(catDtos);
    }
    
    @GetMapping("/breed/{breedId}")
    public ResponseEntity<List<CatSummaryDto>> getCatsByBreed(@PathVariable String breedId) {
        log.info("Fetching cats for breed: {}", breedId);
        List<Cat> cats = catService.getCatsByBreed(breedId);
        List<CatSummaryDto> catDtos = cats.stream()
            .map(this::toSummaryDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(catDtos);
    }
    
    /**
     * Get cats in geographical area
     * GET /api/cats/area?lat=51.0504&lon=13.7373&radius=10
     */
    @GetMapping("/area")
    public ResponseEntity<List<CatSummaryDto>> getCatsInArea(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(defaultValue = "10.0") Double radius) {
        
        log.info("Fetching cats in area: lat={}, lon={}, radius={}", lat, lon, radius);
        List<Cat> cats = catService.getCatsInArea(lat, lon, radius);
        List<CatSummaryDto> catDtos = cats.stream()
            .map(this::toSummaryDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(catDtos);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CatSummaryDto>> getPendingCats() {
        log.info("Fetching pending cats");
        List<Cat> cats = catService.getPendingCats();
        List<CatSummaryDto> catDtos = cats.stream()
            .map(this::toSummaryDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(catDtos);
    }

    @GetMapping("/adopted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CatSummaryDto>> getAdoptedCats() {
        log.info("Fetching available cats");
        List<Cat> cats = catService.getAdoptedCats();
        List<CatSummaryDto> catDtos = cats.stream()
            .map(this::toSummaryDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(catDtos);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CatDto> getCatById(@PathVariable Long id) {
        log.info("Fetching cat with ID: {}", id);
        Optional<Cat> cat = catService.getCatById(id);

        Optional<CatDto> catDto = cat.map(this::toDto);
        
        return catDto.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CatSummaryDto>> getAllCats(@PageableDefault(size = 10) Pageable pageable) {
        log.info("Fetching paginated cats, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Cat> cats = catService.getAllCats(pageable);
        Page<CatSummaryDto> catDtos = cats.map(this::toSummaryDto);
        return ResponseEntity.ok(catDtos);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CatDto> createCat(@RequestBody Cat cat) {
        log.info("Creating new cat: {}", cat.getName());
        try {
            Cat savedCat = catService.saveCat(cat);
            CatDto catDto = toDto(savedCat);
            return ResponseEntity.ok(catDto);
        } catch (Exception e) {
            log.error("Error creating cat", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CatDto> updateCatStatus(@PathVariable Long id, 
                                               @RequestBody StatusUpdateRequestDto request) {
        log.info("Updating cat {} status to {}", id, request.getStatus());
        try {
            Cat updatedCat = catService.updateCatStatus(id, request.getStatus());
            CatDto catDto = toDto(updatedCat);
            return ResponseEntity.ok(catDto);
        } catch (Exception e) {
            log.error("Error updating cat status", e);
            return ResponseEntity.badRequest().build();
        }
    }

    //Map Entity to DTO
    public CatDto toDto(Cat cat) {
        if (cat == null) {
            return null;
        }
        
        return CatDto.builder()
            .id(cat.getId())
            .name(cat.getName())
            .age(cat.getAge())
            .description(cat.getDescription())
            .breedId(cat.getBreed() != null ? cat.getBreed().getId() : null)
            .breedName(cat.getBreed() != null ? cat.getBreed().getName() : null)
            .imageUrl(cat.getImageUrl())
            .latitude(cat.getLatitude())
            .longitude(cat.getLongitude())
            .address(cat.getAddress())
            .status(cat.getStatus())
            .createdAt(cat.getCreatedAt())
            .updatedAt(cat.getUpdatedAt())
            .build();
    }

    public CatSummaryDto toSummaryDto(Cat cat) {
        if (cat == null) {
            return null;
        }
        
        return CatSummaryDto.builder()
            .id(cat.getId())
            .name(cat.getName())
            .age(cat.getAge())
            .breedName(cat.getBreed() != null ? cat.getBreed().getName() : null)
            .imageUrl(cat.getImageUrl())
            .address(cat.getAddress())
            .status(cat.getStatus())
            .build();
    }
}
