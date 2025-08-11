package io.werescuecats.backend.controller;

import io.werescuecats.backend.dto.CatDto;
import io.werescuecats.backend.dto.StatusUpdateRequestDto;
import io.werescuecats.backend.entity.Breed;
import io.werescuecats.backend.entity.Cat;
import io.werescuecats.backend.entity.CatStatus;
import io.werescuecats.backend.service.BreedService;
import io.werescuecats.backend.service.CatService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatControllerTest {

    @Mock
    private CatService catService;

    @Mock
    private BreedService breedService;

    @InjectMocks
    private CatController catController;

    private Cat cat;
    private Breed breed;
    private CatDto catDto;

    @BeforeEach
    void setUp() {
        breed = new Breed("persian", "Persian");

        cat = new Cat();
        cat.setId(1L);
        cat.setName("Fluffy");
        cat.setAge(2);
        cat.setGender("Female");
        cat.setDescription("A lovely cat");
        cat.setBreed(breed);
        cat.setStatus(CatStatus.AVAILABLE);

        catDto = CatDto.builder()
                .id(1L)
                .name("Fluffy")
                .age(2)
                .gender("Female")
                .description("A lovely cat")
                .breedId("persian")
                .breedName("Persian")
                .status(CatStatus.AVAILABLE)
                .build();
    }

    @Test
    void getAvailableCats_ShouldReturnAvailableCats() {
        List<Cat> availableCats = Arrays.asList(cat);
        when(catService.getAvailableCats()).thenReturn(availableCats);

        ResponseEntity<List<CatDto>> response = catController.getAvailableCats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Fluffy", response.getBody().get(0).getName());
        verify(catService).getAvailableCats();
    }

    @Test
    void getCatsByBreed_ShouldReturnCatsOfSpecificBreed() {
        List<Cat> breedCats = Arrays.asList(cat);
        when(catService.getCatsByBreed("persian")).thenReturn(breedCats);
        
        ResponseEntity<List<CatDto>> response = catController.getCatsByBreed("persian");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("persian", response.getBody().get(0).getBreedId());
        verify(catService).getCatsByBreed("persian");
    }

    @Test
    void getCatsInArea_ShouldReturnCatsInArea() {
        List<Cat> areaCats = Arrays.asList(cat);
        when(catService.getCatsInArea(40.7128, -74.0060, 10.0)).thenReturn(areaCats);

        ResponseEntity<List<CatDto>> response = catController.getCatsInArea(40.7128, -74.0060, 10.0);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(catService).getCatsInArea(40.7128, -74.0060, 10.0);
    }

    @Test
    void getCatById_ShouldReturnCat_WhenExists() {
        when(catService.getCatById(1L)).thenReturn(Optional.of(cat));
        
        ResponseEntity<CatDto> response = catController.getCatById(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Fluffy", response.getBody().getName());
        verify(catService).getCatById(1L);
    }

    @Test
    void getCatById_ShouldReturnNotFound_WhenDoesNotExist() {
        when(catService.getCatById(1L)).thenReturn(Optional.empty());

        ResponseEntity<CatDto> response = catController.getCatById(1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(catService).getCatById(1L);
    }

    @Test
    void createCat_ShouldReturnCreatedCat() {
        when(breedService.getBreedById("persian")).thenReturn(Optional.of(breed));
        when(catService.saveCat(any(Cat.class))).thenReturn(cat);
        
        ResponseEntity<CatDto> response = catController.createCat(catDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Fluffy", response.getBody().getName());
        verify(catService).saveCat(any(Cat.class));
    }

    @Test
    void updateCatStatus_ShouldReturnUpdatedCat() {
        cat.setStatus(CatStatus.ADOPTED);
        StatusUpdateRequestDto request = new StatusUpdateRequestDto();
        request.setStatus(CatStatus.ADOPTED);
        when(catService.updateCatStatus(1L, CatStatus.ADOPTED)).thenReturn(cat);
        
        ResponseEntity<CatDto> response = catController.updateCatStatus(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(CatStatus.ADOPTED, response.getBody().getStatus());
        verify(catService).updateCatStatus(1L, CatStatus.ADOPTED);
    }

    @Test
    void getPendingCats_ShouldReturnPendingCats() {
        cat.setStatus(CatStatus.PENDING);
        when(catService.getPendingCats()).thenReturn(List.of(cat));
        
        ResponseEntity<List<CatDto>> response = catController.getPendingCats();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(CatStatus.PENDING, response.getBody().get(0).getStatus());
        verify(catService).getPendingCats();
    }

    @Test
    void getAdoptedCats_ShouldReturnAdoptedCats() {
        cat.setStatus(CatStatus.ADOPTED);
        when(catService.getAdoptedCats()).thenReturn(List.of(cat));
        
        ResponseEntity<List<CatDto>> response = catController.getAdoptedCats();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(CatStatus.ADOPTED, response.getBody().get(0).getStatus());
        verify(catService).getAdoptedCats();
    }

    @Test
    void getAllCats_ShouldReturnAllCats() {
        when(catService.getAllCats()).thenReturn(List.of(cat));
        
        ResponseEntity<List<CatDto>> response = catController.getAllCats();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Fluffy", response.getBody().get(0).getName());
        verify(catService).getAllCats();
    }

    @Test
    void createCat_ShouldReturnBadRequest_WhenExceptionThrown() {
        when(breedService.getBreedById("persian")).thenReturn(Optional.of(breed));
        doThrow(new RuntimeException("DB error")).when(catService).saveCat(any(Cat.class));
        
        ResponseEntity<CatDto> response = catController.createCat(catDto);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(catService).saveCat(any(Cat.class));
    }

    @Test
    void updateCatStatus_ShouldReturnBadRequest_WhenExceptionThrown() {
        StatusUpdateRequestDto request = new StatusUpdateRequestDto();
        request.setStatus(CatStatus.ADOPTED);
        doThrow(new RuntimeException("Update failed"))
                .when(catService).updateCatStatus(1L, CatStatus.ADOPTED);
        
        ResponseEntity<CatDto> response = catController.updateCatStatus(1L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(catService).updateCatStatus(1L, CatStatus.ADOPTED);
    }

    @Test
    void toDto_ShouldMapCatEntityToDto() {
        cat.setLatitude(12.34);
        cat.setLongitude(56.78);
        cat.setAddress("123 Cat Street");

        CatDto dto = catController.toDto(cat);
        
        assertNotNull(dto);
        assertEquals(cat.getId(), dto.getId());
        assertEquals(cat.getName(), dto.getName());
        assertEquals(cat.getAge(), dto.getAge());
        assertEquals(cat.getGender(), dto.getGender());
        assertEquals(cat.getDescription(), dto.getDescription());
        assertEquals(cat.getBreed().getId(), dto.getBreedId());
        assertEquals(cat.getBreed().getName(), dto.getBreedName());
        assertEquals(cat.getImageUrl(), dto.getImageUrl());
        assertEquals(cat.getLatitude(), dto.getLatitude());
        assertEquals(cat.getLongitude(), dto.getLongitude());
        assertEquals(cat.getAddress(), dto.getAddress());
        assertEquals(cat.getStatus(), dto.getStatus());
    }

    @Test
    void toDto_ShouldReturnNull_WhenCatIsNull() {
        assertNull(catController.toDto(null));
    }

    @Test
    void toCatEntity_ShouldMapDtoToCatEntity() {
        when(breedService.getBreedById("persian")).thenReturn(Optional.of(breed));
        catDto.setLatitude(12.34);
        catDto.setLongitude(56.78);
        catDto.setAddress("123 Cat Street");

        Cat entity = catController.toCatEntity(catDto);
        
        assertNotNull(entity);
        assertEquals(catDto.getName(), entity.getName());
        assertEquals(catDto.getAge(), entity.getAge());
        assertEquals(catDto.getGender(), entity.getGender());
        assertEquals(catDto.getDescription(), entity.getDescription());
        assertEquals(breed, entity.getBreed());
        assertEquals(catDto.getImageUrl(), entity.getImageUrl());
        assertEquals(catDto.getLatitude(), entity.getLatitude());
        assertEquals(catDto.getLongitude(), entity.getLongitude());
        assertEquals(catDto.getAddress(), entity.getAddress());
        assertEquals(CatStatus.AVAILABLE, entity.getStatus());
    }
}
