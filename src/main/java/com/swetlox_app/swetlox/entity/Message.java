package com.swetlox_app.swetlox.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "message")
@Data
@Builder
public class Message {
    @Id
    private String id;
    private String chatRoomId;
    private String sender;
    private String recipient;
    private String content;
}
