package com.swetlox_app.swetlox.entity;

import com.swetlox_app.swetlox.allenum.EntityType;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
@Builder
public class Like {
    @Id
    private String id;
    private String userId;
    private String entityId;
    private EntityType entityType;
    private boolean liked;
    @CreatedDate
    private LocalDateTime createdAt;
}
