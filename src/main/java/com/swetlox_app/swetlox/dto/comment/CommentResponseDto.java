package com.swetlox_app.swetlox.dto.comment;

import com.swetlox_app.swetlox.dto.user.UserDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Builder
public class CommentResponseDto {
    private String id;
    private UserDto sender;
    private String content;
    private LocalDateTime createdAt;
}
