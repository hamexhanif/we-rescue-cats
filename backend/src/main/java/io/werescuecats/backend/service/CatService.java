package io.werescuecats.backend.service;

import io.werescuecats.backend.entity.Cat;
import io.werescuecats.backend.entity.CatStatus;
import io.werescuecats.backend.repository.CatRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CatService {
    
    @Autowired
    private CatRepository catRepository;
    
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
    
    public Page<Cat> getAllCats(Pageable pageable) {
        return catRepository.findAll(pageable);
    }

    public List<Cat> getAllCats() {
        return catRepository.findAll();
    }
}
