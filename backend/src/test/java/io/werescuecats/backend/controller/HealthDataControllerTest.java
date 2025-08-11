package io.werescuecats.backend.controller;

import io.werescuecats.backend.dto.AnonymousAdoptionData;
import io.werescuecats.backend.service.AdoptionService;
import io.werescuecats.backend.service.ApiTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthDataControllerTest {

    @Mock
    private AdoptionService adoptionService;

    @Mock
    private ApiTokenService apiTokenService;

    @InjectMocks
    private HealthDataController healthDataController;

    private AnonymousAdoptionData adoptionData;

    @BeforeEach
    void setUp() {
        adoptionData = AnonymousAdoptionData.builder()
                .adoptionDate(LocalDateTime.now())
                .catBreed("Persian")
                .catAge(2)
                .locationRegion("NY")
                .status("COMPLETED")
                .tenantId("main")
                .build();
    }

    @Test
    void getAnonymousData_ShouldReturnData_WhenValidToken() {
        String validToken = "valid-token";
        List<AnonymousAdoptionData> data = Arrays.asList(adoptionData);
        when(apiTokenService.isValidToken(validToken)).thenReturn(true);
        when(adoptionService.getAnonymousAdoptionData()).thenReturn(data);

        ResponseEntity<List<AnonymousAdoptionData>> response = 
                healthDataController.getAnonymousData(validToken);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Persian", response.getBody().get(0).getCatBreed());
        verify(apiTokenService).isValidToken(validToken);
        verify(adoptionService).getAnonymousAdoptionData();
    }

    @Test
    void getAnonymousData_ShouldReturnUnauthorized_WhenInvalidToken() {
        String invalidToken = "invalid-token";
        when(apiTokenService.isValidToken(invalidToken)).thenReturn(false);

        ResponseEntity<List<AnonymousAdoptionData>> response = 
                healthDataController.getAnonymousData(invalidToken);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}