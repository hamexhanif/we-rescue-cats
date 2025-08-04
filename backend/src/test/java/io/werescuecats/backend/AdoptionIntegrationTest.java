package io.werescuecats.backend;

import io.werescuecats.backend.dto.AdoptionDto;
import io.werescuecats.backend.dto.AdoptionRequestDto;
import io.werescuecats.backend.entity.Adoption;
import io.werescuecats.backend.entity.Breed;
import io.werescuecats.backend.entity.Cat;
import io.werescuecats.backend.entity.CatStatus;
import io.werescuecats.backend.entity.User;
import io.werescuecats.backend.entity.UserRole;
import io.werescuecats.backend.repository.AdoptionRepository;
import io.werescuecats.backend.repository.CatRepository;
import io.werescuecats.backend.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.main.allow-bean-definition-overriding=true"
})
class AdoptionIntegrationTest {

    @MockBean(name = "filterChain")
    SecurityFilterChain filterChain;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AdoptionRepository adoptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CatRepository catRepository;

    @LocalServerPort
    private int port;

    private String baseUrl;
    private User testUser;
    private Cat testCat;

    @BeforeEach
    void setUp() {
        adoptionRepository.deleteAll();
        catRepository.deleteAll();
        userRepository.deleteAll();

        baseUrl = "http://localhost:" + port + "/api/adoptions";

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPasswordHash("password");
        testUser.setRole(UserRole.USER);
        testUser = userRepository.save(testUser);

        testCat = new Cat();
        testCat.setName("Fluffy");
        testCat.setStatus(CatStatus.AVAILABLE);
        testCat.setBreed(new Breed("siam", "Siamese"));
        testCat = catRepository.save(testCat);
    }

    @Test
    @DisplayName("Should create adoption through REST API")
    void createAdoption_Integration() {
        AdoptionRequestDto request = new AdoptionRequestDto();
        request.setUserId(testUser.getId());
        request.setCatId(testCat.getId());
        request.setNotes("Integration test notes");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AdoptionRequestDto> entity = new HttpEntity<>(request, headers);

        ResponseEntity<AdoptionDto> response = restTemplate.postForEntity(baseUrl, entity, AdoptionDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNotes()).isEqualTo("Integration test notes");
        assertThat(response.getBody().getUser().getId()).isEqualTo(testUser.getId());
        assertThat(response.getBody().getCat().getId()).isEqualTo(testCat.getId());

        List<Adoption> adoptions = adoptionRepository.findAll();
        assertThat(adoptions).hasSize(1);
        assertThat(adoptions.get(0).getNotes()).isEqualTo("Integration test notes");
    }

    @Test
    @DisplayName("Should not create adoption if cat is already adopted")
    void createAdoption_CatAlreadyAdopted() {
        testCat = new Cat();
        testCat.setName("Fluffy");
        testCat.setStatus(CatStatus.ADOPTED);
        testCat.setBreed(new Breed("siam", "Siamese"));
        testCat = catRepository.save(testCat);

        // Cat already adopted
        Adoption existingAdoption = new Adoption(testUser, testCat, "Already adopted");
        adoptionRepository.save(existingAdoption);

        AdoptionRequestDto request = new AdoptionRequestDto();
        request.setUserId(testUser.getId());
        request.setCatId(testCat.getId());
        request.setNotes("Trying again");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AdoptionRequestDto> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should return user's adoptions through REST API")
    void getUserAdoptions_Integration() {
        Adoption adoption = new Adoption(testUser, testCat, "Test notes");
        adoptionRepository.save(adoption);

        ResponseEntity<AdoptionDto[]> response = restTemplate.getForEntity(
            baseUrl + "/user/" + testUser.getId(), AdoptionDto[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getNotes()).isEqualTo("Test notes");
    }

    @Test
    @DisplayName("Should return empty list when user has no adoptions")
    void getUserAdoptions_Empty() {
        User newUser = new User();
        newUser.setEmail("new@example.com");
        newUser.setFirstName("Empty");
        newUser.setLastName("User");
        newUser.setPasswordHash("password");
        newUser.setRole(UserRole.USER);
        newUser = userRepository.save(newUser);

        ResponseEntity<AdoptionDto[]> response = restTemplate.getForEntity(
            baseUrl + "/user/" + newUser.getId(), AdoptionDto[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("Should return 404 when user or cat does not exist")
    void createAdoption_NonExistentUserOrCat() {
        AdoptionRequestDto request = new AdoptionRequestDto();
        request.setUserId(999L); // non-existent user
        request.setCatId(888L); // non-existent cat
        request.setNotes("Should fail");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AdoptionRequestDto> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
