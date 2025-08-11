package io.werescuecats.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.SpyBean;
import io.werescuecats.backend.entity.Breed;
import io.werescuecats.backend.entity.Cat;
import io.werescuecats.backend.entity.CatStatus;
import io.werescuecats.backend.repository.CatRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatServiceTest {

    @Mock
    private CatRepository catRepository;

    @InjectMocks
    @SpyBean
    private CatService catService;

    private Cat testCat;
    private Breed testBreed;

    @BeforeEach
    void setUp() {
        testBreed = new Breed();
        testBreed.setId("persian");
        testBreed.setName("Persian");

        testCat = new Cat();
        testCat.setId(1L);
        testCat.setName("Fluffy");
        testCat.setAge(3);
        testCat.setDescription("A lovely Persian cat");
        testCat.setBreed(testBreed);
        testCat.setImageUrl("http://example.com/fluffy.jpg");
        testCat.setLatitude(51.0504);
        testCat.setLongitude(13.7373);
        testCat.setAddress("Dresden, Germany");
        testCat.setStatus(CatStatus.AVAILABLE);
        testCat.setCreatedAt(LocalDateTime.now());
        testCat.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getAvailableCats_ShouldReturnAvailableCats() {
        List<Cat> expectedCats = Arrays.asList(testCat);
        when(catRepository.findByStatus(CatStatus.AVAILABLE)).thenReturn(expectedCats);

        List<Cat> result = catService.getAvailableCats();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Fluffy");
        assertThat(result.get(0).getStatus()).isEqualTo(CatStatus.AVAILABLE);
        verify(catRepository).findByStatus(CatStatus.AVAILABLE);
    }

    @Test
    void getPendingCats_ShouldReturnPendingCats() {
        testCat.setStatus(CatStatus.PENDING);
        List<Cat> expectedCats = Arrays.asList(testCat);
        when(catRepository.findByStatus(CatStatus.PENDING)).thenReturn(expectedCats);

        List<Cat> result = catService.getPendingCats();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(CatStatus.PENDING);
        verify(catRepository).findByStatus(CatStatus.PENDING);
    }

    @Test
    void getAdoptedCats_ShouldReturnAdoptedCats() {
        testCat.setStatus(CatStatus.ADOPTED);
        List<Cat> expectedCats = Arrays.asList(testCat);
        when(catRepository.findByStatus(CatStatus.ADOPTED)).thenReturn(expectedCats);

        List<Cat> result = catService.getAdoptedCats();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(CatStatus.ADOPTED);
        verify(catRepository).findByStatus(CatStatus.ADOPTED);
    }

    @Test
    void getCatById_WhenCatExists_ShouldReturnCat() {
        when(catRepository.findById(1L)).thenReturn(Optional.of(testCat));

        Optional<Cat> result = catService.getCatById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Fluffy");
        verify(catRepository).findById(1L);
    }

    @Test
    void getCatById_WhenCatNotExists_ShouldReturnEmpty() {
        when(catRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Cat> result = catService.getCatById(999L);

        assertThat(result).isEmpty();
        verify(catRepository).findById(999L);
    }

    @Test
    void getCatsByBreed_ShouldReturnCatsOfSpecificBreed() {
        List<Cat> expectedCats = Arrays.asList(testCat);
        when(catRepository.findByBreedId("persian")).thenReturn(expectedCats);

        List<Cat> result = catService.getCatsByBreed("persian");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBreed().getId()).isEqualTo("persian");
        verify(catRepository).findByBreedId("persian");
    }

    @Test
    void getCatsInArea_ShouldCalculateBoundingBoxAndReturnCats() {
        Double latitude = 51.0504;
        Double longitude = 13.7373;
        Double radiusKm = 10.0;
        List<Cat> expectedCats = Arrays.asList(testCat);
        
        when(catRepository.findCatsInArea(anyDouble(), anyDouble(), anyDouble(), anyDouble(), eq(CatStatus.AVAILABLE)))
            .thenReturn(expectedCats);

        List<Cat> result = catService.getCatsInArea(latitude, longitude, radiusKm);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLatitude()).isEqualTo(latitude);
        assertThat(result.get(0).getLongitude()).isEqualTo(longitude);
        
        verify(catRepository).findCatsInArea(
            doubleThat(minLat -> minLat < latitude),
            doubleThat(maxLat -> maxLat > latitude),
            doubleThat(minLon -> minLon < longitude),
            doubleThat(maxLon -> maxLon > longitude),
            eq(CatStatus.AVAILABLE)
        );
    }

    // @Test
    // void saveCat_ShouldSaveAndReturnCat() {
    //     when(catRepository.save(testCat)).thenReturn(testCat);

    //     Cat result = catService.saveCat(testCat);

    //     assertThat(result).isEqualTo(testCat);
    //     assertThat(result.getName()).isEqualTo("Fluffy");
    //     verify(catRepository).save(testCat);
    // }

    @Test
    void updateCatStatus_WhenCatExists_ShouldUpdateStatus() {
        when(catRepository.findById(1L)).thenReturn(Optional.of(testCat));
        when(catRepository.save(any(Cat.class))).thenReturn(testCat);

        Cat result = catService.updateCatStatus(1L, CatStatus.ADOPTED);

        assertThat(result.getStatus()).isEqualTo(CatStatus.ADOPTED);
        verify(catRepository).findById(1L);
        verify(catRepository).save(testCat);
    }

    @Test
    void updateCatStatus_WhenCatNotExists_ShouldThrowException() {
        when(catRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catService.updateCatStatus(999L, CatStatus.ADOPTED))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Cat not found with id: 999");
        
        verify(catRepository).findById(999L);
        verify(catRepository, never()).save(any());
    }

    @Test
    void getAllCats_ShouldReturnAllCats() {
        List<Cat> expectedCats = Arrays.asList(testCat);
        when(catRepository.findAll()).thenReturn(expectedCats);

        List<Cat> result = catService.getAllCats();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Fluffy");
        verify(catRepository).findAll();
    }
}
