package io.werescuecats.backend.repository;

import io.werescuecats.backend.entity.Adoption;
import io.werescuecats.backend.entity.AdoptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdoptionRepository extends JpaRepository<Adoption, Long> {
    
    List<Adoption> findByStatus(AdoptionStatus status);
    
    List<Adoption> findByUserId(Long userId);
    
    List<Adoption> findByCatId(Long catId);
    
    @Query("SELECT a FROM Adoption a WHERE a.status = :status AND " +
           "a.adoptionDate BETWEEN :startDate AND :endDate")
    List<Adoption> findAdoptionsByStatusAndDateRange(@Param("status") AdoptionStatus status,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(a) FROM Adoption a WHERE a.user.id = :userId AND a.status = 'COMPLETED'")
    long countCompletedAdoptionsByUser(@Param("userId") Long userId);
    
    List<Adoption> findByStatusOrderByAdoptionDateDesc(AdoptionStatus status);
}
