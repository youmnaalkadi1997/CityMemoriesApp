package org.example.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.example.backend.model.CityComment;
import org.example.backend.model.CityCommentDTO;
import org.example.backend.model.Reply;
import org.example.backend.model.ReplyDTO;
import org.example.backend.repository.CityCommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CityCommentService {

    private final CityCommentRepository cityCommentRepository;
    private final NotificationService notificationService;
    private final Cloudinary cloudinary;

    public CityCommentService(CityCommentRepository cityCommentRepository,  NotificationService notificationService, Cloudinary cloudinary) {
        this.cityCommentRepository = cityCommentRepository;
        this.notificationService = notificationService;
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


    public CityComment updateComment(String id, CityCommentDTO dto, MultipartFile file) throws IOException {
        CityComment existing = cityCommentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Kommentar nicht gefunden: " + id));

        existing.setComment(dto.getComment());
        existing.setUpdatedAt(LocalDateTime.now());

        if (file != null && !file.isEmpty()) {
            String newImageUrl = uploadImageToCloudinary(file);
            existing.setImageUrl(newImageUrl);
        }

        return cityCommentRepository.save(existing);
    }

    private String uploadImageToCloudinary(MultipartFile file) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("folder", "city-comments")
        );
        return (String) uploadResult.get("secure_url");
    }

    public void deleteCommentById(String id) {
        if (cityCommentRepository.existsById(id)) {
            cityCommentRepository.deleteById(id);
        } else {
            throw new NoSuchElementException("Keine Kommentare gefunden für: " + id);
        }
    }
    public CityComment toggleLike(String commentId, String username) {
        CityComment comment = cityCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Kommentar nicht gefunden"));

        if (comment.getLikedByUsers() == null) {
            comment.setLikedByUsers(new ArrayList<>());
        }
        int likes = Optional.ofNullable(comment.getLikesCount()).orElse(0);
        comment.setLikesCount(likes);

        boolean isLiked = comment.getLikedByUsers().contains(username);

        if (isLiked) {
            comment.getLikedByUsers().remove(username);
            comment.setLikesCount(comment.getLikesCount() - 1);
        } else {
            comment.getLikedByUsers().add(username);
            comment.setLikesCount(comment.getLikesCount() + 1);

            if (!comment.getUsername().equals(username)) {
                notificationService.createNotification(
                        comment.getUsername(),
                        username,
                        "LIKE",
                        comment.getCityName(),
                        comment.getId(),
                        null
                );
            }
        }
        return cityCommentRepository.save(comment);
    }

    public CityComment addReply(String commentId, ReplyDTO replyDTO) {
        CityComment comment = cityCommentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Kommentar mit ID " + commentId + " nicht gefunden."
                ));
        comment.setReplies(Optional.ofNullable(comment.getReplies()).orElse(new ArrayList<>()));

        Reply reply = Reply.builder()
                .id(UUID.randomUUID().toString())
                .username(replyDTO.getUsername())
                .reply(replyDTO.getReply())
                .createdAt(LocalDateTime.now())
                .build();

        comment.getReplies().add(reply);
        CityComment updatedComment = cityCommentRepository.save(comment);

        if (!comment.getUsername().equals(replyDTO.getUsername())) {
            notificationService.createNotification(
                    comment.getUsername(),
                    replyDTO.getUsername(),
                    "REPLY",
                    comment.getCityName(),
                    comment.getId(),
                    reply.getId()
            );
        }
        return updatedComment;
    }


    public CityComment deleteReply(String commentId, String replyId, String username) {
        CityComment comment = cityCommentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Kommentar nicht gefunden"));

        List<Reply> replies = Optional.ofNullable(comment.getReplies()).orElse(new ArrayList<>());
        boolean removed = replies.removeIf(r -> r.getId().equals(replyId) && r.getUsername().equals(username));

        if (!removed) {
            throw new NoSuchElementException("Antwort wurde nicht gefunden oder du hast keine Berechtigung sie zu löschen.");
        }
        comment.setReplies(replies);
        return cityCommentRepository.save(comment);
    }

}
