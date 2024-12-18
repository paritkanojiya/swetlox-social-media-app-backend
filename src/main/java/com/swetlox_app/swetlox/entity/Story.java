package com.swetlox_app.swetlox.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "story")
@Data
@Builder
public class Story {
    private String id;
    private String userId;
    private String imageURL;
    private LocalDateTime timeStamp;
}
