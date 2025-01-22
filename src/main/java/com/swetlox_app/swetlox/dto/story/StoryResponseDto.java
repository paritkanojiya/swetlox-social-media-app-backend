package com.swetlox_app.swetlox.dto.story;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StoryResponseDto {
    private String userId;
    private String userName;
    private String profileURL;
    private List<StoryDto> storyDtoList;
}
