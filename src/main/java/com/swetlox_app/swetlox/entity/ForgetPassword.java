package com.swetlox_app.swetlox.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "forgetpassword")
@Data
@Builder
public class ForgetPassword {
    @Id
    private String id;
    private String userId;
    private String token;
    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime expiryTime;
}
