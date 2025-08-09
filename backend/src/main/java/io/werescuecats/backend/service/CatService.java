package io.werescuecats.backend.service;

import io.werescuecats.backend.config.CatApiConfig;
import io.werescuecats.backend.entity.Cat;
import io.werescuecats.backend.entity.CatStatus;
import io.werescuecats.backend.repository.CatRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class CatService {
    
    private final CatRepository catRepository;

    private final CatApiConfig config;

    private final RestTemplate restTemplate;
    
    @Cacheable("availableCats")
    public List<Cat> getAvailableCats() {
        log.debug("Fetching available cats");
        return catRepository.findByStatus(CatStatus.AVAILABLE);
    }

    public List<Cat> getPendingCats() {
        log.debug("Fetching available cats");
        return catRepository.findByStatus(CatStatus.PENDING);
    }

    public List<Cat> getAdoptedCats() {
        log.debug("Fetching available cats");
        return catRepository.findByStatus(CatStatus.ADOPTED);
    }
    
    public Optional<Cat> getCatById(Long id) {
        return catRepository.findById(id);
    }
    
    public List<Cat> getCatsByBreed(String breedId) {
        return catRepository.findByBreedId(breedId);
    }
    
    public List<Cat> getCatsInArea(Double latitude, Double longitude, Double radiusKm) {
        // Simple bounding box calculation (rough approximation)
        double latDelta = radiusKm / 111.0; // ~111km per degree latitude
        double lonDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(latitude)));
        
        return catRepository.findCatsInArea(
            latitude - latDelta,
            latitude + latDelta,
            longitude - lonDelta,
            longitude + lonDelta,
            CatStatus.AVAILABLE
        );
    }
    
    @Transactional
    public Cat saveCat(Cat cat) {
        log.info("Saving cat: {}", cat.getName());
        cat.setImageUrl(fetchImageUrlForSpecificBreed(cat.getBreed().getId()));
        return catRepository.save(cat);
    }
    
    @Transactional
    public Cat updateCatStatus(Long catId, CatStatus status) {
        Optional<Cat> catOpt = catRepository.findById(catId);
        if (catOpt.isPresent()) {
            Cat cat = catOpt.get();
            cat.setStatus(status);
            return catRepository.save(cat);
        }
        throw new RuntimeException("Cat not found with id: " + catId);
    }

    public List<Cat> getAllCats() {
        return catRepository.findAll();
    }

    @Transactional
    public String fetchImageUrlForSpecificBreed(String breedId) {
        log.info("Fetching image URL for breed: {}", breedId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", config.getApiKey());
        headers.set("User-Agent", "WeRescueCats/1.0");
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            String url = config.getBaseUrl() + "/images/search?limit=1&breed_ids=" + breedId;

            ResponseEntity<Map[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map[].class);

            Map[] images = response.getBody();

            if (images != null && images.length > 0) {
                String imageUrl = (String) images[0].get("url");
                log.info("Found image URL: {}", imageUrl);
                return imageUrl;
            }

            log.warn("No images found for breed: {}", breedId);
            return null;

        } catch (Exception e) {
            log.error("Failed to fetch image URL for breed: {}", breedId, e);
            throw new RuntimeException("API call failed", e);
        }
    }
}
