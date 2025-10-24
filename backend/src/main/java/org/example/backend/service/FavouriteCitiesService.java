package org.example.backend.service;

import org.example.backend.security.AppUser;
import org.example.backend.security.AppUserRepository;
import org.springframework.stereotype.Service;

import java.util.*;

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
}
