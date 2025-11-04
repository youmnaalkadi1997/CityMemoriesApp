package org.example.backend.controller;

import org.example.backend.model.Notification;
import org.example.backend.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public List<Notification> getAll(@RequestParam String username) {
        return notificationService.getNotifications(username);
    }

    @GetMapping("/notifications/count")
    public long countUnread(@RequestParam String username) {
        return notificationService.countUnread(username);
    }

    @PostMapping("/notifications/{id}/read")
    public void markRead(@PathVariable String id) {
        notificationService.markAsRead(id);
    }

    @PostMapping("/notifications/readAll")
    public void markAllRead(@RequestParam String username) {
        notificationService.markAllAsRead(username);
    }
}
