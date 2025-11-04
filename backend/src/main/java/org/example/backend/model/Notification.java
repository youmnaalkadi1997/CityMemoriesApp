package org.example.backend.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class Notification {
    private String id;
    private String username;
    private String actor;
    private String type;
    private String message;
    private String targetCity;
    private String commentId;
    private String replyId;
    private boolean read ;
    private LocalDateTime createdAt;
}
