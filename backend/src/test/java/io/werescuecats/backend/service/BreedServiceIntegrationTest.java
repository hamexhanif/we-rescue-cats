package io.werescuecats.backend.service;

import io.werescuecats.backend.entity.Breed;
import io.werescuecats.backend.repository.BreedRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "catapi.fetch-on-startup=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class BreedServiceIntegrationTest {

    @Autowired
    private BreedService breedService;

    @Autowired
    private BreedRepository breedRepository;

    @Test
    void getAllBreeds_WithCaching_ReturnsResults() {
        Breed breed1 = createTestBreed("siam", "Siamese");
        Breed breed2 = createTestBreed("pers", "Persian");
        breedRepository.saveAll(List.of(breed1, breed2));

        List<Breed> result1 = breedService.getAllBreeds();
        List<Breed> result2 = breedService.getAllBreeds(); // Should use cache

        assertEquals(2, result1.size());
        assertEquals(2, result2.size());
        assertEquals(result1, result2);
    }

    @Test
    void searchBreeds_Integration_WorksCorrectly() {
        Breed siamese = createTestBreed("siam", "Siamese");
        siamese.setChildFriendly(5);
        siamese.setDogFriendly(3);
        
        Breed persian = createTestBreed("pers", "Persian");
        persian.setChildFriendly(4);
        persian.setDogFriendly(2);
        
        breedRepository.saveAll(List.of(siamese, persian));

        //Search by name
        List<Breed> byName = breedService.searchBreeds("siamese", null, null, null, null);
        assertEquals(1, byName.size());
        assertEquals("Siamese", byName.get(0).getName());

        //Search by child friendly
        List<Breed> childFriendly = breedService.searchBreeds(null, 5, null, null, null);
        assertEquals(1, childFriendly.size());
        assertEquals("Siamese", childFriendly.get(0).getName());

        //Search by dog friendly
        List<Breed> dogFriendly = breedService.searchBreeds(null, null, 2, null, null);
        assertEquals(2, dogFriendly.size());
    }

    private Breed createTestBreed(String id, String name) {
        Breed breed = new Breed();
        breed.setId(id);
        breed.setName(name);
        breed.setDescription("Test breed");
        breed.setOrigin("Test Country");
        breed.setChildFriendly(4);
        breed.setDogFriendly(3);
        breed.setEnergyLevel(3);
        return breed;
    }
}
