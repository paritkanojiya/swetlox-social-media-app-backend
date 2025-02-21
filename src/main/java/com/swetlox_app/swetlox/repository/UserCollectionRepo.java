package com.swetlox_app.swetlox.repository;


import com.swetlox_app.swetlox.allenum.EntityType;
import com.swetlox_app.swetlox.entity.UserCollection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserCollectionRepo extends MongoRepository<UserCollection,String> {
    Optional<List<UserCollection>> findByUserId(String id);
    @Query(value = "{ 'userId': ?0, 'postList': ?1 }", exists = true)
    boolean existsByUserIdAndPostListContains(String userId, String postId);

    Optional<UserCollection> findByEntityIdAndUserId(String postId, String id);

    Optional<UserCollection> findByEntityIdAndUserIdAndBookMark(String entityId, String userId, boolean b);

    Optional<List<UserCollection>> findByUserIdAndBookMark(String id, boolean b);

    Optional<List<UserCollection>> findByUserIdAndBookMarkAndEntityType(String id, boolean b, EntityType entityType);

    boolean existsByEntityId(String id);

    UserCollection findByEntityId(String id);

    void deleteAllCollectionByUserId(String id);

    void deleteByUserId(String id);

    Integer countByUserIdAndBookMark(String userId,boolean t);

    Optional<List<UserCollection>> findByEntityIdAndBookMark(String id, boolean b);
}
