package org.example.backend.service;

import org.example.backend.model.AppUser;
import org.example.backend.model.FavoriteGroup;
import org.example.backend.repository.AppUserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class FavoriteGroupService {
    private final AppUserRepository appUserRepository;

    public FavoriteGroupService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public List<FavoriteGroup> getGroups(String username) {
        AppUser user = appUserRepository.findByUsername(username);
        if (user == null) throw new NoSuchElementException("Kein User gefunden für: " + username);
        return Optional.ofNullable(user.getFavoriteGroups()).orElse(new ArrayList<>());
    }

    public FavoriteGroup addGroup(String username, String groupName) {
        AppUser user = appUserRepository.findByUsername(username);
        if (user == null) throw new NoSuchElementException("Kein User gefunden für: " + username);

        List<FavoriteGroup> groups = Optional.ofNullable(user.getFavoriteGroups()).orElseGet(ArrayList::new);
        user.setFavoriteGroups(groups);

        FavoriteGroup newGroup = FavoriteGroup.builder()
                .name(groupName)
                .cities(new ArrayList<>())
                .build();

        groups.add(newGroup);
        appUserRepository.save(user);
        return newGroup;
    }


    public void deleteGroup(String username, String groupName) {
        AppUser user = appUserRepository.findByUsername(username);
        if (user == null) throw new NoSuchElementException("Kein User gefunden für: " + username);

        List<FavoriteGroup> groups = Optional.ofNullable(user.getFavoriteGroups()).orElse(new ArrayList<>());
        boolean removed = groups.removeIf(g -> g.getName().equals(groupName));

        if (removed) {
            user.setFavoriteGroups(groups);
            appUserRepository.save(user);
        }
    }

    public FavoriteGroup addCityToGroup(String username, String groupName, String city) {
        AppUser user = appUserRepository.findByUsername(username);
        if (user == null) throw new NoSuchElementException("Kein User gefunden für: " + username);

        FavoriteGroup group = Optional.ofNullable(user.getFavoriteGroups())
                .orElseThrow(() -> new NoSuchElementException("Keine Gruppen für User: " + username))
                .stream()
                .filter(g -> g.getName().equals(groupName))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Gruppe nicht gefunden: " + groupName));

        List<String> cities = Optional.ofNullable(group.getCities()).orElse(new ArrayList<>());
        if (!cities.contains(city)) {
            cities.add(city);
            group.setCities(cities);
            appUserRepository.save(user);
        }

        return group;
    }
}
