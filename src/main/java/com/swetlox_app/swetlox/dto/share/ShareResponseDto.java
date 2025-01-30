package com.swetlox_app.swetlox.dto.share;

import com.swetlox_app.swetlox.allenum.EntityType;
import com.swetlox_app.swetlox.allenum.MediaType;
import com.swetlox_app.swetlox.dto.user.UserDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ShareResponseDto {
    private String entityId;
    private EntityType entityType;
    private String entityURL;
    private UserDto postUser;
    private LocalDateTime createdAt;
    private boolean isLiked;
    private boolean isBookMark;
    private boolean isAuthenticated;
}
