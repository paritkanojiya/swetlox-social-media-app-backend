package com.swetlox_app.swetlox.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private String userId;
    private String userName;
    private String fullName;
    private String profileURL;
}
