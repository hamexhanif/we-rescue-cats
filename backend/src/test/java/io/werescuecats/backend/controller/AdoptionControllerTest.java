package io.werescuecats.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.werescuecats.backend.config.SecurityConfig;
import io.werescuecats.backend.dto.AdoptionRequestDto;
import io.werescuecats.backend.entity.Adoption;
import io.werescuecats.backend.entity.AdoptionStatus;
import io.werescuecats.backend.entity.Cat;
import io.werescuecats.backend.entity.User;
import io.werescuecats.backend.exception.CatNotAvailableException;
import io.werescuecats.backend.security.CustomUserDetails;
import io.werescuecats.backend.security.CustomUserDetailsService;
import io.werescuecats.backend.service.AdoptionService;

@WebMvcTest(AdoptionController.class)
@Import(SecurityConfig.class)
class AdoptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdoptionService adoptionService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private AdoptionRequestDto adoptionRequest;
    private Adoption testAdoption;
    private User testUser;
    private Cat testCat;

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

        testAdoption = new Adoption(testUser, testCat, "Test notes");
        testAdoption.setId(1L);

        adoptionRequest = new AdoptionRequestDto();
        adoptionRequest.setUserId(1L);
        adoptionRequest.setCatId(1L);
        adoptionRequest.setNotes("Test notes");
    }

    @Test
    @WithMockUser
    @DisplayName("Should create adoption successfully")
    void createAdoption_Success() throws Exception {
        when(adoptionService.createAdoption(1L, 1L, "Test notes")).thenReturn(testAdoption);

        mockMvc.perform(post("/api/adoptions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adoptionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.notes").value("Test notes"))
                .andExpect(jsonPath("$.user.id").value(1L))
                .andExpect(jsonPath("$.cat.id").value(1L));
    }

    @Test
    @WithMockUser
    @DisplayName("Should return bad request when creation fails")
    void createAdoption_BadRequest() throws Exception {
        when(adoptionService.createAdoption(1L, 1L, "Test notes"))
            .thenThrow(new CatNotAvailableException("Cat not available"));

        mockMvc.perform(post("/api/adoptions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adoptionRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get pending adoptions for admin")
    void getPendingAdoptions_Success() throws Exception {
        List<Adoption> pendingAdoptions = Arrays.asList(testAdoption);
        when(adoptionService.getPendingAdoptions()).thenReturn(pendingAdoptions);

        mockMvc.perform(get("/api/adoptions/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should deny access to pending adoptions for non-admin")
    void getPendingAdoptions_AccessDenied() throws Exception {
        mockMvc.perform(get("/api/adoptions/pending"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should get adoptions by user")
    void getAdoptionsByUser_Success() throws Exception {
        List<Adoption> userAdoptions = Arrays.asList(testAdoption);
        when(adoptionService.getAdoptionsByUser(1L)).thenReturn(userAdoptions);

        mockMvc.perform(get("/api/adoptions/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get all adoptions for admin")
    void getAllAdoptions_Success() throws Exception {
        when(adoptionService.getAllAdoptions()).thenReturn(List.of(testAdoption));

        mockMvc.perform(get("/api/adoptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should forbid non-admin from getting all adoptions")
    void getAllAdoptions_Forbidden() throws Exception {
        mockMvc.perform(get("/api/adoptions"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get adoption by ID if exists")
    void getAdoptionById_Success() throws Exception {
        when(adoptionService.getAdoptionById(1L)).thenReturn(Optional.of(testAdoption));

        mockMvc.perform(get("/api/adoptions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 if adoption by ID not found")
    void getAdoptionById_NotFound() throws Exception {
        when(adoptionService.getAdoptionById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/adoptions/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should forbid non-admin from getting adoption by ID")
    void getAdoptionById_Forbidden() throws Exception {
        mockMvc.perform(get("/api/adoptions/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should approve adoption successfully")
    void approveAdoption_Success() throws Exception {
        User mockAdmin = new User();
        mockAdmin.setId(2L);
        mockAdmin.setEmail("admin@example.com");
        mockAdmin.setFirstName("Admin");
        mockAdmin.setLastName("User");

        // Create mock CustomUserDetails
        CustomUserDetails mockUserDetails = mock(CustomUserDetails.class);
        when(mockUserDetails.getUser()).thenReturn(mockAdmin);
        
        testAdoption.setStatus(AdoptionStatus.APPROVED);
        when(adoptionService.approveAdoption(eq(1L), any(User.class))).thenReturn(testAdoption);

        mockMvc.perform(put("/api/adoptions/1/approve")
                .with(csrf()) // Add CSRF token
                .with(authentication(new UsernamePasswordAuthenticationToken(mockUserDetails, null, 
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should deny access to approve adoption for non-admin")
    void approveAdoption_Forbidden() throws Exception {
        mockMvc.perform(put("/api/adoptions/1/approve"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should complete adoption successfully")
    void completeAdoption_Success() throws Exception {
        User admin = new User();
        admin.setId(2L);
        CustomUserDetails mockDetails = mock(CustomUserDetails.class);
        when(mockDetails.getUser()).thenReturn(admin);

        testAdoption.setStatus(AdoptionStatus.COMPLETED);
        when(adoptionService.completeAdoption(eq(1L), any(User.class))).thenReturn(testAdoption);

        mockMvc.perform(put("/api/adoptions/1/complete")
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockDetails, null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should forbid user from completing adoption")
    void completeAdoption_Forbidden() throws Exception {
        mockMvc.perform(put("/api/adoptions/1/complete").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should reject adoption with valid reason")
    void rejectAdoption_Success() throws Exception {
        User admin = new User();
        admin.setId(2L);
        CustomUserDetails mockDetails = mock(CustomUserDetails.class);
        when(mockDetails.getUser()).thenReturn(admin);

        testAdoption.setStatus(AdoptionStatus.REJECTED);
        when(adoptionService.rejectAdoption(eq(1L), any(User.class), eq("Invalid info"))).thenReturn(testAdoption);

        mockMvc.perform(put("/api/adoptions/1/reject")
                .with(csrf())
                .with(authentication(new UsernamePasswordAuthenticationToken(mockDetails, null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Invalid info\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return bad request on rejection with invalid body")
    void rejectAdoption_InvalidRequestBody() throws Exception {
        mockMvc.perform(put("/api/adoptions/1/reject")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")) // Missing reason
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should forbid user from rejecting adoption")
    void rejectAdoption_Forbidden() throws Exception {
        mockMvc.perform(put("/api/adoptions/1/reject")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Not allowed\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should get user adoption stats")
    void getUserAdoptionStats_Success() throws Exception {
        when(adoptionService.getUserAdoptionCount(1L)).thenReturn(2L);
        when(adoptionService.getAdoptionsByUser(1L)).thenReturn(Arrays.asList(testAdoption));

        mockMvc.perform(get("/api/adoptions/user/1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.completedAdoptions").value(2L))
                .andExpect(jsonPath("$.totalApplications").value(1));
    }
}
