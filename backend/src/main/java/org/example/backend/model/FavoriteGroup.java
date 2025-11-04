package org.example.backend.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class FavoriteGroup {
    private String name;
    private List<String> cities;
}
