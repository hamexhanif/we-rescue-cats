package io.werescuecats.backend.controller;

import io.werescuecats.backend.entity.Breed;
import io.werescuecats.backend.service.BreedService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BreedController.class)
@WithMockUser
class BreedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BreedService breedService;

    @Test
    void getAllBreeds_WithPagination_ReturnsPagedBreedList() throws Exception {
        Breed breed = createTestBreed("siam", "Siamese");
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Breed> page = new PageImpl<>(List.of(breed), pageable, 1);

        when(breedService.getAllBreeds(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/breeds")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value("siam"))
                .andExpect(jsonPath("$.content[0].name").value("Siamese"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.number").value(0));

        verify(breedService).getAllBreeds(any(Pageable.class));
    }


    @Test
    void getBreedById_ExistingBreed_ReturnsBreed() throws Exception {
        Breed breed = createTestBreed("siam", "Siamese");
        when(breedService.getBreedById("siam")).thenReturn(Optional.of(breed));

        mockMvc.perform(get("/api/breeds/siam"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("siam"))
                .andExpect(jsonPath("$.name").value("Siamese"));
    }

    @Test
    void getBreedById_NonExistingBreed_ReturnsNotFound() throws Exception {
        when(breedService.getBreedById("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/breeds/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchBreeds_WithParameters_ReturnsFilteredResults() throws Exception {
        List<Breed> breeds = Arrays.asList(createTestBreed("siam", "Siamese"));
        when(breedService.searchBreeds(eq("siamese"), any(), any(), any(), any()))
                .thenReturn(breeds);


        mockMvc.perform(get("/api/breeds/search")
                .param("name", "siamese")
                .param("childFriendly", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Siamese"));

        verify(breedService).searchBreeds("siamese", 4, null, null, null);
    }

    private Breed createTestBreed(String id, String name) {
        Breed breed = new Breed();
        breed.setId(id);
        breed.setName(name);
        breed.setDescription("Test breed");
        breed.setOrigin("Test Country");
        breed.setChildFriendly(4);
        breed.setDogFriendly(3);
        return breed;
    }
}
