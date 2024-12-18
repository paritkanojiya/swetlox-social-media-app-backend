package com.swetlox_app.swetlox.repository;


import com.swetlox_app.swetlox.entity.UserCollection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCollectionRepo extends MongoRepository<UserCollection,String> {
    Optional<UserCollection> findByUserId(String id);
    @Query(value = "{ 'userId': ?0, 'postList': ?1 }", exists = true)
    boolean existsByUserIdAndPostListContains(String userId, String postId);
}
