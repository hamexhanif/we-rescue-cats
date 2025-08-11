package io.werescuecats.backend.controller;

import io.werescuecats.backend.dto.BreedDto;
import io.werescuecats.backend.entity.Breed;
import io.werescuecats.backend.service.BreedService;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BreedControllerTest {

    @Mock
    private BreedService breedService;

    @InjectMocks
    private BreedController breedController;

    private Breed breed;

    @BeforeEach
    void setUp() {
        breed = new Breed("persian", "Persian");
        breed.setDescription("A lovely long-haired breed");
        breed.setOrigin("Iran");
        breed.setChildFriendly(4);
        breed.setDogFriendly(3);
        breed.setEnergyLevel(2);
    }

    @Test
    void getAllBreeds_ShouldReturnBreedList() {
        List<Breed> breeds = Arrays.asList(breed);
        when(breedService.getAllBreeds()).thenReturn(breeds);

        ResponseEntity<List<BreedDto>> response = breedController.getAllBreeds();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Persian", response.getBody().get(0).getName());
        verify(breedService).getAllBreeds();
    }

    @Test
    void getBreedById_ShouldReturnBreed_WhenExists() {
        when(breedService.getBreedById("persian")).thenReturn(Optional.of(breed));

        ResponseEntity<BreedDto> response = breedController.getBreedById("persian");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Persian", response.getBody().getName());
        verify(breedService).getBreedById("persian");
    }

    @Test
    void getBreedById_ShouldReturnNotFound_WhenDoesNotExist() {
        
        when(breedService.getBreedById("nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<BreedDto> response = breedController.getBreedById("nonexistent");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(breedService).getBreedById("nonexistent");
    }

    @Test
    void searchBreeds_ShouldReturnFilteredBreeds() {
        List<Breed> breeds = Arrays.asList(breed);
        when(breedService.searchBreeds("Persian", 4, null, null, "Iran")).thenReturn(breeds);
        
        ResponseEntity<List<BreedDto>> response = breedController.searchBreeds("Persian", 4, null, null, "Iran");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Persian", response.getBody().get(0).getName());
        verify(breedService).searchBreeds("Persian", 4, null, null, "Iran");
    }
}