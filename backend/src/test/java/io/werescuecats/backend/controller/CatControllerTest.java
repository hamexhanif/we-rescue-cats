package io.werescuecats.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.werescuecats.backend.config.SecurityConfig;
import io.werescuecats.backend.dto.StatusUpdateRequestDto;
import io.werescuecats.backend.entity.Breed;
import io.werescuecats.backend.entity.Cat;
import io.werescuecats.backend.entity.CatStatus;
import io.werescuecats.backend.security.CustomUserDetailsService;
import io.werescuecats.backend.service.CatService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CatController.class)
@Import(SecurityConfig.class)
class CatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CatService catService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

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
    @WithMockUser
    void getAvailableCats_ShouldReturnAvailableCats() throws Exception {
        List<Cat> cats = Arrays.asList(testCat);
        when(catService.getAvailableCats()).thenReturn(cats);

        mockMvc.perform(get("/api/cats/available"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Fluffy"))
                .andExpect(jsonPath("$[0].age").value(3))
                .andExpect(jsonPath("$[0].breedName").value("Persian"))
                .andExpect(jsonPath("$[0].address").value("Dresden, Germany"))
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));

        verify(catService).getAvailableCats();
    }

    @Test
    @WithMockUser
    void getCatsByBreed_ShouldReturnCatsOfSpecificBreed() throws Exception {
        List<Cat> cats = Arrays.asList(testCat);
        when(catService.getCatsByBreed("persian")).thenReturn(cats);

        mockMvc.perform(get("/api/cats/breed/persian"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].breedName").value("Persian"));

        verify(catService).getCatsByBreed("persian");
    }

    @Test
    @WithMockUser
    void getCatsInArea_ShouldReturnCatsInSpecifiedArea() throws Exception {
        // Given
        List<Cat> cats = Arrays.asList(testCat);
        when(catService.getCatsInArea(51.0504, 13.7373, 10.0)).thenReturn(cats);

        // When & Then
        mockMvc.perform(get("/api/cats/area")
                .param("lat", "51.0504")
                .param("lon", "13.7373")
                .param("radius", "10.0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Fluffy"));

        verify(catService).getCatsInArea(51.0504, 13.7373, 10.0);
    }

    @Test
    @WithMockUser
    void getCatsInArea_WithDefaultRadius_ShouldUseDefaultValue() throws Exception {
        // Given
        List<Cat> cats = Arrays.asList(testCat);
        when(catService.getCatsInArea(51.0504, 13.7373, 10.0)).thenReturn(cats);

        // When & Then
        mockMvc.perform(get("/api/cats/area")
                .param("lat", "51.0504")
                .param("lon", "13.7373"))
                .andExpect(status().isOk());

        verify(catService).getCatsInArea(51.0504, 13.7373, 10.0);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPendingCats_WithAdminRole_ShouldReturnPendingCats() throws Exception {
        testCat.setStatus(CatStatus.PENDING);
        List<Cat> cats = Arrays.asList(testCat);
        when(catService.getPendingCats()).thenReturn(cats);

        mockMvc.perform(get("/api/cats/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(catService).getPendingCats();
    }

    @Test
    @WithMockUser
    void getPendingCats_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/cats/pending"))
                .andExpect(status().isForbidden());

        verify(catService, never()).getPendingCats();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAdoptedCats_WithAdminRole_ShouldReturnAdoptedCats() throws Exception {
        testCat.setStatus(CatStatus.ADOPTED);
        List<Cat> cats = Arrays.asList(testCat);
        when(catService.getAdoptedCats()).thenReturn(cats);

        mockMvc.perform(get("/api/cats/adopted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ADOPTED"));

        verify(catService).getAdoptedCats();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCatById_WhenCatExists_ShouldReturnCat() throws Exception {
        when(catService.getCatById(1L)).thenReturn(Optional.of(testCat));

        mockMvc.perform(get("/api/cats/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Fluffy"))
                .andExpect(jsonPath("$.description").value("A lovely Persian cat"))
                .andExpect(jsonPath("$.breedId").value("persian"))
                .andExpect(jsonPath("$.breedName").value("Persian"))
                .andExpect(jsonPath("$.latitude").value(51.0504))
                .andExpect(jsonPath("$.longitude").value(13.7373));

        verify(catService).getCatById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCatById_WhenCatNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(catService.getCatById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/cats/999"))
                .andExpect(status().isNotFound());

        verify(catService).getCatById(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCats_ShouldReturnPagedCats() throws Exception {
        // Given
        List<Cat> cats = Arrays.asList(testCat);
        Page<Cat> page = new PageImpl<>(cats, PageRequest.of(0, 10), 1);
        when(catService.getAllCats(any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/cats")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Fluffy"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(10));

        verify(catService).getAllCats(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCat_WithValidData_ShouldCreateCat() throws Exception {
        // Given
        when(catService.saveCat(any(Cat.class))).thenReturn(testCat);

        // When & Then
        mockMvc.perform(post("/api/cats")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCat)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Fluffy"));

        verify(catService).saveCat(any(Cat.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCat_WhenServiceThrowsException_ShouldReturnBadRequest() throws Exception {
        // Given
        when(catService.saveCat(any(Cat.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(post("/api/cats")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCat)))
                .andExpect(status().isBadRequest());

        verify(catService).saveCat(any(Cat.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCatStatus_WithValidData_ShouldUpdateStatus() throws Exception {
        // Given
        testCat.setStatus(CatStatus.ADOPTED);
        when(catService.updateCatStatus(1L, CatStatus.ADOPTED)).thenReturn(testCat);
        
        StatusUpdateRequestDto request = new StatusUpdateRequestDto();
        request.setStatus(CatStatus.ADOPTED);

        // When & Then
        mockMvc.perform(put("/api/cats/1/status")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ADOPTED"));

        verify(catService).updateCatStatus(1L, CatStatus.ADOPTED);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCatStatus_WhenServiceThrowsException_ShouldReturnBadRequest() throws Exception {
        // Given
        when(catService.updateCatStatus(1L, CatStatus.ADOPTED))
            .thenThrow(new RuntimeException("Cat not found"));
        
        StatusUpdateRequestDto request = new StatusUpdateRequestDto();
        request.setStatus(CatStatus.ADOPTED);

        // When & Then
        mockMvc.perform(put("/api/cats/1/status")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(catService).updateCatStatus(1L, CatStatus.ADOPTED);
    }

    @Test
    @WithMockUser
    void createCat_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/cats")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCat)))
                .andExpect(status().isForbidden());

        verify(catService, never()).saveCat(any());
    }
}
