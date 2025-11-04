package org.example.backend.service;

import org.example.backend.model.AppUser;
import org.example.backend.model.CityComment;
import org.example.backend.repository.AppUserRepository;
import org.example.backend.repository.CityCommentRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class FavouriteCitiesServiceTest {

    @Test
    void getFavouriteList() {
        AppUserRepository mockRepo = mock(AppUserRepository.class);
        CityCommentRepository  mockRepo2 = mock(CityCommentRepository.class);
        AppUser user = AppUser.builder()
                .username("youmna")
                .favoriteCities(List.of("Berlin", "Paris"))
                .build();

        when(mockRepo.findByUsername("youmna")).thenReturn(user);

        FavouriteCitiesService service = new FavouriteCitiesService(mockRepo, mockRepo2);
        List<String> cities = service.getFavouriteList("youmna");

        assertThat(cities).containsExactly("Berlin", "Paris");
        verify(mockRepo).findByUsername("youmna");
    }

    @Test
    void addFavoriteCity() {
        AppUserRepository mockRepo = mock(AppUserRepository.class);
        CityCommentRepository  mockRepo2 = mock(CityCommentRepository.class);
        AppUser user = AppUser.builder()
                .username("youmna")
                .favoriteCities(new ArrayList<>())
                .build();

        when(mockRepo.findByUsername("youmna")).thenReturn(user);
        when(mockRepo.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        FavouriteCitiesService service = new FavouriteCitiesService(mockRepo, mockRepo2);
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
        CityCommentRepository  mockRepo2 = mock(CityCommentRepository.class);
        AppUser user = AppUser.builder()
                .username("youmna")
                .favoriteCities(new ArrayList<>(List.of("Berlin", "Paris")))
                .build();

        when(mockRepo.findByUsername("youmna")).thenReturn(user);
        when(mockRepo.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        FavouriteCitiesService service = new FavouriteCitiesService(mockRepo, mockRepo2);
        AppUser updatedUser = service.deleteFromFavourits("Berlin", "youmna");
        assertThat(updatedUser.getFavoriteCities()).containsExactly("Paris");
        verify(mockRepo).save(user);
    }

    @Test
    void getMostPopularCities_shouldReturnCorrectData() {
        AppUserRepository mockUserRepo = mock(AppUserRepository.class);
        CityCommentRepository mockCommentRepo = mock(CityCommentRepository.class);

        AppUser user1 = AppUser.builder()
                .favoriteCities(List.of("Berlin", "Paris"))
                .build();

        AppUser user2 = AppUser.builder()
                .favoriteCities(List.of("Berlin"))
                .build();

        when(mockUserRepo.findAll()).thenReturn(List.of(user1, user2));

        CityComment comment1 = CityComment.builder()
                .id("c1").cityName("Berlin").imageUrl("image1.jpg").build();
        CityComment comment2 = CityComment.builder()
                .id("c2").cityName("Berlin").imageUrl(null).build();
        CityComment comment3 = CityComment.builder()
                .id("c3").cityName("Paris").imageUrl("paris.jpg").build();

        when(mockCommentRepo.findAll()).thenReturn(List.of(comment1, comment2, comment3));

        FavouriteCitiesService service = new FavouriteCitiesService(mockUserRepo, mockCommentRepo);
        List<Map<String, Object>> result = service.getMostPopularCities(10);
        assertThat(result).hasSize(2);

        Map<String, Object> berlin = result.get(0);
        assertThat(berlin.get("cityName")).isEqualTo("Berlin");
        assertThat(berlin.get("favoritesCount")).isEqualTo(2L);
        assertThat(berlin.get("commentsCount")).isEqualTo(2L);
        assertThat(berlin.get("firstCommentPhoto")).isEqualTo("image1.jpg");

        Map<String, Object> paris = result.get(1);
        assertThat(paris.get("cityName")).isEqualTo("Paris");
        assertThat(paris.get("favoritesCount")).isEqualTo(1L);
        assertThat(paris.get("commentsCount")).isEqualTo(1L);
        assertThat(paris.get("firstCommentPhoto")).isEqualTo("paris.jpg");

        verify(mockUserRepo).findAll();
        verify(mockCommentRepo).findAll();
    }

    @Test
    void getSearchHistory_shouldReturnHistory_whenUserExists() {
        AppUserRepository mockUserRepo = mock(AppUserRepository.class);
        AppUser user = AppUser.builder()
                .username("user1")
                .searchHistory(new ArrayList<>(List.of("Berlin", "Paris")))
                .build();
        when(mockUserRepo.findByUsername("user1")).thenReturn(user);

        FavouriteCitiesService service = new FavouriteCitiesService(mockUserRepo, null);

        List<String> history = service.getSearchHistory("user1");

        assertThat(history).containsExactly("Berlin", "Paris");
    }

    @Test
    void getSearchHistory_shouldReturnEmptyList_whenHistoryIsNull() {
        AppUserRepository mockUserRepo = mock(AppUserRepository.class);
        AppUser user = AppUser.builder()
                .username("user1")
                .searchHistory(null)
                .build();
        when(mockUserRepo.findByUsername("user1")).thenReturn(user);

        FavouriteCitiesService service = new FavouriteCitiesService(mockUserRepo, null);

        List<String> history = service.getSearchHistory("user1");

        assertThat(history).isEmpty();
    }

    @Test
    void getSearchHistory_shouldThrowException_whenUserNotFound() {
        AppUserRepository mockUserRepo = mock(AppUserRepository.class);
        when(mockUserRepo.findByUsername("user1")).thenReturn(null);

        FavouriteCitiesService service = new FavouriteCitiesService(mockUserRepo, null);

        assertThatThrownBy(() -> service.getSearchHistory("user1"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Kein User gefunden für: user1");
    }

    @Test
    void addSearchEntry_shouldAddCityAtStart_whenCityIsNew() {
        AppUserRepository mockUserRepo = mock(AppUserRepository.class);
        AppUser user = AppUser.builder()
                .username("user1")
                .searchHistory(new ArrayList<>(List.of("Paris", "London")))
                .build();
        when(mockUserRepo.findByUsername("user1")).thenReturn(user);
        when(mockUserRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        FavouriteCitiesService service = new FavouriteCitiesService(mockUserRepo, null);

        List<String> updatedHistory = service.addSearchEntry("user1", "Berlin");

        assertThat(updatedHistory).containsExactly("Berlin", "Paris", "London");
        verify(mockUserRepo).save(user);
    }

    @Test
    void addSearchEntry_shouldMoveCityToStart_whenCityAlreadyExists() {
        AppUserRepository mockUserRepo = mock(AppUserRepository.class);
        AppUser user = AppUser.builder()
                .username("user1")
                .searchHistory(new ArrayList<>(List.of("Berlin", "Paris", "London")))
                .build();
        when(mockUserRepo.findByUsername("user1")).thenReturn(user);
        when(mockUserRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        FavouriteCitiesService service = new FavouriteCitiesService(mockUserRepo, null);

        List<String> updatedHistory = service.addSearchEntry("user1", "Paris");

        assertThat(updatedHistory).containsExactly("Paris", "Berlin", "London");
        verify(mockUserRepo).save(user);
    }

    @Test
    void addSearchEntry_shouldTrimHistory_whenMoreThan10Entries() {
        AppUserRepository mockUserRepo = mock(AppUserRepository.class);
        List<String> initialHistory = IntStream.range(0, 10).mapToObj(i -> "City" + i).collect(Collectors.toList());
        AppUser user = AppUser.builder()
                .username("user1")
                .searchHistory(new ArrayList<>(initialHistory))
                .build();
        when(mockUserRepo.findByUsername("user1")).thenReturn(user);
        when(mockUserRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        FavouriteCitiesService service = new FavouriteCitiesService(mockUserRepo, null);

        List<String> updatedHistory = service.addSearchEntry("user1", "NewCity");

        assertThat(updatedHistory).hasSize(10);
        assertThat(updatedHistory.get(0)).isEqualTo("NewCity");
        verify(mockUserRepo).save(user);
    }

    @Test
    void addSearchEntry_shouldThrowException_whenUserNotFound() {
        AppUserRepository mockUserRepo = mock(AppUserRepository.class);
        when(mockUserRepo.findByUsername("user1")).thenReturn(null);

        FavouriteCitiesService service = new FavouriteCitiesService(mockUserRepo, null);

        assertThatThrownBy(() -> service.addSearchEntry("user1", "Berlin"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Kein User gefunden für: user1");
    }




}