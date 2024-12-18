package com.swetlox_app.swetlox.repository;


import com.swetlox_app.swetlox.entity.User;
import com.swetlox_app.swetlox.entity.UserConnection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserConnectionRepo extends MongoRepository<UserConnection,String> {

    @Query(value = "{ 'userId': ?0, 'follower': ?1 }", exists = true)
    boolean existsByIdAndFollowerContains(String authId, String userId);
    @Query(value = "{ 'UserId': ?0, 'following': ?1 }", exists = true)
    boolean existsByIdAndFollowingContains(String authId, String userId);
    Optional<UserConnection> findByUserId(String id);

    void deleteByUserId(String id);
}
