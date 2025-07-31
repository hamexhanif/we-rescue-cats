package io.werescuecats.backend.service;

import io.werescuecats.backend.config.CatApiConfig;
import io.werescuecats.backend.dto.BreedDto;
import io.werescuecats.backend.entity.Breed;
import io.werescuecats.backend.repository.BreedRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BreedServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CatApiConfig config;

    @Mock
    private BreedRepository breedRepository;

    @InjectMocks
    private BreedService breedService;

    private BreedDto testBreedDto;
    private Breed testBreed;

    @BeforeEach
    void setUp() {
        testBreedDto = createTestBreedDto();
        testBreed = createTestBreed();
    }

    @Test
    void fetchBreedsFromApi_Success() {
        when(config.getApiKey()).thenReturn("test-api-key");
        when(config.getBaseUrl()).thenReturn("https://api.thecatapi.com/v1");

        BreedDto[] breedArray = {testBreedDto};
        ResponseEntity<BreedDto[]> response = new ResponseEntity<>(breedArray, HttpStatus.OK);
        
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.GET), 
            any(HttpEntity.class), 
            eq(BreedDto[].class)
        )).thenReturn(response);

        when(breedRepository.saveAll(anyList())).thenReturn(Arrays.asList(testBreed));

        breedService.fetchBreedsFromApi();

        verify(restTemplate).exchange(
            eq("https://api.thecatapi.com/v1/breeds"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(BreedDto[].class)
        );
        verify(breedRepository).saveAll(anyList());
    }

    @Test
    void fetchBreedsFromApi_ApiFailure_ThrowsException() {
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.GET), 
            any(HttpEntity.class), 
            eq(BreedDto[].class)
        )).thenThrow(new RestClientException("API Error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            breedService.fetchBreedsFromApi();
        });
        
        assertEquals("API call failed", exception.getMessage());
        verify(breedRepository, never()).saveAll(anyList());
    }

    @Test
    @SuppressWarnings("null")
    void fetchBreedsFromApi_NullResponse_HandlesGracefully() {
        ResponseEntity<BreedDto[]> response = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.exchange(
            anyString(), 
            eq(HttpMethod.GET), 
            any(HttpEntity.class), 
            eq(BreedDto[].class)
        )).thenReturn(response);

        assertThrows(RuntimeException.class, () -> {
            breedService.fetchBreedsFromApi();
        });
    }

    @Test
    void getAllBreeds_ReturnsAllBreeds() {
        List<Breed> expectedBreeds = Arrays.asList(testBreed);
        when(breedRepository.findAll()).thenReturn(expectedBreeds);

        List<Breed> result = breedService.getAllBreeds();

        assertEquals(expectedBreeds, result);
        verify(breedRepository).findAll();
    }

    @Test
    void getBreedById_ExistingId_ReturnsBreed() {
        when(breedRepository.findById("siam")).thenReturn(Optional.of(testBreed));

        Optional<Breed> result = breedService.getBreedById("siam");

        assertTrue(result.isPresent());
        assertEquals(testBreed, result.get());
    }

    @Test
    void getBreedById_NonExistingId_ReturnsEmpty() {
        when(breedRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Optional<Breed> result = breedService.getBreedById("nonexistent");

        assertFalse(result.isPresent());
    }

    @Test
    void searchBreeds_WithName_CallsCorrectRepository() {
        List<Breed> expectedBreeds = Arrays.asList(testBreed);
        when(breedRepository.findByNameContainingIgnoreCase("siamese"))
            .thenReturn(expectedBreeds);

        List<Breed> result = breedService.searchBreeds("siamese", null, null, null, null);

        assertEquals(expectedBreeds, result);
        verify(breedRepository).findByNameContainingIgnoreCase("siamese");
        verify(breedRepository, never()).findBreedsWithFilters(any(), any(), any(), any());
    }

    @Test
    void searchBreeds_WithFilters_CallsCorrectRepository() {
        List<Breed> expectedBreeds = Arrays.asList(testBreed);
        when(breedRepository.findBreedsWithFilters(4, 3, null, null))
            .thenReturn(expectedBreeds);

        List<Breed> result = breedService.searchBreeds(null, 4, 3, null, null);

        assertEquals(expectedBreeds, result);
        verify(breedRepository).findBreedsWithFilters(4, 3, null, null);
        verify(breedRepository, never()).findByNameContainingIgnoreCase(any());
    }

    @Test
    void onApplicationReady_EmptyDatabase_FetchesBreeds() {
        when(config.getApiKey()).thenReturn("test-api-key");
        when(config.getBaseUrl()).thenReturn("https://api.thecatapi.com/v1");
        when(config.isFetchOnStartup()).thenReturn(true);

        when(breedRepository.count()).thenReturn(0L);
        BreedDto[] breedArray = {testBreedDto};
        ResponseEntity<BreedDto[]> response = new ResponseEntity<>(breedArray, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(BreedDto[].class)))
            .thenReturn(response);

        breedService.onApplicationReady();

        verify(breedRepository).count();
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(BreedDto[].class));
    }

    @Test
    void onApplicationReady_DatabaseHasBreeds_SkipsFetch() {
        when(config.isFetchOnStartup()).thenReturn(true);

        when(breedRepository.count()).thenReturn(67L);

        breedService.onApplicationReady();

        verify(breedRepository).count();
        verify(restTemplate, never()).exchange(
            anyString(),eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(BreedDto[].class)
        );
    }

    @Test
    void onApplicationReady_FetchDisabled_SkipsFetch() {
        when(config.isFetchOnStartup()).thenReturn(false);

        breedService.onApplicationReady();

        verify(restTemplate, never()).exchange(
            anyString(),eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(BreedDto[].class)
        );
        verify(breedRepository, never()).count();
    }

    private BreedDto createTestBreedDto() {
        BreedDto dto = new BreedDto();
        dto.setId("siam");
        dto.setName("Siamese");
        dto.setDescription("Active and playful");
        dto.setOrigin("Thailand");
        dto.setChildFriendly(4);
        dto.setDogFriendly(3);
        dto.setEnergyLevel(5);
        dto.setGrooming(2);
        dto.setHealthIssues(1);
        dto.setIntelligence(5);
        dto.setSocialNeeds(5);
        dto.setStrangerFriendly(2);
        dto.setAdaptability(5);
        dto.setAffectionLevel(5);
        dto.setWikipediaUrl("https://en.wikipedia.org/wiki/Siamese_cat");
        dto.setReferenceImageId("0XYvRd7oD");
        
        BreedDto.BreedImageDto image = new BreedDto.BreedImageDto();
        image.setId("0XYvRd7oD");
        image.setUrl("https://cdn2.thecatapi.com/images/0XYvRd7oD.jpg");
        dto.setImage(image);
        
        return dto;
    }

    private Breed createTestBreed() {
        Breed breed = new Breed();
        breed.setId("siam");
        breed.setName("Siamese");
        breed.setDescription("Active and playful");
        breed.setOrigin("Thailand");
        breed.setChildFriendly(4);
        breed.setDogFriendly(3);
        breed.setEnergyLevel(5);
        breed.setGrooming(2);
        breed.setHealthIssues(1);
        breed.setIntelligence(5);
        breed.setSocialNeeds(5);
        breed.setStrangerFriendly(2);
        breed.setAdaptability(5);
        breed.setAffectionLevel(5);
        breed.setWikipediaUrl("https://en.wikipedia.org/wiki/Siamese_cat");
        breed.setReferenceImageId("0XYvRd7oD");
        breed.setImageUrl("https://cdn2.thecatapi.com/images/0XYvRd7oD.jpg");
        return breed;
    }
}
