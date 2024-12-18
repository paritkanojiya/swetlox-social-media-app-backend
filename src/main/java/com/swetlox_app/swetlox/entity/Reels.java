package com.swetlox_app.swetlox.entity;


import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "reels")
@Data
@Builder
public class Reels {
    @Id
    private String id;
    private String userId;
    private String caption;
    private List<String> likedUserList;
    private LocalDateTime createdAt;
    private String reelsURL;
}
