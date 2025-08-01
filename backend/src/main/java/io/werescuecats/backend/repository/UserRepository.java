package io.werescuecats.backend.repository;

import io.werescuecats.backend.entity.User;
import io.werescuecats.backend.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    List<User> findByRole(UserRole role);
    
    boolean existsByEmail(String email);
    
    List<User> findByEnabledTrue();
}
