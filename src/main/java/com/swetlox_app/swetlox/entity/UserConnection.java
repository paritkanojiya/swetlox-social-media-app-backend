package com.swetlox_app.swetlox.entity;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.annotation.Collation;

import java.time.LocalDateTime;
import java.util.List;

@Collation(value = "userConnections")
@Getter
@Setter
@Builder
public class UserConnection {
    @Id
    private String id;
    private String userId;
    private String followerId;
    @CreatedDate
    private LocalDateTime createdAt;
}
