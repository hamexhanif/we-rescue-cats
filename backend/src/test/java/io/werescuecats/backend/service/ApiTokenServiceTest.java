package io.werescuecats.backend.service;

import io.werescuecats.backend.entity.ApiToken;
import io.werescuecats.backend.repository.ApiTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiTokenServiceTest {

    @Mock
    private ApiTokenRepository apiTokenRepository;

    @InjectMocks
    private ApiTokenService apiTokenService;

    private ApiToken validToken;
    private ApiToken expiredToken;
    private ApiToken inactiveToken;

    @BeforeEach
    void setUp() {
        validToken = new ApiToken("valid-token", "Test Organization");
        
        expiredToken = new ApiToken("expired-token", "Test Organization");
        expiredToken.setExpiresAt(LocalDateTime.now().minusDays(1));
        
        inactiveToken = new ApiToken("inactive-token", "Test Organization");
        inactiveToken.setActive(false);
    }

    @Test
    void isValidToken_ShouldReturnTrue_WhenTokenIsValid() {
        when(apiTokenRepository.findByTokenAndActiveTrue("valid-token"))
                .thenReturn(Optional.of(validToken));

        boolean result = apiTokenService.isValidToken("valid-token");

        assertTrue(result);
        verify(apiTokenRepository).findByTokenAndActiveTrue("valid-token");
    }

    @Test
    void isValidToken_ShouldReturnFalse_WhenTokenIsExpired() {
        when(apiTokenRepository.findByTokenAndActiveTrue("expired-token"))
                .thenReturn(Optional.of(expiredToken));

        boolean result = apiTokenService.isValidToken("expired-token");

        assertFalse(result);
        verify(apiTokenRepository).findByTokenAndActiveTrue("expired-token");
    }

    @Test
    void isValidToken_ShouldReturnFalse_WhenTokenNotFound() {
        when(apiTokenRepository.findByTokenAndActiveTrue("nonexistent-token"))
                .thenReturn(Optional.empty());

        boolean result = apiTokenService.isValidToken("nonexistent-token");

        assertFalse(result);
        verify(apiTokenRepository).findByTokenAndActiveTrue("nonexistent-token");
    }

    @Test
    void isValidToken_ShouldReturnFalse_WhenTokenIsNull() {
        boolean result = apiTokenService.isValidToken(null);

        assertFalse(result);
        verify(apiTokenRepository, never()).findByTokenAndActiveTrue(any());
    }

    @Test
    void isValidToken_ShouldReturnFalse_WhenTokenIsEmpty() {
        boolean result = apiTokenService.isValidToken("");

        assertFalse(result);
        verify(apiTokenRepository, never()).findByTokenAndActiveTrue(any());
    }

    @Test
    void generateToken_ShouldCreateAndSaveNewToken() {
        ApiToken savedToken = new ApiToken("health_12345", "Health Org");
        when(apiTokenRepository.save(any(ApiToken.class))).thenReturn(savedToken);

        ApiToken result = apiTokenService.generateToken("Health Org");

        assertNotNull(result);
        assertEquals("Health Org", result.getOrganizationName());
        assertTrue(result.getToken().startsWith("health_"));
        verify(apiTokenRepository).save(any(ApiToken.class));
    }

    @Test
    void initializeDefaultTokens_ShouldCreateTokens_WhenRepositoryIsEmpty() {
        when(apiTokenRepository.count()).thenReturn(0L);
        when(apiTokenRepository.save(any(ApiToken.class)))
                .thenReturn(new ApiToken("token1", "Default Health Institution"))
                .thenReturn(new ApiToken("token2", "Test Health Institution"));

        apiTokenService.initializeDefaultTokens();

        verify(apiTokenRepository).count();
        verify(apiTokenRepository, times(2)).save(any(ApiToken.class));
    }

    @Test
    void initializeDefaultTokens_ShouldNotCreateTokens_WhenRepositoryIsNotEmpty() {
        when(apiTokenRepository.count()).thenReturn(1L);

        apiTokenService.initializeDefaultTokens();

        verify(apiTokenRepository).count();
        verify(apiTokenRepository, never()).save(any(ApiToken.class));
    }
}