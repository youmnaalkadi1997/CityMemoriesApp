package org.example.backend.service;

import org.example.backend.model.AppUser;
import org.example.backend.model.CityComment;
import org.example.backend.repository.AppUserRepository;
import org.example.backend.repository.CityCommentRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FavouriteCitiesService {

    private final AppUserRepository appUserRepository;

    private final CityCommentRepository cityCommentRepository;

    public FavouriteCitiesService(AppUserRepository appUserRepository,  CityCommentRepository cityCommentRepository) {
        this.appUserRepository = appUserRepository;
        this.cityCommentRepository = cityCommentRepository;
    }

    public List<String> getFavouriteList(String username) {
        AppUser user = appUserRepository.findByUsername(username);
        return user.getFavoriteCities() != null ? user.getFavoriteCities() : new ArrayList<>();
    }

    public AppUser addFavoriteCity(Map<String, String> body) {
        String username = body.get("username");
        String cityName = body.get("cityName");

        AppUser user = appUserRepository.findByUsername(username);
        if (user == null) {
            throw new NoSuchElementException("Kein User gefunden für: " + username);
        }
        if (user.getFavoriteCities() == null) {
            user.setFavoriteCities(new ArrayList<>());
        }
        if (!user.getFavoriteCities().contains(cityName)) {
            user.getFavoriteCities().add(cityName);
            appUserRepository.save(user);
        }
        return user;
    }

    public AppUser deleteFromFavourits(String cityName, String username) {
        AppUser user = Optional.ofNullable(appUserRepository.findByUsername(username))
                .orElseThrow(() -> new NoSuchElementException("Kein User gefunden für: " + username));

        if (user.getFavoriteCities() != null) {
            user.getFavoriteCities().removeIf(city -> city.equals(cityName));
            appUserRepository.save(user);
        }
        return user;
    }


    public List<Map<String, Object>> getMostPopularCities(int limit) {
        List<AppUser> users = appUserRepository.findAll();
        List<CityComment> comments = cityCommentRepository.findAll();

        Map<String, Long> cityCountMap = users.stream()
                .filter(user -> user.getFavoriteCities() != null)
                .flatMap(user -> user.getFavoriteCities().stream())
                .collect(Collectors.groupingBy(city -> city, Collectors.counting()));

        Map<String, List<CityComment>> commentsByCity = comments.stream()
                .collect(Collectors.groupingBy(CityComment::getCityName));

        return cityCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    String cityName = entry.getKey();
                    long favoritesCount = entry.getValue();

                    List<CityComment> cityComments = commentsByCity.getOrDefault(cityName, List.of());

                    Optional<String> firstCommentPhoto = cityComments.stream()
                            .filter(c -> c.getImageUrl() != null)
                            .map(CityComment::getImageUrl)
                            .findFirst();

                    long commentsCount = cityComments.size();

                    Map<String, Object> cityMap = new HashMap<>();
                    cityMap.put("cityName", cityName);
                    cityMap.put("favoritesCount", favoritesCount);
                    cityMap.put("firstCommentPhoto", firstCommentPhoto.orElse(null));
                    cityMap.put("commentsCount", commentsCount);

                    return cityMap;
                })
                .toList();
    }


    public List<String> getSearchHistory(String username) {
        AppUser user = Optional.ofNullable(appUserRepository.findByUsername(username))
                .orElseThrow(() -> new NoSuchElementException("Kein User gefunden für: " + username));

        return Optional.ofNullable(user.getSearchHistory()).orElse(new ArrayList<>());
    }

    public List<String> addSearchEntry(String username, String cityName) {
        AppUser user = Optional.ofNullable(appUserRepository.findByUsername(username))
                .orElseThrow(() -> new NoSuchElementException("Kein User gefunden für: " + username));

        List<String> history = Optional.ofNullable(user.getSearchHistory())
                .orElse(new ArrayList<>());

        history.remove(cityName);
        history.add(0, cityName);

        if (history.size() > 10) {
            history = new ArrayList<>(history.subList(0, 10));
        }
        user.setSearchHistory(history);
        appUserRepository.save(user);
        return history;
    }

}