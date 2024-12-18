package com.swetlox_app.swetlox.repository;

import com.swetlox_app.swetlox.entity.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepo extends MongoRepository<Message,String> {
    List<Message> findByChatRoomId(String chatId);
}

