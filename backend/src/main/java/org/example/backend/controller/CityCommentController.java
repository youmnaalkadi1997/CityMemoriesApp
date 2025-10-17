package org.example.backend.controller;

import org.example.backend.model.CityComment;
import org.example.backend.service.CityCommentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CityCommentController {

    private final CityCommentService cityCommentService;

    public CityCommentController(CityCommentService cityCommentService) {
        this.cityCommentService = cityCommentService;
    }

    @GetMapping("/city/{cityName}")
    public List<CityComment> getCommentsByCity(@PathVariable String cityName) {
        return  cityCommentService.allComments(cityName);
    }
}
