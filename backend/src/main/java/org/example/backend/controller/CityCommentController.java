package org.example.backend.controller;

import jakarta.validation.Valid;
import org.example.backend.model.CityComment;
import org.example.backend.model.CityCommentDTO;
import org.example.backend.model.ReplyDTO;
import org.example.backend.service.CityCommentService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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

    @GetMapping("/comment/getId/{id}")
    public CityComment getCommentsById(@PathVariable String id) {
        Optional<CityComment> cityCommentOptional =  cityCommentService.getCommentById(id);
        if(cityCommentOptional.isPresent()){
            return cityCommentOptional.get();
        }
        throw new NoSuchElementException("Kommentar mit ID: " + id + " nicht verfügbar");
    }

    @PostMapping("/addcomment")
    public CityComment addComment(
            @Valid @RequestPart("data") CityComment comment,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        return cityCommentService.addCommentWithImage(comment, file);
    }

    @PutMapping("/comment/{id}")
    public CityComment updateComment(
            @PathVariable String id,
            @RequestPart("data") CityCommentDTO cityCommentDTO,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        CityComment cityComment = cityCommentService.updateComment(id, cityCommentDTO,file);
        if (cityComment != null) {
            return cityComment;
        } else {
            throw new NoSuchElementException("Kommentare mit ID: " + id + " nicht verfügbar");
        }
    }

    @DeleteMapping("/comment/{id}")
    public void deleteComment(@PathVariable String id){
        cityCommentService.deleteCommentById(id);
    }

    @PostMapping("/comment/{commentId}/like")
    public CityComment likeComment(@PathVariable String commentId,
                                   @RequestParam String username) {
        return cityCommentService.toggleLike(commentId, username);
    }
    @PostMapping("/comment/{commentId}/reply")
    public CityComment addReply(
            @PathVariable String commentId,
            @Valid @RequestBody ReplyDTO replyRequest
    ) {
        return cityCommentService.addReply(commentId, replyRequest);
    }
    @DeleteMapping("/comment/{commentId}/reply/{replyId}")
    public CityComment deleteReply(
            @PathVariable String commentId,
            @PathVariable String replyId,
            @RequestParam String username) {
        return cityCommentService.deleteReply(commentId, replyId, username);
    }
}
