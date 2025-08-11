package io.werescuecats.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.werescuecats.backend.dto.AnonymousAdoptionData;
import io.werescuecats.backend.entity.Adoption;
import io.werescuecats.backend.entity.AdoptionStatus;
import io.werescuecats.backend.entity.Cat;
import io.werescuecats.backend.entity.CatStatus;
import io.werescuecats.backend.entity.User;
import io.werescuecats.backend.exception.CatNotAvailableException;
import io.werescuecats.backend.exception.ResourceNotFoundException;
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
        testUser.setStreetAddress("Street, City, Region");

        testCat = new Cat();
        testCat.setId(1L);
        testCat.setName("Fluffy");
        testCat.setStatus(CatStatus.AVAILABLE);
        testCat.setBreed(null);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@example.com");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");

        testAdoption = new Adoption(testUser, testCat, "Test notes");
        testAdoption.setId(1L);
        testAdoption.setStatus(AdoptionStatus.PENDING);
    }

    @Test
    @DisplayName("Create adoption success")
    void createAdoption_Success() {
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(catService.getCatById(1L)).thenReturn(Optional.of(testCat));
        when(adoptionRepository.save(any(Adoption.class))).thenAnswer(i -> i.getArgument(0));

        Adoption result = adoptionService.createAdoption(1L, 1L, "Notes");

        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(testCat, result.getCat());
        assertEquals("Notes", result.getNotes());
        assertEquals(AdoptionStatus.PENDING, result.getStatus());

        verify(catService).updateCatStatus(1L, CatStatus.PENDING);
        verify(adoptionRepository).save(any(Adoption.class));
    }

    @Test
    @DisplayName("Create adoption throws when user not found")
    void createAdoption_UserNotFound() {
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> adoptionService.createAdoption(1L, 1L, "Notes"));

        assertTrue(ex.getMessage().contains("User not found with id: 1"));
        verify(adoptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create adoption throws when cat not found")
    void createAdoption_CatNotFound() {
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(catService.getCatById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> adoptionService.createAdoption(1L, 1L, "Notes"));

        assertTrue(ex.getMessage().contains("Cat not found with id: 1"));
        verify(adoptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create adoption throws when cat not available")
    void createAdoption_CatNotAvailable() {
        testCat.setStatus(CatStatus.ADOPTED);
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(catService.getCatById(1L)).thenReturn(Optional.of(testCat));

        CatNotAvailableException ex = assertThrows(CatNotAvailableException.class,
                () -> adoptionService.createAdoption(1L, 1L, "Notes"));

        assertTrue(ex.getMessage().contains("Cat is not available for adoption: Fluffy"));
        verify(adoptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Approve adoption success")
    void approveAdoption_Success() {
        testAdoption.setStatus(AdoptionStatus.PENDING);
        when(adoptionRepository.findById(1L)).thenReturn(Optional.of(testAdoption));
        when(adoptionRepository.save(any(Adoption.class))).thenAnswer(i -> i.getArgument(0));

        Adoption result = adoptionService.approveAdoption(1L, adminUser);

        assertEquals(AdoptionStatus.APPROVED, result.getStatus());
        assertEquals(adminUser, result.getProcessedByAdmin());
        assertNotNull(result.getApprovedDate());

        verify(catService).updateCatStatus(testCat.getId(), CatStatus.PENDING);
        verify(adoptionRepository).save(testAdoption);
    }

    @Test
    @DisplayName("Approve adoption fails on wrong status")
    void approveAdoption_WrongStatus() {
        testAdoption.setStatus(AdoptionStatus.COMPLETED);
        when(adoptionRepository.findById(1L)).thenReturn(Optional.of(testAdoption));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> adoptionService.approveAdoption(1L, adminUser));

        assertTrue(ex.getMessage().contains("Adoption must be PENDING to be transitioned to APPROVED"));
    }

    @Test
    @DisplayName("Complete adoption success")
    void completeAdoption_Success() {
        testAdoption.setStatus(AdoptionStatus.APPROVED);
        when(adoptionRepository.findById(1L)).thenReturn(Optional.of(testAdoption));
        when(adoptionRepository.save(any(Adoption.class))).thenAnswer(i -> i.getArgument(0));

        Adoption result = adoptionService.completeAdoption(1L, adminUser);

        assertEquals(AdoptionStatus.COMPLETED, result.getStatus());
        assertEquals(adminUser, result.getProcessedByAdmin());
        assertNotNull(result.getCompletedDate());

        verify(catService).updateCatStatus(testCat.getId(), CatStatus.ADOPTED);
        verify(adoptionRepository).save(testAdoption);
    }

    @Test
    @DisplayName("Reject adoption success")
    void rejectAdoption_Success() {
        testAdoption.setStatus(AdoptionStatus.PENDING);
        when(adoptionRepository.findById(1L)).thenReturn(Optional.of(testAdoption));
        when(adoptionRepository.save(any(Adoption.class))).thenAnswer(i -> i.getArgument(0));

        Adoption result = adoptionService.rejectAdoption(1L, adminUser, "Reason");

        assertEquals(AdoptionStatus.REJECTED, result.getStatus());
        assertEquals(adminUser, result.getProcessedByAdmin());
        assertEquals("Reason", result.getAdminNotes());

        verify(catService).updateCatStatus(testCat.getId(), CatStatus.AVAILABLE);
        verify(adoptionRepository).save(testAdoption);
    }

    @Test
    @DisplayName("Get pending adoptions returns list")
    void getPendingAdoptions_Success() {
        List<Adoption> list = List.of(testAdoption);
        when(adoptionRepository.findByStatus(AdoptionStatus.PENDING)).thenReturn(list);

        List<Adoption> result = adoptionService.getPendingAdoptions();

        assertEquals(1, result.size());
        assertEquals(testAdoption, result.get(0));
    }

    @Test
    @DisplayName("Get adoptions by user returns list")
    void getAdoptionsByUser_Success() {
        List<Adoption> list = List.of(testAdoption);
        when(adoptionRepository.findByUserId(1L)).thenReturn(list);

        List<Adoption> result = adoptionService.getAdoptionsByUser(1L);

        assertEquals(1, result.size());
        assertEquals(testAdoption, result.get(0));
    }

    @Test
    @DisplayName("Get all adoptions returns list")
    void getAllAdoptions_Success() {
        List<Adoption> list = List.of(testAdoption);
        when(adoptionRepository.findAll()).thenReturn(list);

        List<Adoption> result = adoptionService.getAllAdoptions();

        assertEquals(1, result.size());
        assertEquals(testAdoption, result.get(0));
    }

    @Test
    @DisplayName("Get adoption by id returns adoption")
    void getAdoptionById_Success() {
        when(adoptionRepository.findById(1L)).thenReturn(Optional.of(testAdoption));

        Optional<Adoption> result = adoptionService.getAdoptionById(1L);

        assertTrue(result.isPresent());
        assertEquals(testAdoption, result.get());
    }

    @Test
    @DisplayName("Get user adoption count returns count")
    void getUserAdoptionCount_Success() {
        when(adoptionRepository.countCompletedAdoptionsByUser(1L)).thenReturn(5L);

        long count = adoptionService.getUserAdoptionCount(1L);

        assertEquals(5L, count);
    }

    @Test
    @DisplayName("Get anonymous adoption data maps correctly")
    void getAnonymousAdoptionData_Success() {
        testAdoption.setStatus(AdoptionStatus.COMPLETED);
        testAdoption.setAdoptionDate(LocalDateTime.of(2023, 1, 1, 12, 0));
        when(adoptionRepository.findAll()).thenReturn(List.of(testAdoption));

        List<AnonymousAdoptionData> result = adoptionService.getAnonymousAdoptionData();

        assertEquals(1, result.size());
        AnonymousAdoptionData data = result.get(0);
        assertEquals(testAdoption.getAdoptionDate(), data.getAdoptionDate());
        assertEquals("Unknown", data.getCatBreed());
        assertEquals(testCat.getAge(), data.getCatAge());
        assertEquals("Region", data.getLocationRegion());
        assertEquals("COMPLETED", data.getStatus());
        assertEquals(testAdoption.getTenantId(), data.getTenantId());
    }
}
