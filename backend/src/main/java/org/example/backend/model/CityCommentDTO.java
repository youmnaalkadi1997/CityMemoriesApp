package org.example.backend.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class CityCommentDTO {
    @NotBlank(message = "Kommentar darf nicht leer sein")
    @Size(max = 500, message = "Kommentar darf maximal 500 Zeichen lang sein")
    private String comment;
    private String imageUrl;
    private LocalDateTime updatedAt;
}
