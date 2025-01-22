package com.swetlox_app.swetlox.dto.comment;

import com.swetlox_app.swetlox.allenum.EntityType;
import lombok.Data;

@Data
public class CommentRequestDto {
    private String entityId;
    private EntityType entityType;
    private String commentContent;
}
