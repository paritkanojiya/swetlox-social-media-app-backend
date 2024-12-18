package com.swetlox_app.swetlox.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "comment")
@Data
@Builder
public class Comment {
    @Id
    private String id;
    private String userId;
    private String userName;
    private String reelId;
    private String postId;
    private String content;
    private LocalDateTime createdAt;
}
