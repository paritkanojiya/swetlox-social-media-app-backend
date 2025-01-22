package com.swetlox_app.swetlox.dto.post;

import com.swetlox_app.swetlox.dto.user.UserDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PostResponseDto {
    private String postId;
    private UserDto postUser;
    private String caption;
    private LocalDateTime createdAt;
    private Integer likeCount;
    private String postURL;
    private boolean isLike;
    private boolean isBookMark;
}
