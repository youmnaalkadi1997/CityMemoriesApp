package org.example.backend.service;

import org.example.backend.model.CityComment;
import org.example.backend.repository.CityCommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CityCommentService {

    private final CityCommentRepository cityCommentRepository;
    public CityCommentService(CityCommentRepository cityCommentRepository) {
        this.cityCommentRepository = cityCommentRepository;
    }

    public List<CityComment> allComments(String cityName) {
        return cityCommentRepository.findByCityNameIgnoreCase(cityName);
    }
}
