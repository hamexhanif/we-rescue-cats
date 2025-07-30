package io.werescuecats.backend.repository;

import io.werescuecats.backend.entity.Breed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BreedRepository extends JpaRepository<Breed, String> {
    
    Optional<Breed> findByName(String name);
    
    @Query("SELECT b FROM Breed b WHERE " +
           "(:childFriendly IS NULL OR b.childFriendly >= :childFriendly) AND " +
           "(:dogFriendly IS NULL OR b.dogFriendly >= :dogFriendly) AND " +
           "(:energyLevel IS NULL OR b.energyLevel = :energyLevel) AND " +
           "(:origin IS NULL OR LOWER(b.origin) LIKE LOWER(CONCAT('%', :origin, '%')))")
    List<Breed> findBreedsWithFilters(@Param("childFriendly") Integer childFriendly,
                                      @Param("dogFriendly") Integer dogFriendly,
                                      @Param("energyLevel") Integer energyLevel,
                                      @Param("origin") String origin);
    
    List<Breed> findByNameContainingIgnoreCase(String name);
}
