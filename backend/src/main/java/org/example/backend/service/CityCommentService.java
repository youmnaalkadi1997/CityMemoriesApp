package org.example.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.example.backend.model.CityComment;
import org.example.backend.model.CityCommentDTO;
import org.example.backend.repository.CityCommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CityCommentService {

    private final CityCommentRepository cityCommentRepository;
    private final Cloudinary cloudinary;

    public CityCommentService(CityCommentRepository cityCommentRepository, Cloudinary cloudinary) {
        this.cityCommentRepository = cityCommentRepository;
        this.cloudinary = cloudinary;
    }

    public List<CityComment> allComments(String cityName) {
        List<CityComment> comments = cityCommentRepository.findByCityNameIgnoreCase(cityName);

        if (comments.isEmpty()) {
            throw new NoSuchElementException("Keine Kommentare gefunden für: " + cityName);
        }

        return comments;
    }

    public Optional<CityComment> getCommentById(String id) {
        return cityCommentRepository.findById(id);
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
                .build();
        return cityCommentRepository.save(newComment);
    }

    public CityComment addCommentWithImage(CityComment comment, MultipartFile file) throws IOException {
        String imageUrl = null;

        if (file != null && !file.isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", "city-comments")
            );
            imageUrl = (String) uploadResult.get("secure_url");
        }

        comment.setImageUrl(imageUrl);

        return addComment(comment);
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

    public void deleteCommentById(String id) {
        if (cityCommentRepository.existsById(id)) {
            cityCommentRepository.deleteById(id);
        } else {
            throw new NoSuchElementException("Keine Kommentare gefunden für: " + id);
        }
    }
}
