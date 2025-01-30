package com.swetlox_app.swetlox.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
public class ProfileModel {
    private String userId;
    private String fullName;
    private String userName;
    private String profileURL;
    private Integer follower;
    private Integer following;
    private Integer postCount;
    private String bio;
    private boolean isAuthUserFollow;
    private boolean isSelfUser;
    private boolean isPrivateProfile;
}
