package com.swetlox_app.swetlox.entity;

import com.swetlox_app.swetlox.allenum.MediaType;
import com.swetlox_app.swetlox.dto.user.UserDto;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Document(collection = "story")
@Data
@Builder
public class Story {
    private String id;
    private String userId;
    private String mediaURL;
    private MediaType mediaType;
    private double duration;
    @CreatedDate
    private LocalDateTime timeStamp;
}

