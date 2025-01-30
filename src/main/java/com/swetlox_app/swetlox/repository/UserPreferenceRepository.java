package com.swetlox_app.swetlox.repository;

import com.swetlox_app.swetlox.entity.UserPreference;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPreferenceRepository extends MongoRepository<UserPreference,String> {
    boolean existsByUserId(String userId);

    Optional<UserPreference> findByUserId(String userId);

    void deleteByUserId(String id);
}
