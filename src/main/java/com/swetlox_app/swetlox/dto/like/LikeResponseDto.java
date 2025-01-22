package com.swetlox_app.swetlox.dto.like;

import com.swetlox_app.swetlox.dto.user.UserDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LikeResponseDto {
    private String id;
    private UserDto sender;
    private LocalDateTime createdAt;
}
