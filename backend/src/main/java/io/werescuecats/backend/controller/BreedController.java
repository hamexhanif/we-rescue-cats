package io.werescuecats.backend.controller;

import io.werescuecats.backend.dto.BreedDto;
import io.werescuecats.backend.entity.Breed;
import io.werescuecats.backend.service.BreedService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/breeds")
@CrossOrigin(origins = "*")
@Slf4j
public class BreedController {
    
    private final BreedService breedService;

    public BreedController(BreedService breedService) {
        this.breedService = breedService;
    }
    
    @GetMapping
    public ResponseEntity<Page<BreedDto>> getAllBreeds(@PageableDefault(size = 10) Pageable pageable) {
        log.info("Fetching paginated breeds, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Breed> breedPage = breedService.getAllBreeds(pageable);
        Page<BreedDto> dtoPage = breedPage.map(this::toDto);
        return ResponseEntity.ok(dtoPage);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BreedDto> getBreedById(@PathVariable String id) {
        log.info("Fetching breed with ID: {}", id);
        Optional<Breed> breed = breedService.getBreedById(id);
        
        if (breed.isPresent()) {
            return ResponseEntity.ok(toDto(breed.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Search breeds with filters
     * GET /api/breeds/search?name={name}&childFriendly={val}&dogFriendly={val}&energyLevel={val}&origin={val}
     */
    @GetMapping("/search")
    public ResponseEntity<List<BreedDto>> searchBreeds(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer childFriendly,
            @RequestParam(required = false) Integer dogFriendly,
            @RequestParam(required = false) Integer energyLevel,
            @RequestParam(required = false) String origin) {
        
        log.info("Searching breeds with filters - name: {}, childFriendly: {}, dogFriendly: {}, energyLevel: {}, origin: {}", 
                name, childFriendly, dogFriendly, energyLevel, origin);
        
        List<Breed> breeds = breedService.searchBreeds(name, childFriendly, dogFriendly, energyLevel, origin);
        List<BreedDto> dtos = breeds.stream()
                                    .map(this::toDto)
                                    .toList();
        return ResponseEntity.ok(dtos);
    }

    //Map Entity to DTO
    private BreedDto toDto(Breed breed) {
    BreedDto dto = new BreedDto();
    dto.setId(breed.getId());
    dto.setName(breed.getName());
    dto.setDescription(breed.getDescription());
    dto.setOrigin(breed.getOrigin());
    dto.setChildFriendly(breed.getChildFriendly());
    dto.setDogFriendly(breed.getDogFriendly());
    dto.setEnergyLevel(breed.getEnergyLevel());
    dto.setGrooming(breed.getGrooming());
    dto.setHealthIssues(breed.getHealthIssues());
    dto.setIntelligence(breed.getIntelligence());
    dto.setSocialNeeds(breed.getSocialNeeds());
    dto.setStrangerFriendly(breed.getStrangerFriendly());
    dto.setAdaptability(breed.getAdaptability());
    dto.setAffectionLevel(breed.getAffectionLevel());
    dto.setWikipediaUrl(breed.getWikipediaUrl());
    dto.setReferenceImageId(breed.getReferenceImageId());

    // map image if present
    if (breed.getImageUrl() != null) {
        dto.setImageUrl(breed.getImageUrl());
    }

    return dto;
    }
}
