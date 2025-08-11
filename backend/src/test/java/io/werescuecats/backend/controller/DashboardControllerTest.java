package io.werescuecats.backend.controller;

import io.werescuecats.backend.dto.DashboardStatsDto;
import io.werescuecats.backend.entity.*;
import io.werescuecats.backend.service.AdoptionService;
import io.werescuecats.backend.service.CatService;
import io.werescuecats.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private CatService catService;

    @Mock
    private UserService userService;

    @Mock
    private AdoptionService adoptionService;

    @InjectMocks
    private DashboardController dashboardController;

    private List<Cat> cats;
    private List<User> users;
    private List<Adoption> adoptions;

    @BeforeEach
    void setUp() {
        Cat availableCat = new Cat();
        availableCat.setStatus(CatStatus.AVAILABLE);
        
        Cat adoptedCat = new Cat();
        adoptedCat.setStatus(CatStatus.ADOPTED);
        
        cats = Arrays.asList(availableCat, adoptedCat);

        User regularUser = new User();
        regularUser.setRole(UserRole.USER);
        
        User adminUser = new User();
        adminUser.setRole(UserRole.ADMIN);
        
        users = Arrays.asList(regularUser, adminUser);

        Adoption pendingAdoption = new Adoption();
        pendingAdoption.setStatus(AdoptionStatus.PENDING);
        
        Adoption completedAdoption = new Adoption();
        completedAdoption.setStatus(AdoptionStatus.COMPLETED);
        
        adoptions = Arrays.asList(pendingAdoption, completedAdoption);
    }

    @Test
    void getDashboardStats_ShouldReturnCorrectStatistics() {

        when(catService.getAllCats()).thenReturn(cats);
        when(catService.getAvailableCats()).thenReturn(Arrays.asList(cats.get(0)));
        when(userService.getAllUsers()).thenReturn(users);
        when(userService.getAdminUsers()).thenReturn(Arrays.asList(users.get(1)));
        when(adoptionService.getAllAdoptions()).thenReturn(adoptions);

        ResponseEntity<DashboardStatsDto> response = dashboardController.getDashboardStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        DashboardStatsDto stats = response.getBody();
        assertEquals(2, stats.getTotalCats());
        assertEquals(1, stats.getAvailableCats());
        assertEquals(1, stats.getAdoptedCats());
        assertEquals(2, stats.getTotalUsers());
        assertEquals(1, stats.getAdminUsers());
        assertEquals(2, stats.getTotalAdoptions());
        assertEquals(1, stats.getPendingAdoptions());
        assertEquals(1, stats.getCompletedAdoptions());

        verify(catService, times(2)).getAllCats();
        verify(catService).getAvailableCats();
        verify(userService, times(1)).getAllUsers();
        verify(userService).getAdminUsers();
        verify(adoptionService, times(3)).getAllAdoptions();
    }
}