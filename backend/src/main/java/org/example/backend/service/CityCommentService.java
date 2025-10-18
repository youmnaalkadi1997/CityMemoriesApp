package org.example.backend.service;

import org.example.backend.model.CityComment;
import org.example.backend.repository.CityCommentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CityCommentService {

    private final CityCommentRepository cityCommentRepository;
    public CityCommentService(CityCommentRepository cityCommentRepository) {
        this.cityCommentRepository = cityCommentRepository;
    }

    public List<CityComment> allComments(String cityName) {
        return cityCommentRepository.findByCityNameIgnoreCase(cityName);
    }

    public CityComment addComment(CityComment comment) {
        String id = UUID.randomUUID().toString();
        CityComment newComment = CityComment.builder()
                .id(id)
                .cityName(comment.getCityName())
                .username(comment.getUsername())
                .comment(comment.getComment())
                .imageUrl(comment.getImageUrl())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return cityCommentRepository.save(newComment);
    }
}
