package org.example.backend.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ReplyDTO {

    @NotNull(message = "Username darf nicht leer sein")
    private String username;

    @NotBlank(message = "Antwort darf nicht leer sein")
    private String reply;
}
