package io.werescuecats.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.werescuecats.backend.entity.Adoption;
import io.werescuecats.backend.entity.AdoptionStatus;
import io.werescuecats.backend.entity.Cat;
import io.werescuecats.backend.entity.CatStatus;
import io.werescuecats.backend.entity.User;
import io.werescuecats.backend.repository.AdoptionRepository;

@ExtendWith(MockitoExtension.class)
class AdoptionServiceTest {

    @Mock
    private AdoptionRepository adoptionRepository;
    
    @Mock
    private CatService catService;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private AdoptionService adoptionService;
    
    private User testUser;
    private Cat testCat;
    private User adminUser;
    private Adoption testAdoption;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        testCat = new Cat();
        testCat.setId(1L);
        testCat.setName("Fluffy");
        testCat.setStatus(CatStatus.AVAILABLE);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@example.com");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");

        testAdoption = new Adoption(testUser, testCat, "Test notes");
        testAdoption.setId(1L);
    }

    @Test
    @DisplayName("Should create adoption successfully when user and cat exist and cat is available")
    void createAdoption_Success() {
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(catService.getCatById(1L)).thenReturn(Optional.of(testCat));
        when(adoptionRepository.save(any(Adoption.class))).thenReturn(testAdoption);

        Adoption result = adoptionService.createAdoption(1L, 1L, "Test notes");

        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(testCat, result.getCat());
        assertEquals("Test notes", result.getNotes());
        assertEquals(AdoptionStatus.PENDING, result.getStatus());

        verify(adoptionRepository).save(any(Adoption.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void createAdoption_UserNotFound() {
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> adoptionService.createAdoption(1L, 1L, "Test notes"));
        
        assertTrue(exception.getMessage().contains("User not found with id: 1"));
        verify(adoptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when cat not found")
    void createAdoption_CatNotFound() {
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(catService.getCatById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> adoptionService.createAdoption(1L, 1L, "Test notes"));
        
        assertTrue(exception.getMessage().contains("Cat not found with id: 1"));
        verify(adoptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when cat is not available")
    void createAdoption_CatNotAvailable() {
        testCat.setStatus(CatStatus.ADOPTED);
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(catService.getCatById(1L)).thenReturn(Optional.of(testCat));

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> adoptionService.createAdoption(1L, 1L, "Test notes"));
        
        assertTrue(exception.getMessage().contains("Cat is not available for adoption: Fluffy"));
        verify(adoptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should approve adoption successfully")
    void approveAdoption_Success() {
        testAdoption.setStatus(AdoptionStatus.PENDING);
        when(adoptionRepository.findById(1L)).thenReturn(Optional.of(testAdoption));
        when(adoptionRepository.save(any(Adoption.class))).thenReturn(testAdoption);

        Adoption result = adoptionService.approveAdoption(1L, adminUser);

        assertEquals(AdoptionStatus.APPROVED, result.getStatus());
        assertEquals(adminUser, result.getProcessedByAdmin());
        assertNotNull(result.getApprovedDate());
        
        verify(catService).updateCatStatus(testCat.getId(), CatStatus.PENDING);
        verify(adoptionRepository).save(testAdoption);
    }

    @Test
    @DisplayName("Should throw exception when trying to approve non-pending adoption")
    void approveAdoption_InvalidStatus() {
        // Given
        testAdoption.setStatus(AdoptionStatus.COMPLETED);
        when(adoptionRepository.findById(1L)).thenReturn(Optional.of(testAdoption));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> adoptionService.approveAdoption(1L, adminUser));
        
        assertTrue(exception.getMessage().contains("Adoption must be PENDING to be transitioned to APPROVED"));
    }

    @Test
    @DisplayName("Should complete adoption successfully")
    void completeAdoption_Success() {
        testAdoption.setStatus(AdoptionStatus.APPROVED);
        when(adoptionRepository.findById(1L)).thenReturn(Optional.of(testAdoption));
        when(adoptionRepository.save(any(Adoption.class))).thenReturn(testAdoption);

        Adoption result = adoptionService.completeAdoption(1L, adminUser);

        assertEquals(AdoptionStatus.COMPLETED, result.getStatus());
        assertEquals(adminUser, result.getProcessedByAdmin());
        assertNotNull(result.getCompletedDate());
        
        verify(catService).updateCatStatus(testCat.getId(), CatStatus.ADOPTED);
        verify(adoptionRepository).save(testAdoption);
    }

    @Test
    @DisplayName("Should reject adoption successfully")
    void rejectAdoption_Success() {
        testAdoption.setStatus(AdoptionStatus.PENDING);
        when(adoptionRepository.findById(1L)).thenReturn(Optional.of(testAdoption));
        when(adoptionRepository.save(any(Adoption.class))).thenReturn(testAdoption);

        Adoption result = adoptionService.rejectAdoption(1L, adminUser, "Insufficient experience");

        assertEquals(AdoptionStatus.REJECTED, result.getStatus());
        assertEquals(adminUser, result.getProcessedByAdmin());
        assertEquals("Insufficient experience", result.getAdminNotes());
        
        verify(adoptionRepository).save(testAdoption);
    }

    @Test
    @DisplayName("Should get pending adoptions")
    void getPendingAdoptions_Success() {
        List<Adoption> pendingAdoptions = Arrays.asList(testAdoption);
        when(adoptionRepository.findByStatus(AdoptionStatus.PENDING)).thenReturn(pendingAdoptions);

        List<Adoption> result = adoptionService.getPendingAdoptions();

        assertEquals(1, result.size());
        assertEquals(testAdoption, result.get(0));
    }

    @Test
    @DisplayName("Should get adoptions by user")
    void getAdoptionsByUser_Success() {
        List<Adoption> userAdoptions = Arrays.asList(testAdoption);
        when(adoptionRepository.findByUserId(1L)).thenReturn(userAdoptions);

        List<Adoption> result = adoptionService.getAdoptionsByUser(1L);

        assertEquals(1, result.size());
        assertEquals(testAdoption, result.get(0));
    }

    @Test
    @DisplayName("Should get user adoption count")
    void getUserAdoptionCount_Success() {
        when(adoptionRepository.countCompletedAdoptionsByUser(1L)).thenReturn(3L);

        long result = adoptionService.getUserAdoptionCount(1L);

        assertEquals(3L, result);
    }

    @ParameterizedTest
    @DisplayName("Should validate null parameters")
    @ValueSource(strings = {"userId", "catId", "adoptionId", "admin"})
    void validateNullParameters(String paramType) {
        switch (paramType) {
            case "userId":
                assertThrows(Exception.class, () -> adoptionService.createAdoption(null, 1L, "notes"));
                break;
            case "catId":
                assertThrows(Exception.class, () -> adoptionService.createAdoption(1L, null, "notes"));
                break;
            case "adoptionId":
                assertThrows(Exception.class, () -> adoptionService.approveAdoption(null, adminUser));
                break;
            case "admin":
                assertThrows(Exception.class, () -> adoptionService.approveAdoption(1L, null));
                break;
        }
    }
}

