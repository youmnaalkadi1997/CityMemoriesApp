package org.example.backend.repository;

import org.example.backend.model.CityComment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityCommentRepository extends MongoRepository<CityComment, String> {
    List<CityComment> findByCityNameIgnoreCase(String cityName);

}
