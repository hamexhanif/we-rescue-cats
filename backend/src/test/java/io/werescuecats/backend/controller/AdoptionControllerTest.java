package io.werescuecats.backend.controller;

import io.werescuecats.backend.dto.*;
import io.werescuecats.backend.entity.*;
import io.werescuecats.backend.security.CustomUserDetails;
import io.werescuecats.backend.service.AdoptionService;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdoptionControllerTest {

    @Mock
    private AdoptionService adoptionService;

    @InjectMocks
    private AdoptionController adoptionController;

    private Adoption adoption;
    private User user;
    private Cat cat;
    private Breed breed;
    private User admin;

    @BeforeEach
    void setUp() {
        breed = new Breed("persian", "Persian");
        
        cat = new Cat();
        cat.setId(1L);
        cat.setName("Fluffy");
        cat.setBreed(breed);
        cat.setStatus(CatStatus.AVAILABLE);
        
        user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(UserRole.USER);
        
        admin = new User();
        admin.setId(2L);
        admin.setEmail("admin@test.com");
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setRole(UserRole.ADMIN);
        
        adoption = new Adoption(user, cat);
        adoption.setId(1L);
        adoption.setStatus(AdoptionStatus.PENDING);
        adoption.setAdoptionDate(LocalDateTime.now());
        adoption.setTenantId("main");
    }

    @Test
    void createAdoption_ShouldReturnAdoptionDto() {
        AdoptionRequestDto request = new AdoptionRequestDto();
        request.setUserId(1L);
        request.setCatId(1L);
        request.setNotes("I love cats");

        when(adoptionService.createAdoption(1L, 1L, "I love cats")).thenReturn(adoption);

        ResponseEntity<AdoptionDto> response = adoptionController.createAdoption(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("PENDING", response.getBody().getStatus());
        verify(adoptionService).createAdoption(1L, 1L, "I love cats");
    }

    @Test
    void getPendingAdoptions_ShouldReturnPendingAdoptions() {
        List<Adoption> pendingAdoptions = Arrays.asList(adoption);
        when(adoptionService.getPendingAdoptions()).thenReturn(pendingAdoptions);

        ResponseEntity<List<AdoptionDto>> response = adoptionController.getPendingAdoptions();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(adoptionService).getPendingAdoptions();
    }

    @Test
    void getAdoptionsByUser_ShouldReturnUserAdoptions() {
        List<Adoption> userAdoptions = Arrays.asList(adoption);
        when(adoptionService.getAdoptionsByUser(1L)).thenReturn(userAdoptions);

        ResponseEntity<List<AdoptionDto>> response = adoptionController.getAdoptionsByUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(adoptionService).getAdoptionsByUser(1L);
    }

    @Test
    void getAllAdoptions_ShouldReturnAllAdoptions() {
        List<Adoption> allAdoptions = Arrays.asList(adoption);
        when(adoptionService.getAllAdoptions()).thenReturn(allAdoptions);

        ResponseEntity<List<AdoptionDto>> response = adoptionController.getAllAdoptions();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(adoptionService).getAllAdoptions();
    }

    @Test
    void getAdoptionById_ShouldReturnAdoption() {
        when(adoptionService.getAdoptionById(1L)).thenReturn(Optional.of(adoption));

        ResponseEntity<AdoptionDto> response = adoptionController.getAdoptionById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(adoptionService).getAdoptionById(1L);
    }

    @Test
    void getAdoptionById_NotFound() {
        when(adoptionService.getAdoptionById(999L)).thenReturn(Optional.empty());

        ResponseEntity<AdoptionDto> response = adoptionController.getAdoptionById(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(adoptionService).getAdoptionById(999L);
    }

    @Test
    void approveAdoption_ShouldReturnApprovedAdoption() {
        adoption.setStatus(AdoptionStatus.APPROVED);
        CustomUserDetails userDetails = new CustomUserDetails(admin);
        when(adoptionService.approveAdoption(eq(1L), any(User.class))).thenReturn(adoption);

        ResponseEntity<AdoptionDto> response = adoptionController.approveAdoption(1L, userDetails);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("APPROVED", response.getBody().getStatus());
        verify(adoptionService).approveAdoption(eq(1L), any(User.class));
    }

    @Test
    void approveAdoption_ShouldReturnBadRequestOnException() {
        CustomUserDetails userDetails = new CustomUserDetails(admin);
        when(adoptionService.approveAdoption(eq(1L), any(User.class))).thenThrow(new RuntimeException("Error"));

        ResponseEntity<AdoptionDto> response = adoptionController.approveAdoption(1L, userDetails);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(adoptionService).approveAdoption(eq(1L), any(User.class));
    }

    @Test
    void completeAdoption_ShouldReturnCompletedAdoption() {
        adoption.setStatus(AdoptionStatus.COMPLETED);
        CustomUserDetails userDetails = new CustomUserDetails(admin);
        when(adoptionService.completeAdoption(eq(1L), any(User.class))).thenReturn(adoption);

        ResponseEntity<AdoptionDto> response = adoptionController.completeAdoption(1L, userDetails);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("COMPLETED", response.getBody().getStatus());
        verify(adoptionService).completeAdoption(eq(1L), any(User.class));
    }

    @Test
    void completeAdoption_ShouldReturnBadRequestOnException() {
        CustomUserDetails userDetails = new CustomUserDetails(admin);
        when(adoptionService.completeAdoption(eq(1L), any(User.class))).thenThrow(new RuntimeException("Error"));

        ResponseEntity<AdoptionDto> response = adoptionController.completeAdoption(1L, userDetails);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(adoptionService).completeAdoption(eq(1L), any(User.class));
    }

    @Test
    void rejectAdoption_ShouldReturnRejectedAdoption() {
        adoption.setStatus(AdoptionStatus.REJECTED);
        RejectAdoptionRequestDto request = new RejectAdoptionRequestDto();
        request.setReason("Not suitable");
        CustomUserDetails userDetails = new CustomUserDetails(admin);
        when(adoptionService.rejectAdoption(eq(1L), any(User.class), eq("Not suitable"))).thenReturn(adoption);

        ResponseEntity<AdoptionDto> response = adoptionController.rejectAdoption(1L, userDetails, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("REJECTED", response.getBody().getStatus());
        verify(adoptionService).rejectAdoption(eq(1L), any(User.class), eq("Not suitable"));
    }

    @Test
    void rejectAdoption_ShouldReturnBadRequestOnException() {
        RejectAdoptionRequestDto request = new RejectAdoptionRequestDto();
        request.setReason("Not suitable");
        CustomUserDetails userDetails = new CustomUserDetails(admin);
        when(adoptionService.rejectAdoption(eq(1L), any(User.class), anyString())).thenThrow(new RuntimeException("Error"));

        ResponseEntity<AdoptionDto> response = adoptionController.rejectAdoption(1L, userDetails, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(adoptionService).rejectAdoption(eq(1L), any(User.class), anyString());
    }

    @Test
    void getUserAdoptionStats_ShouldReturnStats() {
        List<Adoption> userAdoptions = Arrays.asList(adoption);
        when(adoptionService.getUserAdoptionCount(1L)).thenReturn(1L);
        when(adoptionService.getAdoptionsByUser(1L)).thenReturn(userAdoptions);

        ResponseEntity<AdoptionStatsDto> response = adoptionController.getUserAdoptionStats(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getUserId());
        assertEquals(1, response.getBody().getTotalApplications());
        assertEquals(1, response.getBody().getPendingApplications());
        verify(adoptionService).getUserAdoptionCount(1L);
        verify(adoptionService).getAdoptionsByUser(1L);
    }
}
