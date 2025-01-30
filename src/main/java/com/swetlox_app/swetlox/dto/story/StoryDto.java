package com.swetlox_app.swetlox.dto.story;

import com.swetlox_app.swetlox.allenum.MediaType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StoryDto {
    private String id;
    private MediaType mediaType;
    private String mediaURL;
    private Double duration;
    private boolean isStoryLike;
    private LocalDateTime createdAt;
}
