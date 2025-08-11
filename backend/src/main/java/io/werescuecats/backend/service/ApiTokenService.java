package io.werescuecats.backend.service;

import io.werescuecats.backend.entity.ApiToken;
import io.werescuecats.backend.repository.ApiTokenRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiTokenService {

    private final ApiTokenRepository apiTokenRepository;

    public boolean isValidToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        Optional<ApiToken> apiToken = apiTokenRepository.findByTokenAndActiveTrue(token);
        return apiToken.isPresent() && apiToken.get().isValid();
    }

    public ApiToken generateToken(String organizationName) {
        String token = "health_" + UUID.randomUUID().toString().replace("-", "");
        ApiToken apiToken = new ApiToken(token, organizationName);
        return apiTokenRepository.save(apiToken);
    }

    @PostConstruct
    public void initializeDefaultTokens() {
        if (apiTokenRepository.count() == 0) {
            log.info("Initializing default API tokens...");
            generateToken("Default Health Institution");
            generateToken("Test Health Institution");
        }
    }
}