package org.example.backend.controller;

import org.example.backend.model.FavoriteGroup;
import org.example.backend.service.FavoriteGroupService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FavoriteGroupController {
    private final FavoriteGroupService favoriteGroupService;

    public FavoriteGroupController(FavoriteGroupService favoriteGroupService) {
        this.favoriteGroupService = favoriteGroupService;
    }

    @GetMapping("/groups")
    public List<FavoriteGroup> getGroups(@RequestParam String username) {
        return favoriteGroupService.getGroups(username);
    }

    @PostMapping("/groups")
    public FavoriteGroup addGroup(@RequestParam String username, @RequestParam String groupName) {
        return favoriteGroupService.addGroup(username, groupName);
    }

    @DeleteMapping("/groups")
    public void deleteGroup(@RequestParam String username, @RequestParam String groupName) {
        favoriteGroupService.deleteGroup(username, groupName);
    }

    @PostMapping("/addCity")
    public FavoriteGroup addCityToGroup(@RequestParam String username,
                                        @RequestParam String groupName,
                                        @RequestParam String city) {
        return favoriteGroupService.addCityToGroup(username, groupName, city);
    }
}
