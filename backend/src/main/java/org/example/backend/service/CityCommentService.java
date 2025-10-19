package org.example.backend.service;

import org.example.backend.model.CityComment;
import org.example.backend.model.CityCommentDTO;
import org.example.backend.repository.CityCommentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
public class CityCommentService {

    private final CityCommentRepository cityCommentRepository;
    public CityCommentService(CityCommentRepository cityCommentRepository) {
        this.cityCommentRepository = cityCommentRepository;
    }

    public List<CityComment> allComments(String cityName) {
        List<CityComment> comments = cityCommentRepository.findByCityNameIgnoreCase(cityName);

        if (comments.isEmpty()) {
            throw new NoSuchElementException("Keine Kommentare gefunden für: " + cityName);
        }

        return comments;
    }

    public CityComment addComment(CityComment comment) {

        String id = UUID.randomUUID().toString();
        CityComment newComment = CityComment.builder()
                .id(id)
                .cityName(comment.getCityName())
                .username(comment.getUsername())
                .comment(comment.getComment())
                .createdAt(LocalDateTime.now())
                .build();
        return cityCommentRepository.save(newComment);
    }

    public CityComment updateComment(String id, CityCommentDTO cityCommentDTO) {
        Optional<CityComment> existingCommentOpt = cityCommentRepository.findById(id);
        if (existingCommentOpt.isPresent()) {
            CityComment existingComment = existingCommentOpt.get();
            existingComment.setComment(cityCommentDTO.getComment());
            existingComment.setUpdatedAt(LocalDateTime.now());
            return cityCommentRepository.save(existingComment);
        } else {
            return null;
        }
    }
}
