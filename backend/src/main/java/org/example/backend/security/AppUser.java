package org.example.backend.security;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class AppUser {
    private String id;
    private String username;
    private String email;
    private String role;
    private List<String> favoriteCities;
}
