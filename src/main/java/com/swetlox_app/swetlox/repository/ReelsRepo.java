package com.swetlox_app.swetlox.repository;

import com.swetlox_app.swetlox.entity.Reels;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReelsRepo extends MongoRepository<Reels,String> {
    Reels deleteByIdAndUserId(String id, String authId);

//    boolean existsByIdAndLikedUserListContaining(String reelId, String userId);

    List<Reels> findByUserId(String authId);

    Page<Reels> findByUserId(String userId, PageRequest pageRequest);

    int countByUserId(String userId);

    void deleteByUserId(String id);
}
