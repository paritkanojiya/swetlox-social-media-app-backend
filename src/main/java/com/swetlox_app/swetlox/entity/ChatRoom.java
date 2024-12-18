package com.swetlox_app.swetlox.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chatRoom")
@Data
@Builder
public class ChatRoom {
    @Id
    private String id;
    private String chatId;
}
