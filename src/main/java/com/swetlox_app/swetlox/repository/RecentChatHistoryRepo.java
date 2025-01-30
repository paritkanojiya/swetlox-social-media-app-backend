package com.swetlox_app.swetlox.repository;

import com.swetlox_app.swetlox.entity.RecentChatHistory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecentChatHistoryRepo extends MongoRepository<RecentChatHistory,String> {


    List<RecentChatHistory> findByAuthUserId(String authUserId, Sort lastInteraction);

    Optional<RecentChatHistory> findByUserIdAndAuthUserId(String requestedUser, String authUser);

    void deleteByUserId(String id);

    void deleteByAuthUserId(String id);
}
