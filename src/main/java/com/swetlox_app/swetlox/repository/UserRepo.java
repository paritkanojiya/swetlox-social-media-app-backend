package com.swetlox_app.swetlox.repository;

import com.swetlox_app.swetlox.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends MongoRepository<User,String> {
    Optional<User> findByEmail(String email);

    List<User> findByIsVerifiedFalseAndCreatedAtBefore(LocalDateTime tenMinutesAgo);

    @Query("{userName:{$regex:?0,$options:i}}")
    List<User> findByUserName(String q);

    boolean existsByEmail(String email);
}
