package com.swetlox_app.swetlox.entity;

import com.swetlox_app.swetlox.allenum.MediaType;
import com.swetlox_app.swetlox.allenum.MessageStatus;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "message")
@Data
@Builder
public class Message {
    @Id
    private String id;
    @Indexed
    private String chatRoomId;
    @Indexed
    private String sender;
    @Indexed
    private String recipient;
    private String content;
    private MessageStatus status;
    private MediaType mediaType;
    private String mediaURL;
    @CreatedDate
    private LocalDateTime createdAt;
    @Version
    private Long version;
}
