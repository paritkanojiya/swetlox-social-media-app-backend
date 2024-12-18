package com.swetlox_app.swetlox.repository;

import com.swetlox_app.swetlox.entity.Story;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoryRepo extends MongoRepository<Story,String> {
    Optional<List<Story>> findByUserId(String id);

    void deleteByUserId(String id);
}
