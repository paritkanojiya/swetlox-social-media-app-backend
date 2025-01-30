package com.swetlox_app.swetlox.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private String userId;
    private String userName;
    private String email;
    private Integer totalPost;
    private Integer totalFollower;
    private Integer totalFollowing;
    private String profileURL;
    private boolean suspense;
}
