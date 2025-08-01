package io.werescuecats.backend.repository;

import io.werescuecats.backend.entity.Cat;
import io.werescuecats.backend.entity.CatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatRepository extends JpaRepository<Cat, Long> {
    
    List<Cat> findByStatus(CatStatus status);
    
    List<Cat> findByBreedId(String breedId);
    
    @Query("SELECT c FROM Cat c WHERE c.status = :status AND " +
           "(:breedId IS NULL OR c.breed.id = :breedId)")
    List<Cat> findAvailableCatsWithBreed(@Param("status") CatStatus status, 
                                         @Param("breedId") String breedId);
    
    @Query("SELECT c FROM Cat c WHERE " +
           "c.latitude BETWEEN :minLat AND :maxLat AND " +
           "c.longitude BETWEEN :minLon AND :maxLon AND " +
           "c.status = :status")
    List<Cat> findCatsInArea(@Param("minLat") Double minLat, 
                             @Param("maxLat") Double maxLat,
                             @Param("minLon") Double minLon, 
                             @Param("maxLon") Double maxLon,
                             @Param("status") CatStatus status);
}
