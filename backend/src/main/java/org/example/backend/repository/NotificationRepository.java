package org.example.backend.repository;

import org.example.backend.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUsernameOrderByCreatedAtDesc(String username);
    long countByUsernameAndReadFalse(String username);
    List<Notification> findByUsernameAndReadFalseOrderByCreatedAtDesc(String username);
}
