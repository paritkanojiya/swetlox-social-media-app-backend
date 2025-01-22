package com.swetlox_app.swetlox.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chatRoom")
@Data
@Builder
public class ChatRoom {
    @Id
    private String id;
    private String chatId;
    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime lastInteraction;
}
