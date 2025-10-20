package org.example.backend.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class ErrorMessage {
    private String message;
    private int status;
    private LocalDateTime timestamp;
}
