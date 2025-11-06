package org.example.backend.service;

import org.example.backend.model.Notification;
import org.example.backend.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;


class NotificationServiceTest {
    @Test
    void createNotification_shouldSaveNotification() {
        NotificationRepository mockRepo = mock(NotificationRepository.class);

        when(mockRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationService service = new NotificationService(mockRepo, null);

        Notification n = service.createNotification("user1", "actor1", "LIKE", "Berlin", "c1", null);

        assertThat(n.getUsername()).isEqualTo("user1");
        assertThat(n.getActor()).isEqualTo("actor1");
        assertThat(n.getType()).isEqualTo("LIKE");
        verify(mockRepo).save(n);
    }

    @Test
    void createNotification_shouldSendMessage_whenTemplatePresent() {
        NotificationRepository mockRepo = mock(NotificationRepository.class);
        SimpMessagingTemplate mockTemplate = mock(SimpMessagingTemplate.class);

        when(mockRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationService service = new NotificationService(mockRepo, mockTemplate);

        Notification n = service.createNotification("user1", "actor1", "REPLY", "Berlin", "c1", "r1");

        verify(mockRepo).save(n);
        verify(mockTemplate).convertAndSendToUser(eq("user1"), eq("/queue/notifications"), eq(n));
    }
    @Test
    void getNotifications_shouldReturnList() {
        NotificationRepository mockRepo = mock(NotificationRepository.class);
        List<Notification> list = List.of(Notification.builder().id("1").username("user1").build());
        when(mockRepo.findByUsernameOrderByCreatedAtDesc("user1")).thenReturn(list);

        NotificationService service = new NotificationService(mockRepo, null);

        List<Notification> result = service.getNotifications("user1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("user1");
        verify(mockRepo).findByUsernameOrderByCreatedAtDesc("user1");
    }
    @Test
    void countUnread_shouldReturnCorrectNumber() {
        NotificationRepository mockRepo = mock(NotificationRepository.class);
        when(mockRepo.countByUsernameAndReadFalse("user1")).thenReturn(5L);

        NotificationService service = new NotificationService(mockRepo, null);

        long count = service.countUnread("user1");

        assertThat(count).isEqualTo(5L);
        verify(mockRepo).countByUsernameAndReadFalse("user1");
    }
    @Test
    void markAsRead_shouldMarkNotificationAsRead() {
        NotificationRepository mockRepo = mock(NotificationRepository.class);
        Notification n = Notification.builder().id("n1").read(false).build();
        when(mockRepo.findById("n1")).thenReturn(Optional.of(n));

        NotificationService service = new NotificationService(mockRepo, null);

        service.markAsRead("n1");

        assertThat(n.isRead()).isTrue();
        verify(mockRepo).save(n);
    }

    @Test
    void markAsRead_shouldDoNothingIfNotificationNotFound() {
        NotificationRepository mockRepo = mock(NotificationRepository.class);
        when(mockRepo.findById("n1")).thenReturn(Optional.empty());

        NotificationService service = new NotificationService(mockRepo, null);

        service.markAsRead("n1");

        verify(mockRepo, never()).save(any());
    }
    @Test
    void markAllAsRead_shouldMarkAllNotificationsAsRead() {
        NotificationRepository mockRepo = mock(NotificationRepository.class);
        Notification n1 = Notification.builder().id("1").read(false).build();
        Notification n2 = Notification.builder().id("2").read(false).build();
        List<Notification> list = new ArrayList<>(List.of(n1, n2));

        when(mockRepo.findByUsernameAndReadFalseOrderByCreatedAtDesc("user1")).thenReturn(list);

        NotificationService service = new NotificationService(mockRepo, null);

        service.markAllAsRead("user1");

        assertThat(list).allMatch(Notification::isRead);
        verify(mockRepo).saveAll(list);
    }






}