package com.swetlox_app.swetlox.dto.usercollection;

import com.swetlox_app.swetlox.allenum.EntityType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Builder
public class UserCollectionDto {
    private String id;
    private String entityId;
    private EntityType entityType;
    private String entityURL;
    private String caption;
    private LocalDateTime savedAt;
}
