package com.swetlox_app.swetlox.repository;

import com.swetlox_app.swetlox.entity.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepo extends MongoRepository<Post,String> {
    List<Post> findByUserId(String id);

    boolean existsByIdAndLikedUserListContaining(String postId, String userId);

    Optional<Post> findByIdAndUserId(String postId,String authId);

    void deleteByUserId(String id);
}
