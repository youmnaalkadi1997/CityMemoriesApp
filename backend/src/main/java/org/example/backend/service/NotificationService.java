package org.example.backend.service;

import org.example.backend.model.Notification;
import org.example.backend.repository.NotificationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository,
                               SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public Notification createNotification(String recipient, String actor, String type,
                                           String targetCity, String commentId, String replyId) {
        Notification n = Notification.builder()
                .id(UUID.randomUUID().toString())
                .username(recipient)
                .actor(actor)
                .type(type)
                .targetCity(targetCity)
                .commentId(commentId)
                .replyId(replyId)
                .message(buildMessage(actor, type))
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(n);

        if (messagingTemplate != null) {
            messagingTemplate.convertAndSendToUser(recipient, "/queue/notifications", saved);
        }

        return saved;
    }

    private String buildMessage(String actor, String type) {
        if ("REPLY".equals(type)) return actor + " hat auf dein Kommentar geantwortet";
        if ("LIKE".equals(type)) return actor + " hat auf dein Kommentar reagiert";
        return actor + " hat etwas gemacht";
    }

    public List<Notification> getNotifications(String username) {
        return notificationRepository.findByUsernameOrderByCreatedAtDesc(username);
    }

    public long countUnread(String username) {
        return notificationRepository.countByUsernameAndReadFalse(username);
    }

    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    public void markAllAsRead(String username) {
        List<Notification> list = notificationRepository.findByUsernameAndReadFalseOrderByCreatedAtDesc(username);
        list.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(list);
    }
}
