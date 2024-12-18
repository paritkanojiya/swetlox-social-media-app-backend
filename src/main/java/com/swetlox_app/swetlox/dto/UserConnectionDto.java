package com.swetlox_app.swetlox.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserConnectionDto {
    private String userId;
    private String userName;
}
