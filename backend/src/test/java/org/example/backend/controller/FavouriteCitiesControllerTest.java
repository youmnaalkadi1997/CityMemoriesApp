package org.example.backend.controller;

import org.example.backend.security.AppUser;
import org.example.backend.security.AppUserRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FavouriteCitiesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @BeforeEach
    void setup() {
        appUserRepository.deleteAll();
    }

    @Test
    @WithMockUser
    void getFavoriteCities() throws Exception {
        AppUser appUser = AppUser.builder().username("youmna").favoriteCities(List.of("Berlin", "Paris")).build();
        appUserRepository.save(appUser);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/favorites")
                        .param("username", "youmna"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        """
                                 ["Berlin", "Paris"]
                                 """
                ));
    }

    @Test
    @WithMockUser
    void addFavoriteCity() throws Exception {
        AppUser user = AppUser.builder()
                .username("youmna")
                .favoriteCities(new ArrayList<>())
                .build();
        appUserRepository.save(user);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/addToFavorites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "youmna",
                                  "cityName": "Berlin"
                                }
                             """))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.favoriteCities[0]").value("Berlin"));

        AppUser updatedUser = appUserRepository.findByUsername("youmna");
        assertThat(updatedUser.getFavoriteCities()).contains("Berlin");
    }

    @Test
    @WithMockUser
    void removeFavoriteCity() throws Exception {
        AppUser user = AppUser.builder()
                .username("youmna")
                .favoriteCities(new ArrayList<>(List.of("Berlin", "Paris")))
                .build();
        appUserRepository.save(user);

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/deleteFromFav/{cityName}", "Berlin")
                        .param("username", "youmna"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.favoriteCities").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.favoriteCities", Matchers.not(Matchers.hasItem("Berlin"))))
                .andExpect(MockMvcResultMatchers.jsonPath("$.favoriteCities[0]").value("Paris"));

        AppUser updatedUser = appUserRepository.findByUsername("youmna");
        assertThat(updatedUser.getFavoriteCities()).doesNotContain("Berlin");
        assertThat(updatedUser.getFavoriteCities()).contains("Paris");

    }
}