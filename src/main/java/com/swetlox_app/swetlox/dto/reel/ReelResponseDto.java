package com.swetlox_app.swetlox.dto.reel;

import com.swetlox_app.swetlox.dto.user.UserDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReelResponseDto {
    private String reelId;
    private UserDto postUser;
    private String caption;
    private LocalDateTime createdAt;
    private Integer likeCount;
    private boolean isFollow;
    private boolean isBookMark;
    private String reelURL;
    private boolean isLike;
}
