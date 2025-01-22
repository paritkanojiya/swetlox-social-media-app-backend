package com.swetlox_app.swetlox.repository;

import com.swetlox_app.swetlox.dto.like.LikeResponseDto;
import com.swetlox_app.swetlox.entity.Like;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends MongoRepository<Like,String> {

    void deleteByEntityId(String entityId);

    Page<Like> findByEntityId(String entityId, PageRequest pageRequest);
}
