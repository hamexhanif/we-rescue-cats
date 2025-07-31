package io.werescuecats.backend.service;

import io.werescuecats.backend.config.CatApiConfig;
import io.werescuecats.backend.dto.BreedDto;
import io.werescuecats.backend.entity.Breed;
import io.werescuecats.backend.repository.BreedRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BreedService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private CatApiConfig config;
    
    @Autowired
    private BreedRepository breedRepository;
    
    /**
     * Fetch all breeds from TheCatAPI and sync with local database
     */
    @Transactional
    public void fetchBreedsFromApi() {
        log.info("Starting to fetch breeds from TheCatAPI");


        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", config.getApiKey());
        headers.set("User-Agent", "WeRescueCats/1.0");
        
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<BreedDto[]> response = restTemplate.exchange(
                config.getBaseUrl() + "/breeds",
                HttpMethod.GET,
                entity,
                BreedDto[].class
            );
            
            List<BreedDto> apiBreeds = Arrays.asList(response.getBody());

            List<Breed> breeds = apiBreeds.stream()
                .map(apiBreed -> {
                    Breed breed = new Breed();
                    breed.setId(apiBreed.getId());  
                    mapApiBreedToEntity(apiBreed, breed);
                    return breed;
                })
                .collect(Collectors.toList());
            
            breedRepository.saveAll(breeds);
                
            log.info("Fetched {} breeds from TheCatAPI", apiBreeds.size());
            
        } catch (Exception e) {
            log.error("Failed to fetch breeds from TheCatAPI", e);
            throw new RuntimeException("API call failed", e);
        }
    }
    
    private void mapApiBreedToEntity(BreedDto apiBreed, Breed breed) {
        breed.setName(apiBreed.getName());
        breed.setDescription(apiBreed.getDescription());
        breed.setOrigin(apiBreed.getOrigin());
        breed.setAdaptability(apiBreed.getAdaptability());
        breed.setAffectionLevel(apiBreed.getAffectionLevel());
        breed.setChildFriendly(apiBreed.getChildFriendly());
        breed.setDogFriendly(apiBreed.getDogFriendly());
        breed.setEnergyLevel(apiBreed.getEnergyLevel());
        breed.setGrooming(apiBreed.getGrooming());
        breed.setHealthIssues(apiBreed.getHealthIssues());
        breed.setIntelligence(apiBreed.getIntelligence());
        breed.setSocialNeeds(apiBreed.getSocialNeeds());
        breed.setStrangerFriendly(apiBreed.getStrangerFriendly());
        breed.setWikipediaUrl(apiBreed.getWikipediaUrl());
        breed.setReferenceImageId(apiBreed.getReferenceImageId());
        
        if (apiBreed.getImage() != null) {
            breed.setImageUrl(apiBreed.getImage().getUrl());
        }
    }
    
    @Cacheable("breeds")
    public List<Breed> getAllBreeds() {
        log.debug("Fetching all breeds from database");
        return breedRepository.findAll();
    }
    
    @Cacheable("breed")
    public Optional<Breed> getBreedById(String id) {
        return breedRepository.findById(id);
    }
    
    public List<Breed> searchBreeds(String name, Integer childFriendly, 
                                   Integer dogFriendly, Integer energyLevel, String origin) {
        if (name != null && !name.trim().isEmpty()) {
            return breedRepository.findByNameContainingIgnoreCase(name.trim());
        }
        
        return breedRepository.findBreedsWithFilters(childFriendly, dogFriendly, energyLevel, origin);
    }
    
    /**
     * Fetch on application startup - only if database is empty
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (config.isFetchOnStartup()) {
            log.info("Application ready - checking if breeds data exists");
            
            long breedCount = breedRepository.count();
            if (breedCount == 0) {
                log.info("No breeds found in database, performing initial fetch");
                fetchBreedsFromApi();
            } else {
                log.info("Database contains {} breeds, no fetch needed", breedCount);
            }
        }
    }
}
