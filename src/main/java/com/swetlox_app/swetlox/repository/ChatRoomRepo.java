package com.swetlox_app.swetlox.repository;

import com.swetlox_app.swetlox.entity.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepo extends MongoRepository<ChatRoom,String> {
    boolean existsByChatIdIn(List<String> chatIds);
    Optional<ChatRoom> findByChatIdIn(List<String> chatIds);
}
