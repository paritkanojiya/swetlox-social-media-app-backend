package com.swetlox_app.swetlox.entity;

import com.swetlox_app.swetlox.allenum.EntityType;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "usercollections")
@Data
@Builder
public class UserCollection {
    @Id
    private String id;
    private String userId;
    private String entityId;
    private EntityType entityType;
    private String entityCaption;
    private boolean bookMark;
    private LocalDateTime createdAt;
}
