package org.example.backend.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class CityComment {

    private String id;
    @NotNull(message = "Stadtname darf nicht leer sein")
    private String cityName;
    @NotNull(message = "Benutzername darf nicht leer sein")
    private String username;
    @NotBlank(message = "Kommentar darf nicht leer sein")
    @Size(max = 500, message = "Kommentar darf maximal 500 Zeichen lang sein")
    private String comment;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer likesCount ;
    private List<String> likedByUsers;
    private List<Reply> replies;
}
