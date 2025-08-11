package io.werescuecats.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.werescuecats.backend.dto.AdoptionRequestDto;
import io.werescuecats.backend.dto.RejectAdoptionRequestDto;
import io.werescuecats.backend.entity.*;
import io.werescuecats.backend.repository.AdoptionRepository;
import io.werescuecats.backend.repository.CatRepository;
import io.werescuecats.backend.repository.UserRepository;
import io.werescuecats.backend.repository.BreedRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import io.werescuecats.backend.security.CustomUserDetails;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AdoptionIntegrationTest {

    // Custom annotation for mock authentication with CustomUserDetails
    @Retention(RetentionPolicy.RUNTIME)
    @WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
    @interface WithMockCustomUser {
        String email() default "user@test.com";
        String role() default "USER";
    }

    // Factory for creating security context with CustomUserDetails
    static class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
        @Override
        public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            
            // Create a mock user based on role
            User mockUser = new User();
            mockUser.setId(customUser.role().equals("ADMIN") ? 2L : 1L);
            mockUser.setEmail(customUser.email());
            mockUser.setFirstName(customUser.role().equals("ADMIN") ? "Admin" : "John");
            mockUser.setLastName(customUser.role().equals("ADMIN") ? "User" : "Doe");
            mockUser.setRole(customUser.role().equals("ADMIN") ? UserRole.ADMIN : UserRole.USER);
            mockUser.setEnabled(true);
            
            CustomUserDetails principal = new CustomUserDetails(mockUser);
            UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken(principal, "password", principal.getAuthorities());
            context.setAuthentication(auth);
            return context;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdoptionRepository adoptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CatRepository catRepository;

    @Autowired
    private BreedRepository breedRepository;

    private User testUser;
    private User adminUser;
    private Cat availableCat;
    private Cat unavailableCat;
    private Breed testBreed;

    @BeforeEach
    void setUp() {
        // Clear repositories
        adoptionRepository.deleteAll();
        catRepository.deleteAll();
        userRepository.deleteAll();
        breedRepository.deleteAll();

        // Create test breed
        testBreed = new Breed();
        testBreed.setId("persian");
        testBreed.setName("Persian");
        testBreed = breedRepository.save(testBreed);

        // Create test users
        testUser = new User();
        testUser.setEmail("user@test.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPasswordHash("password");
        testUser.setStreetAddress("123 Main St, Springfield");
        testUser.setRole(UserRole.USER);
        testUser = userRepository.save(testUser);

        adminUser = new User();
        adminUser.setEmail("admin@test.com");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setPasswordHash("password");
        adminUser.setRole(UserRole.ADMIN);
        adminUser = userRepository.save(adminUser);

        // Create test cats
        availableCat = new Cat();
        availableCat.setName("Fluffy");
        availableCat.setAge(2);
        availableCat.setBreed(testBreed);
        availableCat.setStatus(CatStatus.AVAILABLE);
        availableCat.setDescription("A friendly cat");
        availableCat = catRepository.save(availableCat);

        unavailableCat = new Cat();
        unavailableCat.setName("Adopted Cat");
        unavailableCat.setAge(3);
        unavailableCat.setBreed(testBreed);
        unavailableCat.setStatus(CatStatus.ADOPTED);
        unavailableCat.setDescription("Already adopted");
        unavailableCat = catRepository.save(unavailableCat);
    }

    // HAPPY PATH TESTS
    @Test
    @WithMockCustomUser
    void createAdoption_ValidRequest_Success() throws Exception {
        AdoptionRequestDto request = new AdoptionRequestDto();
        request.setUserId(testUser.getId());
        request.setCatId(availableCat.getId());
        request.setNotes("I love cats!");

        mockMvc.perform(post("/api/adoptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.notes").value("I love cats!"))
                .andExpect(jsonPath("$.user.email").value("user@test.com"))
                .andExpect(jsonPath("$.cat.name").value("Fluffy"));

        List<Adoption> adoptions = adoptionRepository.findAll();
        assertEquals(1, adoptions.size());
        assertEquals(AdoptionStatus.PENDING, adoptions.get(0).getStatus());

        Cat updatedCat = catRepository.findById(availableCat.getId()).orElseThrow();
        assertEquals(CatStatus.PENDING, updatedCat.getStatus());
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    void approveAdoption_PendingAdoption_Success() throws Exception {
        Adoption adoption = createPendingAdoption();

        mockMvc.perform(put("/api/adoptions/{id}/approve", adoption.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approvedDate").exists());

        Adoption updatedAdoption = adoptionRepository.findById(adoption.getId()).orElseThrow();
        assertEquals(AdoptionStatus.APPROVED, updatedAdoption.getStatus());
        assertNotNull(updatedAdoption.getApprovedDate());
        assertNotNull(updatedAdoption.getProcessedByAdmin());
        assertEquals("Admin", updatedAdoption.getProcessedByAdmin().getFirstName());
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    void completeAdoption_ApprovedAdoption_Success() throws Exception {
        Adoption adoption = createApprovedAdoption();

        mockMvc.perform(put("/api/adoptions/{id}/complete", adoption.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedDate").exists());

        Adoption updatedAdoption = adoptionRepository.findById(adoption.getId()).orElseThrow();
        assertEquals(AdoptionStatus.COMPLETED, updatedAdoption.getStatus());
        assertNotNull(updatedAdoption.getCompletedDate());

        Cat updatedCat = catRepository.findById(availableCat.getId()).orElseThrow();
        assertEquals(CatStatus.ADOPTED, updatedCat.getStatus());
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    void rejectAdoption_PendingAdoption_Success() throws Exception {
        Adoption adoption = createPendingAdoption();

        RejectAdoptionRequestDto rejectRequest = new RejectAdoptionRequestDto();
        rejectRequest.setReason("Incomplete application");

        mockMvc.perform(put("/api/adoptions/{id}/reject", adoption.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rejectRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.adminNotes").value("Incomplete application"));

        Adoption updatedAdoption = adoptionRepository.findById(adoption.getId()).orElseThrow();
        assertEquals(AdoptionStatus.REJECTED, updatedAdoption.getStatus());
        assertEquals("Incomplete application", updatedAdoption.getAdminNotes());

        Cat updatedCat = catRepository.findById(availableCat.getId()).orElseThrow();
        assertEquals(CatStatus.AVAILABLE, updatedCat.getStatus());
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    void getPendingAdoptions_HasPendingAdoptions_Success() throws Exception {
        createPendingAdoption();
        createApprovedAdoption();

        mockMvc.perform(get("/api/adoptions/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockCustomUser
    void getAdoptionsByUser_UserHasAdoptions_Success() throws Exception {
        createPendingAdoption();
        createApprovedAdoption();

        mockMvc.perform(get("/api/adoptions/user/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockCustomUser
    void getUserAdoptionStats_UserWithAdoptions_Success() throws Exception {
        createPendingAdoption();
        createCompletedAdoption();

        mockMvc.perform(get("/api/adoptions/user/{userId}/stats", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.totalApplications").value(2))
                .andExpect(jsonPath("$.completedAdoptions").value(1))
                .andExpect(jsonPath("$.pendingApplications").value(1));
    }

    // ERROR CASES
    @Test
    @WithMockCustomUser
    void createAdoption_UserNotFound_NotFound() throws Exception {
        AdoptionRequestDto request = new AdoptionRequestDto();
        request.setUserId(999L); // Non-existent user
        request.setCatId(availableCat.getId());
        request.setNotes("I love cats!");

        mockMvc.perform(post("/api/adoptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound()); // UserNotFoundException

        assertEquals(0, adoptionRepository.count());
    }

    @Test
    @WithMockCustomUser
    void createAdoption_CatNotFound_NotFound() throws Exception {
        AdoptionRequestDto request = new AdoptionRequestDto();
        request.setUserId(testUser.getId());
        request.setCatId(999L); // Non-existent cat
        request.setNotes("I love cats!");

        mockMvc.perform(post("/api/adoptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        assertEquals(0, adoptionRepository.count());
    }

    @Test
    @WithMockCustomUser
    void createAdoption_CatNotAvailable_BadRequest() throws Exception {
        AdoptionRequestDto request = new AdoptionRequestDto();
        request.setUserId(testUser.getId());
        request.setCatId(unavailableCat.getId()); // Cat is already adopted
        request.setNotes("I love cats!");

        mockMvc.perform(post("/api/adoptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertEquals(0, adoptionRepository.count());
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    void approveAdoption_AdoptionNotFound_BadRequest() throws Exception {
        mockMvc.perform(put("/api/adoptions/{id}/approve", 999L))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    void approveAdoption_WrongStatus_BadRequest() throws Exception {
        // Create an already approved adoption
        Adoption adoption = createApprovedAdoption();

        mockMvc.perform(put("/api/adoptions/{id}/approve", adoption.getId()))
                .andExpect(status().isBadRequest());

        // Verify status didn't change
        Adoption unchangedAdoption = adoptionRepository.findById(adoption.getId()).orElseThrow();
        assertEquals(AdoptionStatus.APPROVED, unchangedAdoption.getStatus());
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    void completeAdoption_WrongStatus_BadRequest() throws Exception {
        // Try to complete a pending adoption (should be approved first)
        Adoption adoption = createPendingAdoption();

        mockMvc.perform(put("/api/adoptions/{id}/complete", adoption.getId()))
                .andExpect(status().isBadRequest());

        // Verify status didn't change
        Adoption unchangedAdoption = adoptionRepository.findById(adoption.getId()).orElseThrow();
        assertEquals(AdoptionStatus.PENDING, unchangedAdoption.getStatus());
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    void rejectAdoption_EmptyReason_BadRequest() throws Exception {
        Adoption adoption = createPendingAdoption();

        RejectAdoptionRequestDto rejectRequest = new RejectAdoptionRequestDto();
        rejectRequest.setReason(""); // Empty reason

        mockMvc.perform(put("/api/adoptions/{id}/reject", adoption.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rejectRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    void getAdoptionById_NotFound_NotFound() throws Exception {
        mockMvc.perform(get("/api/adoptions/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(role = "ADMIN")
    void getAdoptionById_Found_Success() throws Exception {
        Adoption adoption = createPendingAdoption();

        mockMvc.perform(get("/api/adoptions/{id}", adoption.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(adoption.getId()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    // ADMIN-ONLY OPERATIONS (Security Tests)
    @Test
    @WithMockCustomUser
    void getPendingAdoptions_UserRole_Forbidden() throws Exception {
        mockMvc.perform(get("/api/adoptions/pending"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    void getAllAdoptions_UserRole_Forbidden() throws Exception {
        mockMvc.perform(get("/api/adoptions"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    void getAdoptionById_UserRole_Forbidden() throws Exception {
        Adoption adoption = createPendingAdoption();

        mockMvc.perform(get("/api/adoptions/{id}", adoption.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    void approveAdoption_UserRole_Forbidden() throws Exception {
        Adoption adoption = createPendingAdoption();

        mockMvc.perform(put("/api/adoptions/{id}/approve", adoption.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    void completeAdoption_UserRole_Forbidden() throws Exception {
        Adoption adoption = createApprovedAdoption();

        mockMvc.perform(put("/api/adoptions/{id}/complete", adoption.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser
    void rejectAdoption_UserRole_Forbidden() throws Exception {
        Adoption adoption = createPendingAdoption();

        RejectAdoptionRequestDto rejectRequest = new RejectAdoptionRequestDto();
        rejectRequest.setReason("Test reason");

        mockMvc.perform(put("/api/adoptions/{id}/reject", adoption.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rejectRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllEndpoints_NoAuthentication_Forbidden() throws Exception {
        mockMvc.perform(get("/api/adoptions/pending"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/adoptions"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/adoptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    // HELPER METHODS
    private Adoption createPendingAdoption() {
        Adoption adoption = new Adoption(testUser, availableCat, "Test notes");
        return adoptionRepository.save(adoption);
    }

    private Adoption createApprovedAdoption() {
        Adoption adoption = new Adoption(testUser, availableCat, "Test notes");
        adoption.setStatus(AdoptionStatus.APPROVED);
        adoption.setProcessedByAdmin(adminUser);
        adoption.setApprovedDate(LocalDateTime.now());
        return adoptionRepository.save(adoption);
    }

    private Adoption createCompletedAdoption() {
        Adoption adoption = new Adoption(testUser, availableCat, "Test notes");
        adoption.setStatus(AdoptionStatus.COMPLETED);
        adoption.setProcessedByAdmin(adminUser);
        adoption.setApprovedDate(LocalDateTime.now().minusDays(1));
        adoption.setCompletedDate(LocalDateTime.now());
        return adoptionRepository.save(adoption);
    }
}