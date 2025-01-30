package com.swetlox_app.swetlox.repository;

import com.swetlox_app.swetlox.allenum.EntityType;
import com.swetlox_app.swetlox.dto.like.LikeResponseDto;
import com.swetlox_app.swetlox.entity.Like;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends MongoRepository<Like,String> {

    void deleteByEntityId(String entityId);

    Page<Like> findByEntityId(String entityId, PageRequest pageRequest);

    boolean existsByEntityIdAndUserId(String entityId, String userId);

    void deleteByEntityIdAndUserId(String storyId, String id);

    Optional<Like> findByEntityIdAndUserId(String entityId, String authId);


    boolean existsByEntityIdAndUserIdAndLiked(String entityId, String userId, boolean b);

    long countByEntityIdAndEntityType(String entityId, EntityType entityType);

    long countByEntityIdAndEntityTypeAndLiked(String entityId, EntityType entityType, boolean b);

    void deleteByUserId(String id);

    Integer countByUserId(String userId);

    List<Like> findByEntityId(String entityId);
}

