package com.swetlox_app.swetlox.repository;

import com.swetlox_app.swetlox.entity.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepo extends MongoRepository<Comment,String> {
    void deleteByIdAndUserId(String commentId, String authUserId);

    List<Comment> findByPostId(String postId);

    void deleteByPostId(String postId);
}

