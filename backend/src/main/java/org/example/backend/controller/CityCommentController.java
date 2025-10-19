package org.example.backend.controller;

import jakarta.validation.Valid;
import org.example.backend.model.CityComment;
import org.example.backend.model.CityCommentDTO;
import org.example.backend.service.CityCommentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
public class CityCommentController {

    private final CityCommentService cityCommentService;

    public CityCommentController(CityCommentService cityCommentService) {
        this.cityCommentService = cityCommentService;
    }

    @GetMapping("/comment/{cityName}")
    public List<CityComment> getCommentsByCity(@PathVariable String cityName) {
        return  cityCommentService.allComments(cityName);
    }

    @PostMapping("/addcomment")
    public CityComment addComment(@Valid @RequestBody CityComment comment)  {
        return  cityCommentService.addComment(comment);
    }

    @PutMapping("/comment/{id}")
    public CityComment updateComment(@PathVariable String id, @RequestBody CityCommentDTO  cityCommentDTO) {
        CityComment cityComment = cityCommentService.updateComment(id, cityCommentDTO);
        if (cityComment != null) {
            return cityComment;
        } else {
            throw new NoSuchElementException("Kommentare mit ID: " + id + " nicht verfügbar");
        }
    }
}
