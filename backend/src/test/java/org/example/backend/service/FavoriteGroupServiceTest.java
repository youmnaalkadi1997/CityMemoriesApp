package org.example.backend.service;

import org.example.backend.model.AppUser;
import org.example.backend.model.FavoriteGroup;
import org.example.backend.repository.AppUserRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

class FavoriteGroupServiceTest {

    @Test
    void getGroups_shouldReturnGroups_whenUserHasGroups() {
        AppUserRepository mockRepo = mock(AppUserRepository.class);
        FavoriteGroup group = FavoriteGroup.builder().name("Group1").build();
        AppUser user = AppUser.builder()
                .username("user1")
                .favoriteGroups(new ArrayList<>(List.of(group)))
                .build();
        when(mockRepo.findByUsername("user1")).thenReturn(user);

        FavoriteGroupService service = new FavoriteGroupService(mockRepo);

        List<FavoriteGroup> groups = service.getGroups("user1");

        assertThat(groups).hasSize(1).contains(group);
    }

    @Test
    void getGroups_shouldReturnEmptyList_whenUserHasNoGroups() {
        AppUserRepository mockRepo = mock(AppUserRepository.class);
        AppUser user = AppUser.builder()
                .username("user1")
                .favoriteGroups(null)
                .build();
        when(mockRepo.findByUsername("user1")).thenReturn(user);

        FavoriteGroupService service = new FavoriteGroupService(mockRepo);

        List<FavoriteGroup> groups = service.getGroups("user1");

        assertThat(groups).isEmpty();
    }

    @Test
    void getGroups_shouldThrowException_whenUserNotFound() {
        AppUserRepository mockRepo = mock(AppUserRepository.class);
        when(mockRepo.findByUsername("user1")).thenReturn(null);

        FavoriteGroupService service = new FavoriteGroupService(mockRepo);

        assertThatThrownBy(() -> service.getGroups("user1"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Kein User gefunden für: user1");
    }

    @Test
    void addGroup_shouldAddNewGroup() {
        AppUserRepository mockRepo = mock(AppUserRepository.class);
        AppUser user = AppUser.builder()
                .username("user1")
                .favoriteGroups(null)
                .build();
        when(mockRepo.findByUsername("user1")).thenReturn(user);
        when(mockRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        FavoriteGroupService service = new FavoriteGroupService(mockRepo);

        FavoriteGroup newGroup = service.addGroup("user1", "Group1");

        assertThat(newGroup.getName()).isEqualTo("Group1");
        assertThat(user.getFavoriteGroups()).contains(newGroup);
        verify(mockRepo).save(user);
    }

    @Test
    void addGroup_shouldThrowException_whenUserNotFound() {
        AppUserRepository mockRepo = mock(AppUserRepository.class);
        when(mockRepo.findByUsername("user1")).thenReturn(null);

        FavoriteGroupService service = new FavoriteGroupService(mockRepo);

        assertThatThrownBy(() -> service.addGroup("user1", "Group1"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Kein User gefunden für: user1");
    }

    @Test
    void deleteGroup_shouldRemoveGroupIfExists() {
        AppUserRepository mockRepo = mock(AppUserRepository.class);
        FavoriteGroup group = FavoriteGroup.builder().name("Group1").build();
        AppUser user = AppUser.builder()
                .username("user1")
                .favoriteGroups(new ArrayList<>(List.of(group)))
                .build();
        when(mockRepo.findByUsername("user1")).thenReturn(user);

        FavoriteGroupService service = new FavoriteGroupService(mockRepo);

        service.deleteGroup("user1", "Group1");

        assertThat(user.getFavoriteGroups()).doesNotContain(group);
        verify(mockRepo).save(user);
    }

    @Test
    void deleteGroup_shouldDoNothingIfGroupNotExists() {
        AppUserRepository mockRepo = mock(AppUserRepository.class);
        AppUser user = AppUser.builder()
                .username("user1")
                .favoriteGroups(new ArrayList<>())
                .build();
        when(mockRepo.findByUsername("user1")).thenReturn(user);

        FavoriteGroupService service = new FavoriteGroupService(mockRepo);

        service.deleteGroup("user1", "Group1");

        assertThat(user.getFavoriteGroups()).isEmpty();
        verify(mockRepo, never()).save(user);
    }

    @Test
    void deleteGroup_shouldThrowException_whenUserNotFound() {
        AppUserRepository mockRepo = mock(AppUserRepository.class);
        when(mockRepo.findByUsername("user1")).thenReturn(null);

        FavoriteGroupService service = new FavoriteGroupService(mockRepo);

        assertThatThrownBy(() -> service.deleteGroup("user1", "Group1"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Kein User gefunden für: user1");
    }

    @Test
    void addCityToGroup_shouldAddCityIfNotExists() {
        AppUserRepository mockRepo = mock(AppUserRepository.class);
        FavoriteGroup group = FavoriteGroup.builder()
                .name("Group1")
                .cities(new ArrayList<>())
                .build();
        AppUser user = AppUser.builder()
                .username("user1")
                .favoriteGroups(new ArrayList<>(List.of(group)))
                .build();
        when(mockRepo.findByUsername("user1")).thenReturn(user);
        when(mockRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        FavoriteGroupService service = new FavoriteGroupService(mockRepo);

        FavoriteGroup updatedGroup = service.addCityToGroup("user1", "Group1", "Berlin");

        assertThat(updatedGroup.getCities()).contains("Berlin");
        verify(mockRepo).save(user);
    }

    @Test
    void addCityToGroup_shouldNotAddCityIfAlreadyExists() {
        AppUserRepository mockRepo = mock(AppUserRepository.class);
        FavoriteGroup group = FavoriteGroup.builder()
                .name("Group1")
                .cities(new ArrayList<>(List.of("Berlin")))
                .build();
        AppUser user = AppUser.builder()
                .username("user1")
                .favoriteGroups(new ArrayList<>(List.of(group)))
                .build();
        when(mockRepo.findByUsername("user1")).thenReturn(user);

        FavoriteGroupService service = new FavoriteGroupService(mockRepo);

        FavoriteGroup updatedGroup = service.addCityToGroup("user1", "Group1", "Berlin");

        assertThat(updatedGroup.getCities()).containsExactly("Berlin");
        verify(mockRepo, never()).save(user);
    }

    @Test
    void addCityToGroup_shouldThrowException_whenGroupNotFound() {
        AppUserRepository mockRepo = mock(AppUserRepository.class);
        AppUser user = AppUser.builder()
                .username("user1")
                .favoriteGroups(new ArrayList<>())
                .build();
        when(mockRepo.findByUsername("user1")).thenReturn(user);

        FavoriteGroupService service = new FavoriteGroupService(mockRepo);

        assertThatThrownBy(() -> service.addCityToGroup("user1", "Group1", "Berlin"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Gruppe nicht gefunden: Group1");
    }

    @Test
    void addCityToGroup_shouldThrowException_whenUserNotFound() {
        AppUserRepository mockRepo = mock(AppUserRepository.class);
        when(mockRepo.findByUsername("user1")).thenReturn(null);

        FavoriteGroupService service = new FavoriteGroupService(mockRepo);

        assertThatThrownBy(() -> service.addCityToGroup("user1", "Group1", "Berlin"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Kein User gefunden für: user1");
    }





}