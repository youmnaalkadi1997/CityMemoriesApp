package org.example.backend.service;

import org.example.backend.security.AppUser;
import org.example.backend.security.AppUserRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FavouriteCitiesService {

    private final AppUserRepository appUserRepository;


    public FavouriteCitiesService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public List<String> getFavouriteList(String username) {
        AppUser user = appUserRepository.findByUsername(username);
        return user.getFavoriteCities() != null ? user.getFavoriteCities() : new ArrayList<>();
    }

    public AppUser addFavoriteCity(Map<String, String> body){

        String username = body.get("username");
        String cityName = body.get("cityName");
        AppUser user = appUserRepository.findByUsername(username);
        if (user == null) {
            throw new NoSuchElementException("Kein User gefunden für:" + username);
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

   public AppUser deleteFromFavourits (String cityName, String username){
       AppUser user = appUserRepository.findByUsername(username);
       if (user == null) {
           throw new NoSuchElementException("Kein User gefunden für:" + username);
       }
       if (user.getFavoriteCities() != null&& user.getFavoriteCities().contains(cityName)) {
           user.getFavoriteCities().remove(cityName);
           appUserRepository.save(user);
       }
       return user;
   }

    public List<Map<String, Object>> getMostPopularCities(int limit) {
        List<AppUser> users = appUserRepository.findAll();

        Map<String, Long> cityCountMap = users.stream()
                .filter(user -> user.getFavoriteCities() != null)
                .flatMap(user -> user.getFavoriteCities().stream())
                .collect(Collectors.groupingBy(city -> city, Collectors.counting()));

        return cityCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> cityMap = new HashMap<>();
                    cityMap.put("cityName", entry.getKey());
                    cityMap.put("favoritesCount", entry.getValue());
                    return cityMap;
                })
                .toList();
    }
}