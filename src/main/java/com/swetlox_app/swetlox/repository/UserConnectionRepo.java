package com.swetlox_app.swetlox.repository;


import com.swetlox_app.swetlox.entity.UserConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserConnectionRepo extends MongoRepository<UserConnection,String> {

    @Query(value = "{ 'userId': ?0, 'followerId': ?1 }", exists = true)
    boolean existsByIdAndFollowerContains(String authId, String userId);
    @Query(value = "{ 'UserId': ?0, 'followerId': ?1 }", exists = true)
    boolean existsByIdAndFollowingContains(String authId, String userId);
    List<UserConnection> findByUserId(String id);
    Page<UserConnection> findByUserId(String id, PageRequest pageRequest);

    void deleteByUserId(String id);
    List<UserConnection> findByFollowerId(String userId);
    Page<UserConnection> findByFollowerId(String userId, PageRequest pageRequest);

    void deleteByFollowerId(String id);

}
