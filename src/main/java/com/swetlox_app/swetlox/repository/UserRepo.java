package com.swetlox_app.swetlox.repository;

import com.swetlox_app.swetlox.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserRepo extends MongoRepository<User,String> {
    User findByEmail(String email);

    List<User> findByIsVerifiedFalseAndCreatedAtBefore(LocalDateTime tenMinutesAgo);

    @Query("{userName:{$regex:?0,$options:i}}")
    List<User> findByUserName(String q);
}
