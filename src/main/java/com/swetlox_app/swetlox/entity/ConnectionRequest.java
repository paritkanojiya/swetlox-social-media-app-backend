package com.swetlox_app.swetlox.entity;

import com.swetlox_app.swetlox.allenum.ConnectionRequestStatus;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
@Builder
public class ConnectionRequest {
    @Id
    private String id;
    private String senderId;
    private String receiverId;
    private ConnectionRequestStatus status;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
