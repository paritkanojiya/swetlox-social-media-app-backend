package com.swetlox_app.swetlox.repository;

import com.swetlox_app.swetlox.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepo extends MongoRepository<Comment,String> {
    void deleteByIdAndUserId(String commentId, String authUserId);

    List<Comment> findByEntityId(String entityId);

    void deleteByEntityId(String postId);

    Page<Comment> findByEntityId(String entityId, PageRequest pageRequest);
}

