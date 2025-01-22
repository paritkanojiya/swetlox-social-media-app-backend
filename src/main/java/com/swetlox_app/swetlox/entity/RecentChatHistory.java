package com.swetlox_app.swetlox.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
@Builder
public class RecentChatHistory {
    @Id
    private String id;
    private String userId;
    private String authUserId;
    private LocalDateTime lastInteraction;
}
