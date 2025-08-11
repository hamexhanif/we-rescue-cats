package io.werescuecats.backend.repository;

import io.werescuecats.backend.entity.ApiToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiTokenRepository extends JpaRepository<ApiToken, String> {
    Optional<ApiToken> findByTokenAndActiveTrue(String token);
}