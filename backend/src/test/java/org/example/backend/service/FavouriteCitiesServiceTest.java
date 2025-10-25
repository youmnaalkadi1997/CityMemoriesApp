package org.example.backend.service;

import org.example.backend.security.AppUser;
import org.example.backend.security.AppUserRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FavouriteCitiesServiceTest {

    @Test
    void getFavouriteList() {
        AppUserRepository mockRepo = mock(AppUserRepository.class);
        AppUser user = AppUser.builder()
                .username("youmna")
                .favoriteCities(List.of("Berlin", "Paris"))
                .build();

        when(mockRepo.findByUsername("youmna")).thenReturn(user);

        FavouriteCitiesService service = new FavouriteCitiesService(mockRepo);
        List<String> cities = service.getFavouriteList("youmna");

        assertThat(cities).containsExactly("Berlin", "Paris");
        verify(mockRepo).findByUsername("youmna");
    }

    @Test
    void addFavoriteCity() {
        AppUserRepository mockRepo = mock(AppUserRepository.class);
        AppUser user = AppUser.builder()
                .username("youmna")
                .favoriteCities(new ArrayList<>())
                .build();

        when(mockRepo.findByUsername("youmna")).thenReturn(user);
        when(mockRepo.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        FavouriteCitiesService service = new FavouriteCitiesService(mockRepo);
        AppUser updatedUser = service.addFavoriteCity(Map.of(
                "username", "youmna",
                "cityName", "Berlin"
        ));
        assertThat(updatedUser.getFavoriteCities()).containsExactly("Berlin");
        verify(mockRepo).save(user);
    }

    @Test
    void deleteFromFavourits() {
        AppUserRepository mockRepo = mock(AppUserRepository.class);
        AppUser user = AppUser.builder()
                .username("youmna")
                .favoriteCities(new ArrayList<>(List.of("Berlin", "Paris")))
                .build();

        when(mockRepo.findByUsername("youmna")).thenReturn(user);
        when(mockRepo.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        FavouriteCitiesService service = new FavouriteCitiesService(mockRepo);
        AppUser updatedUser = service.deleteFromFavourits("Berlin", "youmna");
        assertThat(updatedUser.getFavoriteCities()).containsExactly("Paris");
        verify(mockRepo).save(user);
    }
}