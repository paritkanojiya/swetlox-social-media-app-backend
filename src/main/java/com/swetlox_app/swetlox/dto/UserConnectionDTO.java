package com.swetlox_app.swetlox.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserConnectionDTO {
    private String userId;
    private String fullName;
    private String userName;
    private String profileURL;
    private boolean isAuthUserFollow;
    private boolean isRequestPending;
}
