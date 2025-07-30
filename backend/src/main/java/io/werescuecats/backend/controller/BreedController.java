package io.werescuecats.backend.controller;

import io.werescuecats.backend.entity.Breed;
import io.werescuecats.backend.service.BreedService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/breeds")
@CrossOrigin(origins = "*")
@Slf4j
public class BreedController {
    
    @Autowired
    private BreedService breedService;
    
    @GetMapping
    public ResponseEntity<List<Breed>> getAllBreeds() {
        log.info("Fetching all breeds");
        List<Breed> breeds = breedService.getAllBreeds();
        return ResponseEntity.ok(breeds);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Breed> getBreedById(@PathVariable String id) {
        log.info("Fetching breed with ID: {}", id);
        Optional<Breed> breed = breedService.getBreedById(id);
        
        if (breed.isPresent()) {
            return ResponseEntity.ok(breed.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Search breeds with filters
     * GET /api/breeds/search?name={name}&childFriendly={val}&dogFriendly={val}&energyLevel={val}&origin={val}
     */
    @GetMapping("/search")
    public ResponseEntity<List<Breed>> searchBreeds(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer childFriendly,
            @RequestParam(required = false) Integer dogFriendly,
            @RequestParam(required = false) Integer energyLevel,
            @RequestParam(required = false) String origin) {
        
        log.info("Searching breeds with filters - name: {}, childFriendly: {}, dogFriendly: {}, energyLevel: {}, origin: {}", 
                name, childFriendly, dogFriendly, energyLevel, origin);
        
        List<Breed> breeds = breedService.searchBreeds(name, childFriendly, dogFriendly, energyLevel, origin);
        return ResponseEntity.ok(breeds);
    }
}
