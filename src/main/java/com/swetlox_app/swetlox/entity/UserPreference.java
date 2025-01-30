package com.swetlox_app.swetlox.entity;


import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Builder
@Data
public class UserPreference {
    @Id
    private String id;
    private String userId;
    private boolean privateAccount;
    private boolean autoFollow;
    private boolean like_commentNotification;
    private boolean followerNotification;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime modifyAt;
}
