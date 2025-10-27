package org.example.backend.controller;

import org.example.backend.security.AppUser;
import org.example.backend.service.FavouriteCitiesService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FavouriteCitiesController {

    private final FavouriteCitiesService favouriteCitiesService;

    public FavouriteCitiesController (FavouriteCitiesService favouriteCitiesService) {
        this.favouriteCitiesService = favouriteCitiesService;
    }

    @GetMapping("/favorites")
    public List<String> getFavoriteCities(@RequestParam String username) {
        return favouriteCitiesService.getFavouriteList(username);
    }

    @PostMapping("/addToFavorites")
    public AppUser addFavoriteCity(@RequestBody Map<String, String> body) {

        return favouriteCitiesService.addFavoriteCity(body);
    }

    @DeleteMapping("/deleteFromFav/{cityName}")
    public AppUser removeFavoriteCity(@PathVariable String cityName, @RequestParam String username) {
        return favouriteCitiesService.deleteFromFavourits(cityName,username);
    }

    @GetMapping("/mostPopularCities")
    public List<Map<String, Object>> getPopularCities() {
        return favouriteCitiesService.getMostPopularCities(10);
    }
}
