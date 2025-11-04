package org.example.backend.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class Reply {
    private String id;
    private String username;
    private String reply;
    private LocalDateTime createdAt;
}
